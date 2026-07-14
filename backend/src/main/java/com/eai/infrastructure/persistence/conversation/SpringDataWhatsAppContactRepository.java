package com.eai.infrastructure.persistence.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataWhatsAppContactRepository extends JpaRepository<WhatsAppContactJpaEntity, UUID> {

    Optional<WhatsAppContactJpaEntity> findByStoreIdAndPhone(UUID storeId, String phone);
}
