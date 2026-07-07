package com.eai.application.common;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
