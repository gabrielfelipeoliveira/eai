package com.eai.infrastructure.persistence.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCompanyRepository extends JpaRepository<CompanyJpaEntity, UUID> {
}
