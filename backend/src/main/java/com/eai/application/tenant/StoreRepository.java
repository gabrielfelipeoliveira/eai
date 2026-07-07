package com.eai.application.tenant;

import com.eai.domain.tenant.Store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {

    List<Store> findAll();

    List<Store> findByCompanyId(UUID companyId);

    List<Store> findByIdIn(List<UUID> ids);

    Optional<Store> findById(UUID id);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, UUID id);

    Store save(Store store);
}
