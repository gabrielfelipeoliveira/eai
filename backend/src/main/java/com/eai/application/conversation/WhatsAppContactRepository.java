package com.eai.application.conversation;

import com.eai.domain.conversation.WhatsAppContact;

import java.util.Optional;
import java.util.UUID;

public interface WhatsAppContactRepository {

    Optional<WhatsAppContact> findById(UUID id);

    Optional<WhatsAppContact> findByStoreIdAndPhone(UUID storeId, String phone);

    WhatsAppContact save(WhatsAppContact contact);
}
