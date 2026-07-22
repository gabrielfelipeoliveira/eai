package com.eai.infrastructure.persistence.lead;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lead_tags")
public class LeadTagJpaEntity {

    @Id
    private UUID id;

    @Column(name = "lead_id", nullable = false)
    private UUID leadId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

}
