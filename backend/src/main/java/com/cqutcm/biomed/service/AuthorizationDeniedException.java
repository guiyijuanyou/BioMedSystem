package com.cqutcm.biomed.service;

public class AuthorizationDeniedException extends RuntimeException {
    public AuthorizationDeniedException(String message) {
        super(message);
    }
}
