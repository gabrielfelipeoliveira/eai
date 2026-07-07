package com.eai.application.email;

public interface EmailParser {

    boolean supports(EmailMessage message);

    ParsedEmailLead parse(EmailMessage message);
}
