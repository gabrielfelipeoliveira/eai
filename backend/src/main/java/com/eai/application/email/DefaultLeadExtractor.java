package com.eai.application.email;

import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultLeadExtractor implements LeadExtractor {

    private final List<EmailParser> parsers;

    @Override
    public ParsedEmailLead extract(EmailMessage message) {
        return parsers.stream()
                .filter(parser -> parser.supports(message))
                .findFirst()
                .map(parser -> parser.parse(message))
                .orElse(null);
    }
}
