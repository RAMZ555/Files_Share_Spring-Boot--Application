package com.example.fileshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private String fileId;
    private String message;
    private boolean success;
    private Long expiresInMinutes;

    public static FileResponse success(String fileId, String message, Long expiresInMinutes) {
        return FileResponse.builder()
                .fileId(fileId)
                .message(message)
                .success(true)
                .expiresInMinutes(expiresInMinutes)
                .build();
    }

    public static FileResponse error(String message) {
        return FileResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}