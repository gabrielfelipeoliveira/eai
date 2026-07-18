package com.eai.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eai.domain.user.UserStatus;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    List<UserJpaEntity> findByCompanyId(UUID companyId);

    List<UserJpaEntity> findByStoreId(UUID storeId);

    boolean existsByCompanyIdAndStatus(UUID companyId, UserStatus status);

    List<UserJpaEntity> findByStoreIdAndStatus(UUID storeId, UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
