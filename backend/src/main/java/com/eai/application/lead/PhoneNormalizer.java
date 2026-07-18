package com.eai.application.lead;

import java.util.regex.Pattern;

public final class PhoneNormalizer {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private PhoneNormalizer() {
    }

    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String trimmed = phone.trim();
        if (trimmed.startsWith("+")) {
            if (!E164.matcher(trimmed).matches()) {
                throw new IllegalArgumentException("customerPhone must be E.164 or a Brazilian phone with 10 or 11 digits");
            }
            return trimmed;
        }
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.length() == 10 || digits.length() == 11) {
            return "+55" + digits;
        }
        if (digits.startsWith("55") && isValidE164Digits(digits)) {
            return "+" + digits;
        }
        throw new IllegalArgumentException("customerPhone must be E.164 or a Brazilian phone with 10 or 11 digits");
    }

    private static boolean isValidE164Digits(String digits) {
        return digits.length() >= 8 && digits.length() <= 15 && digits.charAt(0) != '0';
    }
}
