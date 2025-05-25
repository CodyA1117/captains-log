package com.minderall.captainslogapp.exception;

public class OuraApiException extends RuntimeException {
    public OuraApiException(String message) {
        super(message);
    }

    public OuraApiException(String message, Throwable cause) {
        super(message, cause);
    }
}