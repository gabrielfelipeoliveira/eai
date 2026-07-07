package com.eai.application.user;

import com.eai.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    List<User> findAll();

    List<User> findByCompanyId(UUID companyId);

    List<User> findByStoreId(UUID storeId);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    User save(User user);
}
