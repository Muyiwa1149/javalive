package com.javalive.backend.service.storage;

import com.javalive.backend.web.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Saves user-uploaded files (KYC docs, deposit proofs, avatars, etc.) to the same disk that
 * {@link com.javalive.backend.config.StorageConfig} serves at {@code /storage/**}, mirroring the
 * source app's `$file->store('uploads', 'public')` pattern. Returns a path relative to the storage
 * root (e.g. {@code "uploads/xxxx.png"}), matching what's already stored for migrated media.
 */
@Service
public class FileStorageService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    @Value("${javalive.storage.public-path}")
    private String publicStoragePath;

    /** Validates the file has one of the given allowed extensions (case-insensitive), throwing a 400 if not. */
    public String storeImage(MultipartFile file, String subdirectory, List<String> allowedExtensions) {
        String extension = extensionOf(file);
        Set<String> allowed = allowedExtensions == null || allowedExtensions.isEmpty()
                ? IMAGE_EXTENSIONS
                : Set.copyOf(allowedExtensions);
        if (!allowed.contains(extension.toLowerCase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Unaccepted image uploaded, please make sure to upload the correct document.");
        }
        return store(file, subdirectory, extension);
    }

    private String store(MultipartFile file, String subdirectory, String extension) {
        try {
            Path dir = Path.of(publicStoragePath, subdirectory);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return subdirectory + "/" + filename;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save uploaded file.");
        }
    }

    /** Silently no-ops if the path is blank or the file doesn't exist — mirrors source's exists-check-then-delete. */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Files.deleteIfExists(Path.of(publicStoragePath, relativePath));
        } catch (IOException ignored) {
            // best-effort cleanup, matches source's silent behavior when the file is already gone
        }
    }

    private String extensionOf(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file has no extension.");
        }
        return original.substring(original.lastIndexOf('.') + 1);
    }
}
