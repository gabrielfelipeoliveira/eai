package com.eai.application.tenant;

public record CreateCompanyCommand(String name, String document, String email, String phone) {
}
