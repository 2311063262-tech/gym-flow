package dh13c8.nhom4.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dh13c8.nhom4.auth.dto.AuthResponse;
import dh13c8.nhom4.auth.dto.LoginRequest;
import dh13c8.nhom4.auth.service.AuthService;

/**
 * Controller xử lý authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Đăng nhập: POST /api/auth/login
     * Trả về JWT token và thông tin user (không có password).
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Sai tài khoản hoặc mật khẩu!");
        }
    }
}
