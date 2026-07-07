package com.eai.application.email;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
@Order
public class GenericEmailParser implements EmailParser {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?55\\s*)?(?:\\(?\\d{2}\\)?\\s*)?9?\\d{4}[-\\s]?\\d{4}");

    @Override
    public boolean supports(EmailMessage message) {
        return true;
    }

    @Override
    public ParsedEmailLead parse(EmailMessage message) {
        String body = message.body() == null ? "" : message.body();
        String subject = message.subject() == null ? "" : message.subject();
        String combined = subject + "\n" + body;
        return new ParsedEmailLead(
                firstField(combined, "nome", "cliente", "name"),
                firstMatch(PHONE_PATTERN, combined),
                firstField(combined, "e-mail", "email", "mail") == null ? firstMatch(EMAIL_PATTERN, combined) : firstField(combined, "e-mail", "email", "mail"),
                firstField(combined, "veiculo", "veículo", "carro", "modelo", "interesse"),
                body.isBlank() ? subject : body,
                firstField(combined, "origem", "source") == null ? "EMAIL" : firstField(combined, "origem", "source")
        );
    }

    private String firstField(String text, String... labels) {
        for (String label : labels) {
            Pattern pattern = Pattern.compile("(?im)^\\s*" + Pattern.quote(label) + "\\s*[:=-]\\s*(.+?)\\s*$");
            var matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    private String firstMatch(Pattern pattern, String text) {
        var matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim().toLowerCase(Locale.ROOT);
        }
        return null;
    }
}
