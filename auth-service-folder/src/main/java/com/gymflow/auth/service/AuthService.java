package com.gymflow.auth.service;

import com.gymflow.auth.dto.*;
import com.gymflow.auth.entity.RevokedToken;
import com.gymflow.auth.entity.User;
import com.gymflow.auth.exception.ApiException;
import com.gymflow.auth.repository.RevokedTokenRepository;
import com.gymflow.auth.repository.UserRepository;
import com.gymflow.auth.security.JwtPrincipal;
import com.gymflow.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        ensureUnique(request.getUsername(), request.getEmail(), null);
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.MEMBER);
        user.setFullName(trimToNull(request.getFullName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setActive(true);

        User saved = userRepository.save(user);
        return toAuthResponse(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .filter(User::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
        }

        user.setLastLoginAt(LocalDateTime.now());
        return toAuthResponse(userRepository.save(user));
    }

    @Transactional
    public void logout(JwtPrincipal principal) {
        if (principal != null && !revokedTokenRepository.existsById(principal.tokenId())) {
            revokedTokenRepository.save(new RevokedToken(principal.tokenId(), principal.expiresAt()));
        }
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(JwtPrincipal principal) {
        return UserDto.from(findActiveUser(principal.userId()));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(
            String search,
            String role,
            String status,
            int page,
            int size,
            String sort
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        String sortField = switch (sort == null ? "createdAt" : sort) {
            case "username", "email", "role", "createdAt", "lastLoginAt" -> sort;
            default -> "createdAt";
        };
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, sortField));

        Specification<User> specification = (root, query, builder) ->
                builder.isNull(root.get("deletedAt"));
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) ->
                    builder.or(
                            builder.like(builder.lower(root.get("username")), pattern),
                            builder.like(builder.lower(root.get("email")), pattern)
                    ));
        }
        if (role != null && !role.isBlank()) {
            User.Role parsedRole = parseRole(role);
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("role"), parsedRole));
        }
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            boolean active = switch (status.toLowerCase(Locale.ROOT)) {
                case "active" -> true;
                case "inactive" -> false;
                default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Status must be active, inactive, or all");
            };
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("isActive"), active));
        }
        return userRepository.findAll(specification, pageable).map(UserDto::from);
    }

    @Transactional(readOnly = true)
    public UserStatsDto getStats() {
        List<UserDto> recentUsers = userRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(UserDto::from)
                .toList();
        return new UserStatsDto(
                userRepository.countByDeletedAtIsNull(),
                userRepository.countByRoleAndDeletedAtIsNull(User.Role.MEMBER),
                userRepository.countByRoleAndDeletedAtIsNull(User.Role.TRAINER),
                userRepository.countByRoleAndDeletedAtIsNull(User.Role.STAFF),
                userRepository.countActiveState(true),
                userRepository.countActiveState(false),
                recentUsers
        );
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id, JwtPrincipal caller) {
        if (caller.role() != User.Role.ADMIN && caller.role() != User.Role.STAFF && !caller.userId().equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You can only view your own profile");
        }
        return UserDto.from(findActiveUser(id));
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        User.Role role = parseRole(request.getRole());
        if (role != User.Role.TRAINER && role != User.Role.MEMBER) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Admin can create TRAINER or MEMBER accounts only");
        }
        ensureUnique(request.getUsername(), request.getEmail(), null);

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setFullName(trimToNull(request.getFullName()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setActive(true);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request, JwtPrincipal caller) {
        User user = findActiveUser(id);
        boolean isSelf = caller.userId().equals(id);
        boolean isAdmin = caller.role() == User.Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You can only update your own profile");
        }
        if (isAdmin && !isSelf && user.getRole() != User.Role.TRAINER && user.getRole() != User.Role.MEMBER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only TRAINER and MEMBER accounts can be managed here");
        }
        if (!isAdmin && request.getRole() != null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ROLE_CHANGE_FORBIDDEN", "You cannot change your role");
        }
        if (isSelf && request.getRole() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "SELF_ROLE_CHANGE", "You cannot change your own role");
        }

        ensureUnique(
                request.getUsername() == null ? user.getUsername() : request.getUsername(),
                request.getEmail() == null ? user.getEmail() : request.getEmail(),
                id
        );
        if (request.getUsername() != null) user.setUsername(request.getUsername().trim());
        if (request.getEmail() != null) user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFullName() != null) user.setFullName(trimToNull(request.getFullName()));
        if (request.getPhone() != null) user.setPhone(trimToNull(request.getPhone()));
        if (request.getRole() != null) {
            User.Role role = parseRole(request.getRole());
            if (role != User.Role.TRAINER && role != User.Role.MEMBER) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Admin cannot assign ADMIN or STAFF through user editing");
            }
            user.setRole(role);
        }
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto setUserStatus(Long id, boolean active, JwtPrincipal caller) {
        User user = findActiveUser(id);
        if (caller.userId().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "SELF_STATUS_CHANGE", "You cannot disable your own account");
        }
        requireManageableRole(user);
        user.setActive(active);
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public void softDeleteUser(Long id, JwtPrincipal caller) {
        User user = findActiveUser(id);
        if (caller.userId().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "SELF_DELETE", "You cannot delete your own account");
        }
        requireManageableRole(user);
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private User findActiveUser(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }

    private void requireManageableRole(User user) {
        if (user.getRole() != User.Role.TRAINER && user.getRole() != User.Role.MEMBER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "USER_NOT_MANAGEABLE", "Only TRAINER and MEMBER accounts can be changed");
        }
    }

    private void ensureUnique(String username, String email, Long exceptId) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        boolean usernameExists = userRepository.findByUsername(normalizedUsername)
                .filter(user -> !user.getId().equals(exceptId))
                .isPresent();
        boolean emailExists = userRepository.findByEmail(normalizedEmail)
                .filter(user -> !user.getId().equals(exceptId))
                .isPresent();
        if (usernameExists || emailExists) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_USER", usernameExists
                    ? "Username already exists" : "Email already exists");
        }
    }

    private User.Role parseRole(String role) {
        try {
            return User.Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE",
                    "Role must be one of " + Arrays.toString(User.Role.values()));
        }
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
