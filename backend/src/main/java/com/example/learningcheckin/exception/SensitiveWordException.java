package com.example.learningcheckin.exception;

public class SensitiveWordException extends RuntimeException {
    public SensitiveWordException(String message) {
        super(message);
    }
}
