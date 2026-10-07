package com.gymflow.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {
    @Test
    void generatesTemporarySigningKeyWhenSecretIsNotConfigured() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        jwtUtil.initializeSecret();
        String token = jwtUtil.generateToken(17L, "admin", "admin@example.com", "ADMIN");

        assertEquals("17", jwtUtil.parseToken(token).getSubject());
        assertNotNull(jwtUtil.parseToken(token).getId());
    }

    @Test
    void rejectsConfiguredSigningSecretsShorterThan32Bytes() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "too-short");

        assertThrows(IllegalStateException.class, jwtUtil::initializeSecret);
    }
}
