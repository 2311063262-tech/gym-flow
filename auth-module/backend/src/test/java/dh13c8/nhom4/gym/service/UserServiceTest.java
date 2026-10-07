package dh13c8.nhom4.auth.service;

import dh13c8.nhom4.auth.dto.UserResponse;
import dh13c8.nhom4.auth.entity.Role;
import dh13c8.nhom4.auth.entity.User;
import dh13c8.nhom4.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User(1L, "admin", "encoded-admin-password", Role.ADMIN,
                "Admin User", "admin@gym.com", "0123456789");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "password"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllUsersRequiresAdminAndReturnsSafeResponses() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(admin));

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals("ADMIN", result.get(0).getRole());
        verify(userRepository).findAll();
    }

    @Test
    void createUserHashesPasswordBeforeSaving() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        User newUser = new User(null, "member1", "plain-password", Role.MEMBER,
                "Member One", "member1@gym.com", "0987654321");
        when(userRepository.existsByUsername("member1")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.createUser(newUser);

        assertEquals("member1", result.getUsername());
        assertEquals("encoded-password", newUser.getPassword());
        verify(passwordEncoder).encode("plain-password");
        verify(userRepository).save(newUser);
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        User duplicate = new User(null, "admin", "password", Role.MEMBER,
                null, null, null);
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(duplicate));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void nonAdminCannotListUsers() {
        User member = new User(2L, "member", "encoded-password", Role.MEMBER,
                "Member", "member@gym.com", null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("member", "password"));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));

        assertThrows(SecurityException.class, () -> userService.getAllUsers());

        verify(userRepository, never()).findAll();
    }
}
