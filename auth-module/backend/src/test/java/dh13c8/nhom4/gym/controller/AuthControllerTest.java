package dh13c8.nhom4.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dh13c8.nhom4.auth.dto.AuthResponse;
import dh13c8.nhom4.auth.dto.LoginRequest;
import dh13c8.nhom4.auth.dto.UserResponse;
import dh13c8.nhom4.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void loginReturnsTokenAndUserDetails() throws Exception {
        LoginRequest request = new LoginRequest("admin", "password123");
        UserResponse user = new UserResponse(
                1L, "admin", "ADMIN", "Admin User", "admin@gym.com", "0123456789");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("mock-jwt-token", user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.user.email").value("admin@gym.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrong-password");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest());
    }
}
