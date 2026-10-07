package dh13c8.nhom4.auth.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import dh13c8.nhom4.auth.dto.AccountResponse;
import dh13c8.nhom4.auth.dto.CreateAccountRequest;
import dh13c8.nhom4.auth.dto.UpdateAccountRequest;
import dh13c8.nhom4.auth.service.AdminAccountService;
import jakarta.validation.Valid;

/**
 * REST Controller cho quản lý tài khoản (dành cho ADMIN).
 */
@RestController
@RequestMapping("/api/admin/accounts")
@CrossOrigin(origins = {"http://localhost:5501", "http://127.0.0.1:5501"})
public class AdminAccountController {

    @Autowired
    private AdminAccountService adminAccountService;

    /**
     * Lấy danh sách tất cả tài khoản.
     * GET /api/admin/accounts
     */
    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        try {
            List<AccountResponse> accounts = adminAccountService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Tìm kiếm và lọc tài khoản.
     * GET /api/admin/accounts/search?search=...&role=...&isActive=...
     * 
     * @param search   - Tìm theo username hoặc email (optional)
     * @param role     - Lọc theo role: TRAINER hoặc MEMBER (optional)
     * @param isActive - Lọc theo trạng thái: true/false (optional)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAccounts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        try {
            List<AccountResponse> accounts = adminAccountService.searchAndFilterAccounts(search, role, isActive);
            return ResponseEntity.ok(accounts);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xem chi tiết một tài khoản.
     * GET /api/admin/accounts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        try {
            AccountResponse account = adminAccountService.getAccountById(id);
            return ResponseEntity.ok(account);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Tạo tài khoản mới.
     * POST /api/admin/accounts
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createValidationErrorResponse(bindingResult));
        }

        try {
            AccountResponse account = adminAccountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin tài khoản.
     * PUT /api/admin/accounts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id,
                                          @Valid @RequestBody UpdateAccountRequest request,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createValidationErrorResponse(bindingResult));
        }

        try {
            AccountResponse account = adminAccountService.updateAccount(id, request);
            return ResponseEntity.ok(account);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Kích hoạt tài khoản.
     * PUT /api/admin/accounts/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateAccount(@PathVariable Long id) {
        try {
            AccountResponse account = adminAccountService.activateAccount(id);
            return ResponseEntity.ok(account);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Vô hiệu hóa tài khoản.
     * PUT /api/admin/accounts/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateAccount(@PathVariable Long id) {
        try {
            AccountResponse account = adminAccountService.deactivateAccount(id);
            return ResponseEntity.ok(account);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xóa mềm tài khoản.
     * DELETE /api/admin/accounts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        try {
            adminAccountService.softDeleteAccount(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa tài khoản thành công");
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Tạo response lỗi đơn giản.
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /**
     * Tạo response lỗi validation.
     */
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Dữ liệu không hợp lệ");
        Map<String, String> fieldErrors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        errors.put("fields", fieldErrors);
        return errors;
    }
}
