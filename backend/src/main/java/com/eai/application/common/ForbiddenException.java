package com.eai.application.common;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}
