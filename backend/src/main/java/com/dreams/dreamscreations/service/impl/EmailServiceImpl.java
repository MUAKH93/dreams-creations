package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@dreamscreations.local}")
    private String mailFrom;

    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean isConfigured() {
        return mailEnabled && mailSender != null;
    }

    @Override
    public void send(String to, String subject, String body) {
        if (!isConfigured()) {
            log.info("Email (not sent — mail disabled or SMTP not configured) to={} subject={} body={}", to, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent to {} — {}", to, subject);
    }
}
