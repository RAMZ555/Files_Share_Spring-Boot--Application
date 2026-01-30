package com.example.fileshare.service;

import com.example.fileshare.model.EncryptedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

/**
 * 100% IN-MEMORY storage
 * NO DISK WRITES - Files stored in RAM only
 * Auto-cleanup of expired files
 */
@Service
@RequiredArgsConstructor
public class InMemoryStorageService {

    private final Map<String, EncryptedFile> fileStore = new ConcurrentHashMap<>();
    private final com.example.fileshare.service.EncryptionService encryptionService;

    private static final long FILE_LIFETIME_MINUTES = 60;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int FILE_ID_LENGTH = 3;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate short unique file ID (10 characters)
     */
    private String generateShortFileId() {
        StringBuilder fileId = new StringBuilder(FILE_ID_LENGTH);

        // Generate random alphanumeric ID
        for (int i = 0; i < FILE_ID_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            fileId.append(CHARACTERS.charAt(index));
        }

        // Check for collision (very rare)
        String id = fileId.toString();
        if (fileStore.containsKey(id)) {
            return generateShortFileId(); // Retry if collision
        }

        return id;
    }

    /**
     * Store encrypted file in memory
     */
    public String storeFile(String filename, byte[] data) throws Exception {
        // Generate SHORT unique file ID (10 characters)
        String fileId = generateShortFileId();

        // Generate unique encryption key and IV
        byte[] key = encryptionService.generateKey();
        byte[] iv = encryptionService.generateIV();

        // Encrypt the file data
        byte[] encryptedData = encryptionService.encrypt(data, key, iv);

        // Create encrypted file object
        EncryptedFile encryptedFile = EncryptedFile.builder()
                .fileId(fileId)
                .originalFilename(filename)
                .encryptedData(encryptedData)
                .encryptionKey(key)
                .iv(iv)
                .size(data.length)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(FILE_LIFETIME_MINUTES, ChronoUnit.MINUTES))
                .downloaded(false)
                .build();

        // Store in memory
        fileStore.put(fileId, encryptedFile);

        // Securely wipe original data from method scope
        encryptionService.secureWipe(data);

        return fileId;
    }

    /**
     * Retrieve and decrypt file from memory
     */
    public EncryptedFile retrieveFile(String fileId) throws Exception {
        EncryptedFile encryptedFile = fileStore.get(fileId);

        if (encryptedFile == null) {
            throw new RuntimeException("File not found");
        }

        if (encryptedFile.isExpired()) {
            deleteFile(fileId);
            throw new RuntimeException("File has expired");
        }

        return encryptedFile;
    }

    /**
     * Decrypt file data
     */
    public byte[] decryptFile(EncryptedFile file) throws Exception {
        return encryptionService.decrypt(
                file.getEncryptedData(),
                file.getEncryptionKey(),
                file.getIv()
        );
    }

    /**
     * Delete file and mark for download
     */
    public void markAsDownloaded(String fileId) {
        EncryptedFile file = fileStore.get(fileId);
        if (file != null) {
            file.markAsDownloaded();
            // Auto-delete immediately after download
            deleteFile(fileId);
        }
    }

    /**
     * Securely delete file from memory
     */
    public void deleteFile(String fileId) {
        EncryptedFile file = fileStore.remove(fileId);
        if (file != null) {
            // Securely wipe sensitive data
            file.clearSensitiveData();
        }
    }

    /**
     * Auto-cleanup expired files every 5 minutes
     * Runs in background
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void cleanupExpiredFiles() {
        fileStore.entrySet().removeIf(entry -> {
            EncryptedFile file = entry.getValue();
            if (file.shouldAutoDelete()) {
                file.clearSensitiveData();
                return true;
            }
            return false;
        });
    }

    /**
     * Get total files in memory
     */
    public int getFileCount() {
        return fileStore.size();
    }

    /**
     * Emergency - Clear all files from memory
     */
    public void clearAll() {
        fileStore.values().forEach(EncryptedFile::clearSensitiveData);
        fileStore.clear();
    }
}