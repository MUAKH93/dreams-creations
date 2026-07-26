package com.dreams.dreamscreations.service;

public interface EmailService {
    void send(String to, String subject, String body);
    boolean isConfigured();
}
