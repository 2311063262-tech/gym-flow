package dh13c8.nhom4.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dh13c8.nhom4.auth.dto.AuthResponse;
import dh13c8.nhom4.auth.dto.LoginRequest;
import dh13c8.nhom4.auth.dto.UserResponse;
import dh13c8.nhom4.auth.entity.User;
import dh13c8.nhom4.auth.repository.UserRepository;
import dh13c8.nhom4.auth.security.JwtService;

/**
 * Service xử lý authentication.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Đăng nhập: xác thực username/password và trả về JWT token.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Load user từ database trước để kiểm tra
        User user = userRepository.findByUsernameAndNotDeleted(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng"));

        // Kiểm tra tài khoản đã bị xóa mềm chưa
        if (user.getDeletedAt() != null) {
            throw new DisabledException("Tài khoản đã bị xóa");
        }

        // Kiểm tra tài khoản có đang active không
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new DisabledException("Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên");
        }

        // Authenticate với Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Cập nhật lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Tạo JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        // Tạo UserResponse (không trả password)
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone());

        return new AuthResponse(token, userResponse);
    }
}
