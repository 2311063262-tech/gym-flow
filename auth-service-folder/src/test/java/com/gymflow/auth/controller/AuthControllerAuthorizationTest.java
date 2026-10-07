package com.gymflow.auth.controller;

import com.gymflow.auth.config.JwtAuthenticationFilter;
import com.gymflow.auth.config.SecurityConfig;
import com.gymflow.auth.dto.UserStatsDto;
import com.gymflow.auth.repository.RevokedTokenRepository;
import com.gymflow.auth.repository.UserRepository;
import com.gymflow.auth.service.AuthService;
import com.gymflow.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadDashboardStats() throws Exception {
        when(authService.getStats()).thenReturn(new UserStatsDto(9, 5, 2, 1, 8, 1, List.of()));

        mockMvc.perform(get("/api/auth/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(9))
                .andExpect(jsonPath("$.members").value(5))
                .andExpect(jsonPath("$.inactive").value(1));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotReadAdminStatsButCanReadDirectory() throws Exception {
        when(authService.getUsers(anyString(), isNull(), anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/auth/users/stats"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void memberCannotReadDirectory() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousRequestCannotReadDirectory() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
