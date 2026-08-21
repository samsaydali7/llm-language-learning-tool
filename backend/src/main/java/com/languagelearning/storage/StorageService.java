package com.languagelearning.storage;

import com.languagelearning.common.exception.InvalidRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded book/audio material on the local filesystem under a configurable base path.
 * Files never leave the user's machine (local-first principle, SPEC.md #6).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final StorageProperties storageProperties;

    /**
     * Stores a file under {@code {languageCode}/{bookId}/{subDir}/} and returns the path
     * relative to the storage base path (this relative path is what gets persisted in the DB).
     */
    public String store(String languageCode, Long bookId, String subDir, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Uploaded file must not be empty");
        }
        Path targetDir = resolveBase().resolve(languageCode).resolve(String.valueOf(bookId)).resolve(subDir);
        Path target = null;
        try {
            String safeName = sanitize(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "-" + safeName;
            Files.createDirectories(targetDir);
            target = targetDir.resolve(storedName);
            // Deliberately not MultipartFile#transferTo: on the Servlet-based multipart resolver
            // that backs it, transferTo ultimately calls Part#write, which some container/OS
            // combinations implement as a filesystem rename from the part's temp location - and a
            // rename across mount points (e.g. a bind-mounted or differently-backed volume vs. the
            // container's temp dir) can fail unpredictably. A plain stream copy has no such
            // dependency on the temp file and destination sharing a filesystem.
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return resolveBase().relativize(target).toString();
        } catch (IOException e) {
            log.error("Failed to store uploaded file '{}' ({} bytes, content-type {}) under {}: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType(), targetDir, e.toString());
            throw new IllegalStateException("Failed to store uploaded file: " + e.getMessage(), e);
        }
    }

    public Path resolve(String relativePath) {
        return resolveBase().resolve(relativePath).normalize();
    }

    public InputStream open(String relativePath) {
        try {
            return Files.newInputStream(resolve(relativePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read stored file: " + relativePath, e);
        }
    }

    public long sizeOf(String relativePath) {
        try {
            return Files.size(resolve(relativePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read stored file size: " + relativePath, e);
        }
    }

    private Path resolveBase() {
        Path base = Path.of(storageProperties.basePath());
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create storage base path: " + base, e);
        }
        return base.toAbsolutePath().normalize();
    }

    // Most filesystems (ext4, overlay2, APFS, NTFS...) cap a single path component at 255 bytes.
    // Real downloaded ebooks/audio frequently carry long, metadata-stuffed filenames, so this
    // leaves headroom for the 36-char UUID + "-" prefix store() adds on top of the sanitized name.
    private static final int MAX_SANITIZED_NAME_LENGTH = 150;

    private String sanitize(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }
        String name = Path.of(originalFilename).getFileName().toString();
        String cleaned = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return truncateKeepingExtension(cleaned, MAX_SANITIZED_NAME_LENGTH);
    }

    private String truncateKeepingExtension(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        int dot = name.lastIndexOf('.');
        // Only treat it as a real extension if it's short and not the whole name (e.g. not a
        // filename that merely contains an early '.' from the sanitized-away original text).
        boolean hasExtension = dot > 0 && name.length() - dot <= 10;
        String extension = hasExtension ? name.substring(dot) : "";
        String base = hasExtension ? name.substring(0, dot) : name;
        int allowedBaseLength = Math.max(1, maxLength - extension.length());
        return base.substring(0, Math.min(base.length(), allowedBaseLength)) + extension;
    }

    // Kept for services that already have raw bytes/streams (e.g. tests) rather than a MultipartFile.
    public String storeBytes(String languageCode, Long bookId, String subDir, String fileName, InputStream data) {
        try {
            String safeName = sanitize(fileName);
            String storedName = UUID.randomUUID() + "-" + safeName;
            Path targetDir = resolveBase().resolve(languageCode).resolve(String.valueOf(bookId)).resolve(subDir);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(storedName);
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            return resolveBase().relativize(target).toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }
}
