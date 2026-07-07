package com.eai.api.user;

import com.eai.application.user.CreateUserCommand;
import com.eai.application.user.UpdateUserCommand;
import com.eai.application.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<UserResponse> listUsers() {
        return userService.listUsers().stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserResponse getUser(@PathVariable UUID id) {
        return UserResponse.fromDomain(userService.getUser(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return UserResponse.fromDomain(userService.createUser(new CreateUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.phone(),
                request.jobTitle(),
                request.roles()
        )));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return UserResponse.fromDomain(userService.updateUser(id, new UpdateUserCommand(
                request.name(),
                request.email(),
                request.password(),
                request.phone(),
                request.jobTitle(),
                request.roles()
        )));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse activateUser(@PathVariable UUID id) {
        return UserResponse.fromDomain(userService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deactivateUser(@PathVariable UUID id) {
        return UserResponse.fromDomain(userService.deactivateUser(id));
    }
}
