package com.example.fileshare.controller;

import com.example.fileshare.dto.FileResponse;
import com.example.fileshare.model.EncryptedFile;
import com.example.fileshare.service.InMemoryStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Secure File Transfer REST API
 * NO LOGGING - NO TRACES
 */
@RestController
@RequestMapping("/api/checking")
@RequiredArgsConstructor
public class FileController {

    private final InMemoryStorageService storageService;

    /**
     * Upload file - Returns unique file ID
     * POST /api/files/upload
     */
    @PostMapping(value = "/error", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(FileResponse.error("File is empty"));
            }

            // Size check (15MB limit)
            if (file.getSize() > 15 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(FileResponse.error("File too large (max 15MB)"));
            }

            // Store encrypted file in memory
            String fileId = storageService.storeFile(
                    file.getOriginalFilename(),
                    file.getBytes()
            );

            return ResponseEntity.ok(FileResponse.success(
                    fileId,
                    "File uploaded successfully",
                    60L
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(FileResponse.error("Upload failed"));
        }
    }

    /**
     * Download file by ID
     * GET /api/files/download/{fileId}
     */
    @GetMapping("/errors/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        try {
            // Retrieve encrypted file
            EncryptedFile encryptedFile = storageService.retrieveFile(fileId);

            // Decrypt file data
            byte[] decryptedData = storageService.decryptFile(encryptedFile);

            // Mark as downloaded (triggers auto-delete)
            storageService.markAsDownloaded(fileId);

            // Return file with proper headers
            ByteArrayResource resource = new ByteArrayResource(decryptedData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encryptedFile.getOriginalFilename() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(decryptedData.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if file exists
     * GET /api/files/check/{fileId}
     */
    @GetMapping("/check/{fileId}")
    public ResponseEntity<FileResponse> checkFile(@PathVariable String fileId) {
        try {
            EncryptedFile file = storageService.retrieveFile(fileId);
            return ResponseEntity.ok(FileResponse.success(
                    fileId,
                    "File exists",
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(FileResponse.error("File not found or expired"));
        }
    }

    /**
     * Get server status (no sensitive info)
     * GET /api/files/status
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Server running - " +
                storageService.getFileCount() + " files in memory");
    }

    /**
     * Emergency delete all files
     * POST /api/files/clear
     */
    @PostMapping("/clear")
    public ResponseEntity<FileResponse> clearAllFiles() {
        storageService.clearAll();
        return ResponseEntity.ok(FileResponse.success(
                null,
                "All files cleared from memory",
                null
        ));
    }
}