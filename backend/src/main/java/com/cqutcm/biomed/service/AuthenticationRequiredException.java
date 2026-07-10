package com.cqutcm.biomed.service;

public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
