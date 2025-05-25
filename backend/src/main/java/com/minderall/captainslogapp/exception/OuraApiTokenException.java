package com.minderall.captainslogapp.exception;

// Specific exception for token-related issues (e.g., expired, invalid, missing)
public class OuraApiTokenException extends OuraApiException {
    public OuraApiTokenException(String message) {
        super(message);
    }

    public OuraApiTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}