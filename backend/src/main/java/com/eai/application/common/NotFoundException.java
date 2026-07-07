package com.eai.application.common;

public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
