package dh13c8.nhom4.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dh13c8.nhom4.auth.dto.AccountResponse;
import dh13c8.nhom4.auth.dto.CreateAccountRequest;
import dh13c8.nhom4.auth.dto.UpdateAccountRequest;
import dh13c8.nhom4.auth.entity.Role;
import dh13c8.nhom4.auth.entity.User;
import dh13c8.nhom4.auth.repository.UserRepository;

/**
 * Service quản lý tài khoản cho ADMIN.
 */
@Service
public class AdminAccountService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Kiểm tra quyền ADMIN và lấy user hiện tại.
     */
    private User getCurrentAdmin() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new SecurityException("User not authenticated"));

        if (currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Chỉ ADMIN mới có quyền thực hiện thao tác này");
        }

        return currentUser;
    }

    /**
     * Lấy danh sách tất cả tài khoản chưa bị xóa mềm.
     */
    public List<AccountResponse> getAllAccounts() {
        getCurrentAdmin();
        return userRepository.findAllActive().stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và lọc tài khoản.
     * 
     * @param search   - Tìm theo username hoặc email (optional)
     * @param role     - Lọc theo role (optional): TRAINER hoặc MEMBER
     * @param isActive - Lọc theo trạng thái (optional): true/false
     */
    public List<AccountResponse> searchAndFilterAccounts(String search, String role, Boolean isActive) {
        getCurrentAdmin();

        Role roleEnum = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Role không hợp lệ. Chỉ chấp nhận TRAINER hoặc MEMBER");
            }
        }

        List<User> users = userRepository.findByFilters(search, roleEnum, isActive);
        return users.stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xem chi tiết một tài khoản.
     */
    public AccountResponse getAccountById(Long id) {
        getCurrentAdmin();
        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + id));
        return toAccountResponse(user);
    }

    /**
     * Tạo tài khoản mới (chỉ TRAINER hoặc MEMBER).
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        getCurrentAdmin();

        // Validate role: chỉ cho phép tạo TRAINER hoặc MEMBER
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
            if (role == Role.ADMIN) {
                throw new IllegalArgumentException("Không được phép tạo tài khoản ADMIN");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role không hợp lệ. Chỉ chấp nhận TRAINER hoặc MEMBER");
        }

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại: " + request.getUsername());
        }

        // Kiểm tra email đã tồn tại
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
            && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
        }

        // Tạo user mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password với BCrypt
        user.setRole(role);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(true); // Mặc định kích hoạt

        User savedUser = userRepository.save(user);
        return toAccountResponse(savedUser);
    }

    /**
     * Cập nhật thông tin tài khoản (không bao gồm password).
     */
    @Transactional
    public AccountResponse updateAccount(Long id, UpdateAccountRequest request) {
        User currentAdmin = getCurrentAdmin();

        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + id));

        // Không cho phép admin tự cập nhật tài khoản của mình để tránh khóa tài khoản
        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Không thể cập nhật tài khoản của chính mình qua chức năng này");
        }

        // Cập nhật username nếu có
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername()) 
                && userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        // Cập nhật email nếu có
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail()) 
                && userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật role nếu có (chỉ cho phép TRAINER hoặc MEMBER)
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role newRole;
            try {
                newRole = Role.valueOf(request.getRole().toUpperCase());
                if (newRole == Role.ADMIN) {
                    throw new IllegalArgumentException("Không được phép thay đổi role thành ADMIN");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Role không hợp lệ. Chỉ chấp nhận TRAINER hoặc MEMBER");
            }
            user.setRole(newRole);
        }

        // Cập nhật các trường khác
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        User updatedUser = userRepository.save(user);
        return toAccountResponse(updatedUser);
    }

    /**
     * Kích hoạt tài khoản.
     */
    @Transactional
    public AccountResponse activateAccount(Long id) {
        User currentAdmin = getCurrentAdmin();

        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + id));

        // Không cho phép admin tự vô hiệu hóa tài khoản của mình
        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Không thể kích hoạt/vô hiệu hóa tài khoản của chính mình");
        }

        user.setIsActive(true);
        User updatedUser = userRepository.save(user);
        return toAccountResponse(updatedUser);
    }

    /**
     * Vô hiệu hóa tài khoản (tài khoản không thể đăng nhập).
     */
    @Transactional
    public AccountResponse deactivateAccount(Long id) {
        User currentAdmin = getCurrentAdmin();

        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + id));

        // Không cho phép admin tự vô hiệu hóa tài khoản của mình
        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Không thể kích hoạt/vô hiệu hóa tài khoản của chính mình");
        }

        // Không cho vô hiệu hóa tài khoản ADMIN khác
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Không được phép vô hiệu hóa tài khoản ADMIN");
        }

        user.setIsActive(false);
        User updatedUser = userRepository.save(user);
        return toAccountResponse(updatedUser);
    }

    /**
     * Xóa mềm tài khoản (set deleted_at).
     */
    @Transactional
    public void softDeleteAccount(Long id) {
        User currentAdmin = getCurrentAdmin();

        User user = userRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với ID: " + id));

        // Không cho phép admin tự xóa tài khoản của mình
        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Không thể xóa tài khoản của chính mình");
        }

        // Không cho xóa tài khoản ADMIN khác
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Không được phép xóa tài khoản ADMIN");
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setIsActive(false); // Đồng thời vô hiệu hóa
        userRepository.save(user);
    }

    /**
     * Convert User entity sang AccountResponse DTO.
     */
    private AccountResponse toAccountResponse(User user) {
        return new AccountResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName(),
                user.getPhone(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
