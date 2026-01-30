package com.example.fileshare.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * In-memory encrypted file storage
 * NO DISK PERSISTENCE - RAM ONLY
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedFile {

    private String fileId;           // Random UUID
    private String originalFilename; // Original name (encrypted)
    private byte[] encryptedData;    // Encrypted file content
    private byte[] encryptionKey;    // Unique key per file
    private byte[] iv;               // Initialization vector
    private long size;               // Original size
    private Instant createdAt;       // Creation time
    private Instant expiresAt;       // Auto-delete time
    private boolean downloaded;      // Track if downloaded (for auto-delete)

    /**
     * Check if file has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if file should be deleted (downloaded + auto-delete enabled)
     */
    public boolean shouldAutoDelete() {
        return downloaded || isExpired();
    }

    /**
     * Mark file as downloaded
     */
    public void markAsDownloaded() {
        this.downloaded = true;
    }

    /**
     * Clear sensitive data from memory (security measure)
     */
    public void clearSensitiveData() {
        if (encryptedData != null) {
            java.util.Arrays.fill(encryptedData, (byte) 0);
        }
        if (encryptionKey != null) {
            java.util.Arrays.fill(encryptionKey, (byte) 0);
        }
        if (iv != null) {
            java.util.Arrays.fill(iv, (byte) 0);
        }
    }
}