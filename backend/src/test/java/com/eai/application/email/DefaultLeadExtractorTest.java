package com.eai.application.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultLeadExtractorTest {

    private static final EmailMessage MESSAGE = new EmailMessage(
            "Novo lead",
            "portal@example.com",
            "Nome: Maria",
            Instant.parse("2026-01-01T00:00:00Z")
    );

    @DisplayName("Extrator usa primeiro parser compativel")
    @Test
    void extractorUsesFirstSupportedParser() {
        EmailParser unsupportedParser = mock(EmailParser.class);
        EmailParser supportedParser = mock(EmailParser.class);
        ParsedEmailLead parsedLead = new ParsedEmailLead("Maria", "11999990000", null, "Honda Civic", "Nome: Maria", "EMAIL");
        when(unsupportedParser.supports(MESSAGE)).thenReturn(false);
        when(supportedParser.supports(MESSAGE)).thenReturn(true);
        when(supportedParser.parse(MESSAGE)).thenReturn(parsedLead);

        DefaultLeadExtractor extractor = new DefaultLeadExtractor(List.of(unsupportedParser, supportedParser));

        assertThat(extractor.extract(MESSAGE)).isSameAs(parsedLead);
        verify(unsupportedParser, never()).parse(MESSAGE);
        verify(supportedParser).parse(MESSAGE);
    }

    @DisplayName("Extrator retorna nulo quando nenhum parser for compativel")
    @Test
    void extractorReturnsNullWhenNoParserSupportsMessage() {
        EmailParser parser = mock(EmailParser.class);
        when(parser.supports(MESSAGE)).thenReturn(false);

        DefaultLeadExtractor extractor = new DefaultLeadExtractor(List.of(parser));

        assertThat(extractor.extract(MESSAGE)).isNull();
        verify(parser, never()).parse(MESSAGE);
    }
}
