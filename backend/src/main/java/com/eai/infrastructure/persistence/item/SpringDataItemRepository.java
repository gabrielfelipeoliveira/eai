package com.eai.infrastructure.persistence.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataItemRepository extends JpaRepository<ItemJpaEntity, UUID> {
}
