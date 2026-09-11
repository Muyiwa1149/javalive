package com.javalive.backend.service.wallet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Real BIP-39 mnemonic checksum validation — same algorithm as the source app's
 * {@code CryptoWalletService} (backed by the {@code bitwasp/bitcoin} library), reimplemented
 * directly against the standard 2048-word English wordlist rather than pulling in a full Bitcoin
 * library for this one check. Wordlist extracted verbatim from that same library's bundled data.
 */
@Component
public class Bip39Validator {

    private static final Set<Integer> VALID_WORD_COUNTS = Set.of(12, 15, 18, 21, 24);

    private final List<String> words = new ArrayList<>(2048);
    private final Map<String, Integer> indexByWord = new HashMap<>(4096);

    public Bip39Validator() {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("bip39/english.txt").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                words.add(line);
                indexByWord.put(line, i++);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load BIP-39 wordlist", e);
        }
        if (words.size() != 2048) {
            throw new IllegalStateException("BIP-39 wordlist must contain exactly 2048 words, found " + words.size());
        }
    }

    /** Used to give a specific "word #N isn't recognized" error instead of a generic checksum failure. */
    public boolean knowsWord(String word) {
        return indexByWord.containsKey(word.toLowerCase());
    }

    /** True only if the mnemonic has a valid word count, every word is in the wordlist, and the checksum bits match. */
    public boolean isValid(String mnemonic) {
        String[] mnemonicWords = mnemonic.trim().toLowerCase().split("\\s+");
        if (!VALID_WORD_COUNTS.contains(mnemonicWords.length)) {
            return false;
        }

        StringBuilder bits = new StringBuilder(mnemonicWords.length * 11);
        for (String word : mnemonicWords) {
            Integer index = indexByWord.get(word);
            if (index == null) {
                return false;
            }
            bits.append(String.format("%11s", Integer.toBinaryString(index)).replace(' ', '0'));
        }

        int totalBits = bits.length();
        int checksumBits = totalBits / 33;
        int entropyBits = totalBits - checksumBits;

        byte[] entropy = new byte[entropyBits / 8];
        for (int i = 0; i < entropy.length; i++) {
            entropy[i] = (byte) Integer.parseInt(bits.substring(i * 8, i * 8 + 8), 2);
        }

        String expectedChecksum = bits.substring(entropyBits);
        String actualChecksum = sha256BitsPrefix(entropy, checksumBits);
        return expectedChecksum.equals(actualChecksum);
    }

    private String sha256BitsPrefix(byte[] data, int bitCount) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder allBits = new StringBuilder();
            for (byte b : hash) {
                allBits.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }
            return allBits.substring(0, bitCount);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
