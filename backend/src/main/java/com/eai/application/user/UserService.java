package com.eai.application.user;

import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.tenant.CompanyService;
import com.eai.application.tenant.StoreService;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.application.security.PasswordHasher;
import com.eai.domain.user.User;
import com.eai.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final CompanyService companyService;
    private final StoreService storeService;

    public UserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            CompanyService companyService,
            StoreService storeService
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.companyService = companyService;
        this.storeService = storeService;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers(AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return userRepository.findAll();
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER)) {
            if (authenticatedUser.storeId() != null) {
                return userRepository.findByStoreId(authenticatedUser.storeId());
            }
            return userRepository.findByCompanyId(requireCompany(authenticatedUser));
        }
        return userRepository.findByStoreId(requireStore(authenticatedUser));
    }

    @Transactional(readOnly = true)
    public User getUser(UUID id) {
        return findRequired(id);
    }

    @Transactional(readOnly = true)
    public User getUser(UUID id, AuthenticatedUser authenticatedUser) {
        User user = findRequired(id);
        assertCanAccessUser(user, authenticatedUser);
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User createUser(CreateUserCommand command) {
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        validateTenant(command.companyId(), command.storeId(), command.roles());
        User user = User.create(
                command.name(),
                email,
                passwordHasher.hash(command.password()),
                command.phone(),
                command.jobTitle(),
                command.companyId(),
                command.storeId(),
                command.roles()
        );
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserCommand command) {
        User user = getUser(id);
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("Email already registered");
        }
        validateTenant(command.companyId(), command.storeId(), command.roles());
        user.updateProfile(command.name(), email, command.phone(), command.jobTitle(), command.companyId(), command.storeId(), command.roles());
        if (command.password() != null && !command.password().isBlank()) {
            user.updatePasswordHash(passwordHasher.hash(command.password()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public User assignTenant(UUID id, AssignUserTenantCommand command) {
        User user = findRequired(id);
        validateTenant(command.companyId(), command.storeId(), user.getRoles());
        user.updateTenant(command.companyId(), command.storeId());
        return userRepository.save(user);
    }

    @Transactional
    public User activateUser(UUID id) {
        User user = getUser(id);
        user.activate();
        return userRepository.save(user);
    }

    @Transactional
    public User deactivateUser(UUID id) {
        User user = getUser(id);
        user.deactivate();
        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        return email.trim().toLowerCase();
    }

    private User findRequired(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateTenant(UUID companyId, UUID storeId, java.util.Set<UserRole> roles) {
        if (roles == null || roles.size() != 1) {
            throw new IllegalArgumentException("User must have exactly one role");
        }
        UserRole role = roles.iterator().next();
        if (role == UserRole.ADMIN) {
            if (storeId != null) {
                throw new IllegalArgumentException("ADMIN users must not be linked to a store");
            }
            if (companyId != null) {
                assertActiveCompany(companyId);
            }
            return;
        }
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required for role " + role);
        }
        Company company = assertActiveCompany(companyId);
        if (role == UserRole.MANAGER) {
            if (storeId != null) {
                throw new IllegalArgumentException("MANAGER users must not be linked to a store");
            }
            return;
        }
        if (storeId == null) {
            throw new IllegalArgumentException("storeId is required for role " + role);
        }
        Store store = storeService.findRequired(storeId);
        if (!store.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("store does not belong to company");
        }
        if (!company.isActive() || !store.isActive()) {
            throw new IllegalArgumentException("company and store must be active");
        }
    }

    private Company assertActiveCompany(UUID companyId) {
        Company company = companyService.findRequired(companyId);
        if (!company.isActive()) {
            throw new IllegalArgumentException("company must be active");
        }
        return company;
    }

    private void assertCanAccessUser(User user, AuthenticatedUser authenticatedUser) {
        if (hasRole(authenticatedUser, UserRole.ADMIN)) {
            return;
        }
        if (hasRole(authenticatedUser, UserRole.MANAGER)
                && user.getCompanyId() != null
                && user.getCompanyId().equals(requireCompany(authenticatedUser))) {
            if (authenticatedUser.storeId() == null || authenticatedUser.storeId().equals(user.getStoreId())) {
                return;
            }
        }
        if (user.getStoreId() != null && user.getStoreId().equals(requireStore(authenticatedUser))) {
            return;
        }
        throw new ForbiddenException("Access denied for user");
    }

    private UUID requireCompany(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.companyId() == null) {
            throw new ForbiddenException("User is not linked to a company");
        }
        return authenticatedUser.companyId();
    }

    private UUID requireStore(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.storeId() == null) {
            throw new ForbiddenException("User is not linked to a store");
        }
        return authenticatedUser.storeId();
    }

    private boolean hasRole(AuthenticatedUser authenticatedUser, UserRole role) {
        return authenticatedUser.roles().contains(role);
    }
}
