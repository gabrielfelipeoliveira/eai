package com.eai.application.user;

import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    List<User> findAll();

    List<User> findByCompanyId(UUID companyId);

    List<User> findByStoreId(UUID storeId);

    boolean existsActiveByCompanyId(UUID companyId);

    List<User> findActiveByStoreId(UUID storeId);

    List<User> findActiveByRole(UserRole role);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    User save(User user);
}
