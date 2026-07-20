package com.eai.infrastructure.persistence.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataEmailImportHistoryRepository extends JpaRepository<EmailImportHistoryJpaEntity, UUID> {
}
