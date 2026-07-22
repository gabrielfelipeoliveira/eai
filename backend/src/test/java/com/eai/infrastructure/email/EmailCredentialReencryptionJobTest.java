package com.eai.infrastructure.email;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.eai.application.email.EmailCredentialReencryptionResult;
import com.eai.application.email.EmailCredentialReencryptionService;
import com.eai.infrastructure.config.EmailCredentialEncryptionProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailCredentialReencryptionJobTest {

    @DisplayName("Registra metricas de recriptografia IMAP sem expor segredos nos logs")
    @Test
    void logsReencryptionMetricsWithoutSecrets() {
        EmailCredentialReencryptionService service = mock(EmailCredentialReencryptionService.class);
        EmailCredentialReencryptionJob job = new EmailCredentialReencryptionJob(
                new EmailCredentialEncryptionProperties(
                        "segredo-atual-super-secreto",
                        List.of("segredo-anterior-super-secreto"),
                        true
                ),
                service
        );
        when(service.reencryptAll()).thenReturn(new EmailCredentialReencryptionResult(3, 1, 1, 1));

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        Logger logger = (Logger) LoggerFactory.getLogger(EmailCredentialReencryptionJob.class);
        appender.start();
        logger.addAppender(appender);
        try {
            job.run(null);
        } finally {
            logger.detachAppender(appender);
        }

        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .singleElement()
                .satisfies(message -> {
                    assertThat(message).contains("avaliadas=3", "migradas=1", "ignoradas=1", "falhas=1");
                    assertThat(message).doesNotContain("segredo-atual-super-secreto", "segredo-anterior-super-secreto");
                });
        verify(service).reencryptAll();
    }
}
