package com.example.LunchGo.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public interface EmailService {
    String createCode();

    String setContext(String code);

    MimeMessage createEmailForm(String email) throws MessagingException;

    void sendEmail(String toEmail) throws MessagingException;

    Boolean verifyEmailCode(String email, String code);
}
