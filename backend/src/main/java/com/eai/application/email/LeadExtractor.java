package com.eai.application.email;

public interface LeadExtractor {

    ParsedEmailLead extract(EmailMessage message);
}
