package com.eai.application.tenant;

import com.eai.domain.tenant.Company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository {

    List<Company> findAll();

    Optional<Company> findById(UUID id);

    boolean existsByDocument(String document);

    boolean existsByDocumentAndIdNot(String document, UUID id);

    Company save(Company company);
}
