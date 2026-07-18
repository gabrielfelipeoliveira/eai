package com.eai.infrastructure.persistence.lead;

import lombok.Getter;
import lombok.Setter;

import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "leads")
public class LeadJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_city")
    private String customerCity;

    @Column(name = "vehicle_interest")
    private String vehicleInterest;

    @Column(name = "item_id")
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadSource source;

    @Column(name = "original_message", columnDefinition = "TEXT")
    private String originalMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    @Column(name = "assigned_to_user_id")
    private UUID assignedToUserId;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "first_contact_at")
    private Instant firstContactAt;

    @Column(name = "last_contact_at")
    private Instant lastContactAt;

    @Column(name = "lost_reason")
    private String lostReason;

    @Column(name = "sale_value", precision = 14, scale = 2)
    private BigDecimal saleValue;

    @Column(name = "sale_currency", nullable = false)
    private String saleCurrency;

}
