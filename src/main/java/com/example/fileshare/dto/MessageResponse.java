package com.example.fileshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String messageId;
    private String content;
    private String senderId;
    private Instant timestamp;
    private boolean success;
    private String error;

    public static MessageResponse success(String messageId, String content, String senderId, Instant timestamp) {
        return MessageResponse.builder()
                .messageId(messageId)
                .content(content)
                .senderId(senderId)
                .timestamp(timestamp)
                .success(true)
                .build();
    }

    public static MessageResponse error(String error) {
        return MessageResponse.builder()
                .success(false)
                .error(error)
                .build();
    }
}