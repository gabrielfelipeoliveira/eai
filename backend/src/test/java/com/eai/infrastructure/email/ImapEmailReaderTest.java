package com.eai.infrastructure.email;

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImapEmailReaderTest {

    private final ImapEmailReader reader = new ImapEmailReader();

    @DisplayName("Extrai texto direto de mensagem simples")
    @Test
    void extractsTextFromSimpleMessage() throws Exception {
        MimeMessage message = message();
        message.setText("Corpo simples");

        assertThat(extractText(message)).isEqualTo("Corpo simples");
    }

    @DisplayName("Extrai primeira parte text plain de multipart")
    @Test
    void extractsTextPlainFromMultipart() throws Exception {
        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart plain = new MimeBodyPart();
        plain.setText("Texto plain");
        MimeBodyPart html = new MimeBodyPart();
        html.setContent("<p>HTML</p>", "text/html");
        multipart.addBodyPart(plain);
        multipart.addBodyPart(html);

        assertThat(extractText(multipart)).isEqualTo("Texto plain");
    }

    @DisplayName("Extrai texto de multipart aninhado")
    @Test
    void extractsTextFromNestedMultipart() throws Exception {
        Multipart root = mock(Multipart.class);
        BodyPart wrapper = mock(BodyPart.class);
        Multipart nested = mock(Multipart.class);
        BodyPart nestedPlain = mock(BodyPart.class);
        when(root.getCount()).thenReturn(1);
        when(root.getBodyPart(0)).thenReturn(wrapper);
        when(wrapper.isMimeType("text/plain")).thenReturn(false);
        when(wrapper.getContent()).thenReturn(nested);
        when(nested.getCount()).thenReturn(1);
        when(nested.getBodyPart(0)).thenReturn(nestedPlain);
        when(nestedPlain.isMimeType("text/plain")).thenReturn(true);
        when(nestedPlain.getContent()).thenReturn("Texto aninhado");

        assertThat(extractText(root)).isEqualTo("Texto aninhado");
    }

    @DisplayName("Retorna vazio quando multipart nao possui texto")
    @Test
    void returnsEmptyWhenMultipartHasNoText() throws Exception {
        Multipart multipart = mock(Multipart.class);
        BodyPart attachment = mock(BodyPart.class);
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(attachment);
        when(attachment.isMimeType("text/plain")).thenReturn(false);
        when(attachment.getContent()).thenReturn(new Object());

        assertThat(extractText(multipart)).isEmpty();
    }

    @DisplayName("Converte received date para instant e usa agora quando ausente")
    @Test
    void receivedAtUsesMessageDateOrNowFallback() throws Exception {
        Instant received = Instant.parse("2026-07-27T10:00:00Z");
        MimeMessage withDate = new ReceivedDateMessage(Date.from(received));

        MimeMessage withoutDate = message();
        Instant before = Instant.now();
        Instant fallback = receivedAt(withoutDate);
        Instant after = Instant.now();

        assertThat(receivedAt(withDate)).isEqualTo(received);
        assertThat(fallback).isBetween(before, after);
    }

    @DisplayName("Extrai texto de mensagem multipart completa")
    @Test
    void extractsTextFromMultipartMessage() throws Exception {
        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart plain = new MimeBodyPart();
        plain.setText("Mensagem multipart");
        multipart.addBodyPart(plain);
        MimeMessage message = message();
        message.setContent(multipart);
        message.saveChanges();

        assertThat(extractText(message)).isEqualTo("Mensagem multipart");
    }

    @DisplayName("Monta termo de busca apenas para nao lidos quando data inicial nao foi informada")
    @Test
    void searchTermWithoutSinceUsesOnlyUnseenFlag() throws Exception {
        SearchTerm term = searchTerm(null);

        assertThat(term).isInstanceOf(FlagTerm.class);
    }

    @DisplayName("Monta termo de busca combinando nao lidos e data recebida")
    @Test
    void searchTermWithSinceCombinesUnseenAndReceivedDate() throws Exception {
        SearchTerm term = searchTerm(Instant.parse("2026-07-07T10:00:00Z"));

        assertThat(term).isInstanceOf(AndTerm.class);
        AndTerm andTerm = (AndTerm) term;
        assertThat(andTerm.getTerms()).hasAtLeastOneElementOfType(FlagTerm.class);
        assertThat(andTerm.getTerms()).hasAtLeastOneElementOfType(ReceivedDateTerm.class);
    }

    @DisplayName("Fecha folder e store somente quando estao abertos")
    @Test
    void closeOnlyClosesOpenResources() throws Exception {
        Folder openFolder = mock(Folder.class);
        Folder closedFolder = mock(Folder.class);
        Store connectedStore = mock(Store.class);
        Store disconnectedStore = mock(Store.class);
        when(openFolder.isOpen()).thenReturn(true);
        when(closedFolder.isOpen()).thenReturn(false);
        when(connectedStore.isConnected()).thenReturn(true);
        when(disconnectedStore.isConnected()).thenReturn(false);

        close(openFolder);
        close(closedFolder);
        close(connectedStore);
        close(disconnectedStore);

        verify(openFolder).close(false);
        verify(closedFolder, never()).close(false);
        verify(connectedStore).close();
        verify(disconnectedStore, never()).close();
    }

    private MimeMessage message() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private static class ReceivedDateMessage extends MimeMessage {

        private final Date receivedDate;

        ReceivedDateMessage(Date receivedDate) {
            super(Session.getInstance(new Properties()));
            this.receivedDate = receivedDate;
        }

        @Override
        public Date getReceivedDate() throws MessagingException {
            return receivedDate;
        }
    }

    private String extractText(Message message) throws Exception {
        Method method = ImapEmailReader.class.getDeclaredMethod("extractText", Message.class);
        method.setAccessible(true);
        return (String) method.invoke(reader, message);
    }

    private String extractText(Multipart multipart) throws Exception {
        Method method = ImapEmailReader.class.getDeclaredMethod("extractText", Multipart.class);
        method.setAccessible(true);
        return (String) method.invoke(reader, multipart);
    }

    private Instant receivedAt(Message message) throws Exception {
        Method method = ImapEmailReader.class.getDeclaredMethod("receivedAt", Message.class);
        method.setAccessible(true);
        return (Instant) method.invoke(reader, message);
    }

    private SearchTerm searchTerm(Instant since) throws Exception {
        Method method = ImapEmailReader.class.getDeclaredMethod("searchTerm", Instant.class);
        method.setAccessible(true);
        return (SearchTerm) method.invoke(reader, since);
    }

    private void close(Folder folder) throws Exception {
        Method method = ImapEmailReader.class.getDeclaredMethod("close", Folder.class);
        method.setAccessible(true);
        method.invoke(reader, folder);
    }

    private void close(Store store) throws Exception {
        Method method = ImapEmailReader.class.getDeclaredMethod("close", Store.class);
        method.setAccessible(true);
        method.invoke(reader, store);
    }
}
