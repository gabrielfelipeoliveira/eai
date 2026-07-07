package com.eai.application.user;

import com.eai.application.common.ConflictException;
import com.eai.application.common.NotFoundException;
import com.eai.application.security.PasswordHasher;
import com.eai.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
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
        User user = User.create(
                command.name(),
                email,
                passwordHasher.hash(command.password()),
                command.phone(),
                command.jobTitle(),
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
        user.updateProfile(command.name(), email, command.phone(), command.jobTitle(), command.roles());
        if (command.password() != null && !command.password().isBlank()) {
            user.updatePasswordHash(passwordHasher.hash(command.password()));
        }
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
}
