package com.gymflow.auth.service;

import com.gymflow.auth.dto.CreateUserRequest;
import com.gymflow.auth.dto.RegisterRequest;
import com.gymflow.auth.entity.RevokedToken;
import com.gymflow.auth.entity.User;
import com.gymflow.auth.exception.ApiException;
import com.gymflow.auth.repository.RevokedTokenRepository;
import com.gymflow.auth.repository.UserRepository;
import com.gymflow.auth.security.JwtPrincipal;
import com.gymflow.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository userRepository;
    private RevokedTokenRepository revokedTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        revokedTokenRepository = mock(RevokedTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtil = mock(JwtUtil.class);
        authService = new AuthService(userRepository, revokedTokenRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void publicRegistrationAlwaysCreatesMemberAndStoresBcryptPassword() {
        when(userRepository.findByUsername("new-member")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });
        when(jwtUtil.generateToken(7L, "new-member", "new@example.com", "MEMBER")).thenReturn("signed-token");
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new-member");
        request.setEmail("NEW@example.com");
        request.setPassword("secret123");

        var result = authService.register(request);

        assertEquals("MEMBER", result.getRole());
        assertEquals("signed-token", result.getToken());
        verify(userRepository).save(argThat(user ->
                user.getRole() == User.Role.MEMBER
                        && passwordEncoder.matches("secret123", user.getPassword())
                        && !user.getPassword().equals("secret123")));
    }

    @Test
    void registrationRejectsUsernameReservedBySoftDeletedAccount() {
        User deletedUser = new User();
        deletedUser.setId(4L);
        deletedUser.setDeletedAt(LocalDateTime.now());
        when(userRepository.findByUsername("reserved-name")).thenReturn(Optional.of(deletedUser));
        RegisterRequest request = new RegisterRequest();
        request.setUsername("reserved-name");
        request.setEmail("new@example.com");
        request.setPassword("secret123");

        ApiException exception = assertThrows(ApiException.class, () -> authService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminCrudCannotCreateAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("another-admin");
        request.setEmail("another-admin@example.com");
        request.setPassword("secret123");
        request.setRole("ADMIN");

        ApiException exception = assertThrows(ApiException.class, () -> authService.createUser(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(userRepository);
    }

    @Test
    void inactiveAccountCannotLogIn() {
        User user = new User();
        user.setEmail("disabled@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setActive(false);
        when(userRepository.findByEmailAndDeletedAtIsNull("disabled@example.com")).thenReturn(Optional.of(user));

        ApiException exception = assertThrows(ApiException.class, () -> {
            var request = new com.gymflow.auth.dto.LoginRequest();
            request.setEmail("disabled@example.com");
            request.setPassword("secret123");
            authService.login(request);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void userCannotReadAnotherUsersProfile() {
        JwtPrincipal member = new JwtPrincipal(2L, "member", User.Role.MEMBER, "token-id", LocalDateTime.now().plusHours(1));

        ApiException exception = assertThrows(ApiException.class, () -> authService.getUserById(3L, member));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(userRepository);
    }

    @Test
    void adminCannotSoftDeleteOwnAccount() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(User.Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        JwtPrincipal principal = new JwtPrincipal(1L, "admin", User.Role.ADMIN, "token-id", LocalDateTime.now().plusHours(1));

        ApiException exception = assertThrows(ApiException.class, () -> authService.softDeleteUser(1L, principal));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any());
    }

    @Test
    void logoutRevokesJtiInsteadOfStoringJwt() {
        String tokenId = "token-id";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        when(revokedTokenRepository.existsById(tokenId)).thenReturn(false);
        JwtPrincipal principal = new JwtPrincipal(1L, "admin", User.Role.ADMIN, tokenId, expiresAt);

        authService.logout(principal);

        verify(revokedTokenRepository).save(new RevokedToken(tokenId, expiresAt));
    }
}
