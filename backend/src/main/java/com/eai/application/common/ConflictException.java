package com.eai.application.common;

public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
