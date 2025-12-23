package com.example.learningcheckin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to stop process? Or maybe throw.
            e.printStackTrace();
            System.err.println("Failed to send email to: " + to);
        }
    }

    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Learning Checkin System";
        String content = "Hello " + username + ",\n\nWelcome to join us! Please login and start your learning journey.\n\nBest Regards,\nLearning Checkin Team";
        sendSimpleMail(to, subject, content);
    }
}
