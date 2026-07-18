package com.eai.infrastructure.persistence.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "name")
    private String name;

    @Column(name = "\"year\"")
    private Integer year;

    @Column(name = "model")
    private String model;

    @Column(name = "\"value\"", precision = 14, scale = 2)
    private BigDecimal value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
