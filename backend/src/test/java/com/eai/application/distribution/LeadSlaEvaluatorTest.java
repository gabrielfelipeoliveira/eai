package com.eai.application.distribution;

import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeadSlaEvaluatorTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SELLER_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    private final LeadSlaEvaluator evaluator = new LeadSlaEvaluator();

    @Test
    void identifiesLeadOverdueToAssign() {
        Instant createdAt = Instant.parse("2026-07-07T10:00:00Z");
        Lead lead = lead(LeadStatus.AVAILABLE, null, null, createdAt, null);
        LeadSlaPolicy policy = LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, true);

        assertThat(evaluator.isOverdueToAssign(lead, policy, Instant.parse("2026-07-07T10:16:00Z"))).isTrue();
    }

    @Test
    void identifiesLeadOverdueToFirstContact() {
        Instant createdAt = Instant.parse("2026-07-07T10:00:00Z");
        Instant assignedAt = Instant.parse("2026-07-07T10:05:00Z");
        Lead lead = lead(LeadStatus.ASSIGNED, SELLER_ID, assignedAt, createdAt, null);
        LeadSlaPolicy policy = LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, true);

        assertThat(evaluator.isOverdueToFirstContact(lead, policy, Instant.parse("2026-07-07T10:36:00Z"))).isTrue();
    }

    @Test
    void doesNotMarkFirstContactOverdueAfterFirstContact() {
        Instant createdAt = Instant.parse("2026-07-07T10:00:00Z");
        Instant assignedAt = Instant.parse("2026-07-07T10:05:00Z");
        Lead lead = lead(LeadStatus.FIRST_CONTACT, SELLER_ID, assignedAt, createdAt, Instant.parse("2026-07-07T10:20:00Z"));
        LeadSlaPolicy policy = LeadSlaPolicy.create(COMPANY_ID, STORE_ID, 15, 30, true);

        assertThat(evaluator.isOverdueToFirstContact(lead, policy, Instant.parse("2026-07-07T11:00:00Z"))).isFalse();
    }

    private Lead lead(LeadStatus status, UUID assignedToUserId, Instant assignedAt, Instant createdAt, Instant firstContactAt) {
        return new Lead(
                UUID.randomUUID(),
                COMPANY_ID,
                STORE_ID,
                "Cliente",
                "11999990000",
                null,
                null,
                "Civic",
                LeadSource.MANUAL,
                null,
                status,
                assignedToUserId,
                assignedAt,
                createdAt,
                createdAt,
                firstContactAt,
                firstContactAt,
                null,
                null
        );
    }
}
