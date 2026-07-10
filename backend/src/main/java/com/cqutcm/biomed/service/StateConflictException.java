package com.cqutcm.biomed.service;

public class StateConflictException extends RuntimeException {
    public StateConflictException(String message) {
        super(message);
    }
}
