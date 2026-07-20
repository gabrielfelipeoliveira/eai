package com.eai.infrastructure.email;

import com.eai.application.email.EmailMessage;
import com.eai.application.email.EmailReader;
import com.eai.domain.email.EmailAccount;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Component
public class ImapEmailReader implements EmailReader {

    @Override
    public void testConnection(EmailAccount account, String password) {
        Store store = null;
        try {
            store = openStore(account, password);
        } catch (MessagingException exception) {
            throw new IllegalStateException("Falha ao conectar no IMAP: " + exception.getMessage(), exception);
        } finally {
            close(store);
        }
    }

    @Override
    public List<EmailMessage> readMessages(EmailAccount account, String password, Instant since) {
        Store store = null;
        Folder inbox = null;
        try {
            store = openStore(account, password);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.search(searchTerm(since));
            List<EmailMessage> result = new ArrayList<>();
            for (Message message : messages) {
                result.add(new EmailMessage(
                        message.getSubject(),
                        message.getFrom() == null || message.getFrom().length == 0 ? null : message.getFrom()[0].toString(),
                        extractText(message),
                        receivedAt(message)
                ));
                message.setFlag(Flags.Flag.SEEN, true);
            }
            return result;
        } catch (MessagingException | IOException exception) {
            throw new IllegalStateException("Falha ao ler e-mails via IMAP: " + exception.getMessage(), exception);
        } finally {
            close(inbox);
            close(store);
        }
    }

    private Store openStore(EmailAccount account, String password) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imap");
        properties.put("mail.imap.host", account.getHost());
        properties.put("mail.imap.port", String.valueOf(account.getPort()));
        properties.put("mail.imap.ssl.enable", String.valueOf(account.isUseSsl()));
        properties.put("mail.imap.connectiontimeout", "10000");
        properties.put("mail.imap.timeout", "10000");
        Session session = Session.getInstance(properties);
        Store store = session.getStore("imap");
        store.connect(account.getHost(), account.getPort(), account.getUsername(), password);
        return store;
    }

    private SearchTerm searchTerm(Instant since) {
        FlagTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        if (since == null) {
            return unseen;
        }
        Date receivedSince = Date.from(since.atZone(ZoneId.systemDefault()).toInstant());
        return new AndTerm(unseen, new ReceivedDateTerm(ComparisonTerm.GT, receivedSince));
    }

    private String extractText(Message message) throws MessagingException, IOException {
        Object content = message.getContent();
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof Multipart multipart) {
            return extractText(multipart);
        }
        return "";
    }

    private String extractText(Multipart multipart) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            var part = multipart.getBodyPart(i);
            if (part.isMimeType("text/plain")) {
                return String.valueOf(part.getContent());
            }
            if (part.getContent() instanceof Multipart nested) {
                String text = extractText(nested);
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }

    private Instant receivedAt(Message message) throws MessagingException {
        Date receivedDate = message.getReceivedDate();
        return receivedDate == null ? Instant.now() : receivedDate.toInstant();
    }

    private void close(Folder folder) {
        if (folder == null || !folder.isOpen()) {
            return;
        }
        try {
            folder.close(false);
        } catch (MessagingException ignored) {
        }
    }

    private void close(Store store) {
        if (store == null || !store.isConnected()) {
            return;
        }
        try {
            store.close();
        } catch (MessagingException ignored) {
        }
    }
}
