package com.eai.application.email;

public record ParsedEmailLead(
        String customerName,
        String customerPhone,
        String customerEmail,
        String vehicleInterest,
        String originalMessage,
        String origin
) {
}
