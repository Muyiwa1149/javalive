package com.javalive.backend.service.exchange;

import com.javalive.backend.dto.exchange.AssetBalance;
import com.javalive.backend.dto.exchange.ExchangeQuoteResponse;
import com.javalive.backend.dto.exchange.ExchangeRecordSummary;
import com.javalive.backend.dto.exchange.ExchangeRequest;
import com.javalive.backend.entity.CryptoAccount;
import com.javalive.backend.entity.CryptoRecord;
import com.javalive.backend.entity.Instrument;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.CryptoAccountRepository;
import com.javalive.backend.repository.CryptoRecordRepository;
import com.javalive.backend.repository.InstrumentRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Mirrors the source app's ExchangeController — swap between the user's USD balance and any of
 * the 10 tracked crypto sub-balances on {@code CryptoAccount}, or crypto-to-crypto directly.
 *
 * <p><b>Security fix</b>: the source's {@code exchange()} endpoint trusted a client-supplied
 * {@code quantity} field directly when crediting the destination currency — a client could submit
 * any quantity regardless of the real exchange rate, fabricating funds. Here the server always
 * computes the quantity itself from the live {@link Instrument} price, the same way the quote
 * endpoint does; there is no client-supplied quantity field at all.
 */
@Service
public class ExchangeService {

    private static final Set<String> CRYPTO_CURRENCIES = Set.of("btc", "eth", "ltc", "xrp", "link", "bnb", "aave", "usdt", "xlm", "bch", "ada");

    private final CryptoAccountRepository cryptoAccountRepository;
    private final CryptoRecordRepository cryptoRecordRepository;
    private final InstrumentRepository instrumentRepository;
    private final UserRepository userRepository;
    private final SettingsService settingsService;

    private final Map<String, Function<CryptoAccount, BigDecimal>> getters = new LinkedHashMap<>();
    private final Map<String, BiConsumer<CryptoAccount, BigDecimal>> setters = new LinkedHashMap<>();

    public ExchangeService(CryptoAccountRepository cryptoAccountRepository, CryptoRecordRepository cryptoRecordRepository,
                            InstrumentRepository instrumentRepository, UserRepository userRepository,
                            SettingsService settingsService) {
        this.cryptoAccountRepository = cryptoAccountRepository;
        this.cryptoRecordRepository = cryptoRecordRepository;
        this.instrumentRepository = instrumentRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;

        getters.put("btc", CryptoAccount::getBtc); setters.put("btc", CryptoAccount::setBtc);
        getters.put("eth", CryptoAccount::getEth); setters.put("eth", CryptoAccount::setEth);
        getters.put("ltc", CryptoAccount::getLtc); setters.put("ltc", CryptoAccount::setLtc);
        getters.put("xrp", CryptoAccount::getXrp); setters.put("xrp", CryptoAccount::setXrp);
        getters.put("link", CryptoAccount::getLink); setters.put("link", CryptoAccount::setLink);
        getters.put("bnb", CryptoAccount::getBnb); setters.put("bnb", CryptoAccount::setBnb);
        getters.put("aave", CryptoAccount::getAave); setters.put("aave", CryptoAccount::setAave);
        getters.put("usdt", CryptoAccount::getUsdt); setters.put("usdt", CryptoAccount::setUsdt);
        getters.put("xlm", CryptoAccount::getXlm); setters.put("xlm", CryptoAccount::setXlm);
        getters.put("bch", CryptoAccount::getBch); setters.put("bch", CryptoAccount::setBch);
        getters.put("ada", CryptoAccount::getAda); setters.put("ada", CryptoAccount::setAda);
    }

    private void requireFeatureEnabled() {
        if (!Boolean.TRUE.equals(settingsService.get().getUseCryptoFeature())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "The exchange feature is not currently available.");
        }
    }

    @Transactional
    public List<AssetBalance> assetBalances(Long userId) {
        requireFeatureEnabled();
        User user = findUser(userId);
        CryptoAccount account = cryptoAccountRepository.findByUserId(userId)
                .orElseGet(() -> cryptoAccountRepository.save(CryptoAccount.builder()
                        .user(user).btc(BigDecimal.ZERO).eth(BigDecimal.ZERO).ltc(BigDecimal.ZERO).xrp(BigDecimal.ZERO)
                        .link(BigDecimal.ZERO).bnb(BigDecimal.ZERO).aave(BigDecimal.ZERO).usdt(BigDecimal.ZERO)
                        .xlm(BigDecimal.ZERO).bch(BigDecimal.ZERO).ada(BigDecimal.ZERO)
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()));

        java.util.List<AssetBalance> balances = new java.util.ArrayList<>();
        balances.add(new AssetBalance("usd", user.getAccountBalance(), user.getAccountBalance()));
        for (String currency : CRYPTO_CURRENCIES) {
            BigDecimal balance = getters.get(currency).apply(account);
            BigDecimal rate = rateFor(currency);
            balances.add(new AssetBalance(currency, balance, balance.multiply(rate).setScale(2, RoundingMode.HALF_UP)));
        }
        return balances;
    }

    @Transactional(readOnly = true)
    public ExchangeQuoteResponse quote(String source, String destination, BigDecimal amount) {
        requireFeatureEnabled();
        validatePair(source, destination);
        BigDecimal feePct = feePercentage();
        BigDecimal fee = amount.multiply(feePct).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal amountAfterFee = amount.subtract(fee);
        BigDecimal quantity = convert(source, destination, amountAfterFee);
        BigDecimal rate = convert(source, destination, BigDecimal.ONE);
        return new ExchangeQuoteResponse(quantity, fee, feePct, rate);
    }

    @Transactional
    public ExchangeRecordSummary exchange(Long userId, ExchangeRequest request) {
        requireFeatureEnabled();
        String source = request.source().toLowerCase(Locale.ROOT);
        String destination = request.destination().toLowerCase(Locale.ROOT);
        validatePair(source, destination);

        User user = findUser(userId);
        CryptoAccount account = cryptoAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Crypto account not found."));

        BigDecimal feePct = feePercentage();
        BigDecimal fee = request.amount().multiply(feePct).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal amountAfterFee = request.amount().subtract(fee);
        BigDecimal quantity = convert(source, destination, amountAfterFee);

        LocalDateTime now = LocalDateTime.now();

        if ("usd".equals(source)) {
            if (user.getAccountBalance().compareTo(request.amount()) < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient funds in your USD account.");
            }
            user.setAccountBalance(user.getAccountBalance().subtract(request.amount()));
            userRepository.save(user);
            credit(account, destination, quantity);
        } else if ("usd".equals(destination)) {
            BigDecimal balance = getters.get(source).apply(account);
            if (balance.compareTo(request.amount()) < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient funds in your " + source.toUpperCase(Locale.ROOT) + " account.");
            }
            debit(account, source, request.amount());
            user.setAccountBalance(user.getAccountBalance().add(quantity));
            userRepository.save(user);
        } else {
            BigDecimal balance = getters.get(source).apply(account);
            if (balance.compareTo(request.amount()) < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient funds in your " + source.toUpperCase(Locale.ROOT) + " account.");
            }
            debit(account, source, request.amount());
            credit(account, destination, quantity);
        }
        account.setUpdatedAt(now);
        cryptoAccountRepository.save(account);

        CryptoRecord record = CryptoRecord.builder()
                .user(user).source(source.toUpperCase(Locale.ROOT)).dest(destination.toUpperCase(Locale.ROOT))
                .amount(request.amount()).quantity(quantity).createdAt(now).updatedAt(now).build();
        record = cryptoRecordRepository.save(record);

        return ExchangeRecordSummary.from(record);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRecordSummary> history(Long userId) {
        return cryptoRecordRepository.findByUserIdOrderByIdDesc(userId).stream().map(ExchangeRecordSummary::from).toList();
    }

    private void validatePair(String source, String destination) {
        if (source.equals(destination)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Source and destination currencies cannot be the same.");
        }
        if (!"usd".equals(source) && !CRYPTO_CURRENCIES.contains(source)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported source currency.");
        }
        if (!"usd".equals(destination) && !CRYPTO_CURRENCIES.contains(destination)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported destination currency.");
        }
    }

    private BigDecimal convert(String source, String destination, BigDecimal amount) {
        if ("usd".equals(source) && "usd".equals(destination)) {
            return amount.setScale(8, RoundingMode.HALF_UP);
        }
        if ("usd".equals(destination)) {
            return amount.multiply(rateFor(source)).setScale(8, RoundingMode.HALF_UP);
        }
        if ("usd".equals(source)) {
            return amount.divide(rateFor(destination), 8, RoundingMode.HALF_UP);
        }
        BigDecimal rate = rateFor(source).divide(rateFor(destination), 12, RoundingMode.HALF_UP);
        return amount.multiply(rate).setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal rateFor(String currency) {
        if ("usd".equals(currency)) return BigDecimal.ONE;
        if ("usdt".equals(currency)) return BigDecimal.ONE;
        Instrument instrument = instrumentRepository.findBySymbol(currency.toUpperCase(Locale.ROOT) + "/USD")
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "Unable to get exchange rate for " + currency.toUpperCase(Locale.ROOT) + ". Please try again later."));
        if (instrument.getPrice() == null || instrument.getPrice().signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Unable to get exchange rate for " + currency.toUpperCase(Locale.ROOT) + ". Please try again later.");
        }
        return instrument.getPrice();
    }

    private BigDecimal feePercentage() {
        BigDecimal fee = settingsService.get().getExchangeFeePct();
        return fee != null ? fee : BigDecimal.ZERO;
    }

    private void credit(CryptoAccount account, String currency, BigDecimal quantity) {
        setters.get(currency).accept(account, getters.get(currency).apply(account).add(quantity));
    }

    private void debit(CryptoAccount account, String currency, BigDecimal quantity) {
        setters.get(currency).accept(account, getters.get(currency).apply(account).subtract(quantity));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
