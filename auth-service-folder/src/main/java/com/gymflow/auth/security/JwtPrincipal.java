package com.gymflow.auth.security;

import com.gymflow.auth.entity.User;

import java.time.LocalDateTime;

public record JwtPrincipal(Long userId, String username, User.Role role, String tokenId, LocalDateTime expiresAt) {
}
