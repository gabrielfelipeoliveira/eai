package com.eai.application.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GenericEmailParserTest {

    private final GenericEmailParser parser = new GenericEmailParser();

    @DisplayName("Parser extrai lead de e-mail com campos rotulados")
    @Test
    void shouldParseLabeledLeadEmail() {
        EmailMessage message = new EmailMessage(
                "Novo lead recebido",
                "portal@example.com",
                """
                        Origem: Webmotors
                        Nome: Maria Souza
                        Telefone: (11) 99999-8888
                        E-mail: maria@example.com
                        Veiculo: Honda Civic 2022
                        Mensagem: Tenho interesse nesse veiculo.
                        """,
                Instant.parse("2026-07-07T12:00:00Z")
        );

        ParsedEmailLead parsed = parser.parse(message);

        assertThat(parsed.customerName()).isEqualTo("Maria Souza");
        assertThat(parsed.customerPhone()).isEqualTo("(11) 99999-8888");
        assertThat(parsed.customerEmail()).isEqualTo("maria@example.com");
        assertThat(parsed.vehicleInterest()).isEqualTo("Honda Civic 2022");
        assertThat(parsed.origin()).isEqualTo("Webmotors");
        assertThat(parsed.originalMessage()).contains("Tenho interesse");
    }

    @DisplayName("Parser usa regex como fallback para telefone e e-mail")
    @Test
    void shouldFallbackToRegexForPhoneAndEmail() {
        EmailMessage message = new EmailMessage(
                "Interesse no Corolla",
                "site@example.com",
                "Cliente pediu contato pelo telefone 11988887777 e email joao@example.com.",
                Instant.parse("2026-07-07T12:00:00Z")
        );

        ParsedEmailLead parsed = parser.parse(message);

        assertThat(parsed.customerPhone()).isEqualTo("11988887777");
        assertThat(parsed.customerEmail()).isEqualTo("joao@example.com");
        assertThat(parsed.origin()).isEqualTo("EMAIL");
        assertThat(parsed.originalMessage()).contains("Cliente pediu contato");
    }
}
