package dh13c8.nhom4.auth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dh13c8.nhom4.auth.dto.UserResponse;
import dh13c8.nhom4.auth.entity.Role;
import dh13c8.nhom4.auth.entity.User;
import dh13c8.nhom4.auth.repository.UserRepository;

/**
 * Service quản lý User.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Kiểm tra quyền: user hiện tại phải có một trong các role được phép.
     */
    public void checkRole(Role... allowedRoles) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        boolean hasRole = false;
        for (Role role : allowedRoles) {
            if (user.getRole() == role) {
                hasRole = true;
                break;
            }
        }

        if (!hasRole) {
            throw new SecurityException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    /**
     * Lấy danh sách tất cả users (chỉ ADMIN).
     */
    public List<UserResponse> getAllUsers() {
        checkRole(Role.ADMIN);
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy user theo ID (chỉ ADMIN).
     */
    public UserResponse getUserById(Long id) {
        checkRole(Role.ADMIN);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserResponse(user);
    }

    /**
     * Tạo user mới (chỉ ADMIN).
     */
    public UserResponse createUser(User user) {
        checkRole(Role.ADMIN);

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Hash password trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    /**
     * Cập nhật user (chỉ ADMIN).
     */
    public UserResponse updateUser(Long id, User userDetails) {
        checkRole(Role.ADMIN);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }

        User updated = userRepository.save(user);
        return toUserResponse(updated);
    }

    /**
     * Xóa user (chỉ ADMIN).
     */
    public void deleteUser(Long id) {
        checkRole(Role.ADMIN);
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Convert User entity sang UserResponse DTO (không trả password).
     */
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone());
    }
}
