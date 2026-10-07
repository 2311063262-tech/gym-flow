package dh13c8.nhom4.auth.service;

import dh13c8.nhom4.auth.dto.AuthResponse;
import dh13c8.nhom4.auth.dto.LoginRequest;
import dh13c8.nhom4.auth.entity.Role;
import dh13c8.nhom4.auth.entity.User;
import dh13c8.nhom4.auth.repository.UserRepository;
import dh13c8.nhom4.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User(1L, "admin", "encoded-password", Role.ADMIN,
                "Admin User", "admin@gym.com", "0123456789");
        loginRequest = new LoginRequest("admin", "password123");
    }

    @Test
    void loginReturnsTokenAndUserWithoutPassword() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("admin")
                .password("encoded-password")
                .roles("ADMIN")
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("admin", response.getUser().getUsername());
        assertEquals("ADMIN", response.getUser().getRole());
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void loginRejectsInvalidCredentialsBeforeLoadingUser() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verifyNoInteractions(userRepository, jwtService, userDetailsService);
    }

    @Test
    void loginFailsWhenAuthenticatedUserIsMissing() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtService, userDetailsService);
    }
}
