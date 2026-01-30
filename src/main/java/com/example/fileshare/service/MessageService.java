package com.example.fileshare.service;

import com.example.fileshare.model.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final Map<String, Message> messageStore = new ConcurrentHashMap<>();
    private static final long MESSAGE_LIFETIME_MINUTES = 60;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MESSAGE_ID_LENGTH = 8;
    private final SecureRandom secureRandom = new SecureRandom();

    private String generateMessageId() {
        StringBuilder id = new StringBuilder(MESSAGE_ID_LENGTH);
        for (int i = 0; i < MESSAGE_ID_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            id.append(CHARACTERS.charAt(index));
        }
        String messageId = id.toString();
        if (messageStore.containsKey(messageId)) {
            return generateMessageId();
        }
        return messageId;
    }

    public Message sendMessage(String content, String senderId) {
        String messageId = generateMessageId();
        Instant now = Instant.now();

        Message message = Message.builder()
                .messageId(messageId)
                .content(content)
                .senderId(senderId)
                .timestamp(now)
                .expiresAt(now.plus(MESSAGE_LIFETIME_MINUTES, ChronoUnit.MINUTES))
                .build();

        messageStore.put(messageId, message);
        return message;
    }

    public List<Message> getAllMessages() {
        return messageStore.values().stream()
                .filter(msg -> !msg.isExpired())
                .sorted((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .collect(Collectors.toList());
    }

    public Message getMessage(String messageId) {
        Message message = messageStore.get(messageId);
        if (message == null || message.isExpired()) {
            return null;
        }
        return message;
    }

    public void deleteMessage(String messageId) {
        messageStore.remove(messageId);
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupExpiredMessages() {
        messageStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public int getMessageCount() {
        return (int) messageStore.values().stream()
                .filter(msg -> !msg.isExpired())
                .count();
    }

    public void clearAll() {
        messageStore.clear();
    }
}