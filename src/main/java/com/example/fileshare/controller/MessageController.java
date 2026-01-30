package com.example.fileshare.controller;

import com.example.fileshare.dto.MessageResponse;
import com.example.fileshare.model.Message;
import com.example.fileshare.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String senderId = request.get("senderId");

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("Message content cannot be empty"));
            }

            if (senderId == null || senderId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("Sender ID is required"));
            }

            Message message = messageService.sendMessage(content, senderId);
            return ResponseEntity.ok(MessageResponse.success(
                    message.getMessageId(),
                    message.getContent(),
                    message.getSenderId(),
                    message.getTimestamp()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.error("Failed to send message"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        try {
            List<MessageResponse> messages = messageService.getAllMessages().stream()
                    .map(msg -> MessageResponse.success(
                            msg.getMessageId(),
                            msg.getContent(),
                            msg.getSenderId(),
                            msg.getTimestamp()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable String messageId) {
        try {
            Message message = messageService.getMessage(messageId);
            if (message == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(MessageResponse.success(
                    message.getMessageId(),
                    message.getContent(),
                    message.getSenderId(),
                    message.getTimestamp()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<MessageResponse> deleteMessage(@PathVariable String messageId) {
        try {
            messageService.deleteMessage(messageId);
            return ResponseEntity.ok(MessageResponse.builder()
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.error("Failed to delete message"));
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<MessageResponse> clearAllMessages() {
        messageService.clearAll();
        return ResponseEntity.ok(MessageResponse.builder()
                .success(true)
                .build());
    }
}