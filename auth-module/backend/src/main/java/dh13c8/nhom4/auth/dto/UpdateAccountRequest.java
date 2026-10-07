package dh13c8.nhom4.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO request cho cập nhật tài khoản bởi admin.
 */
public class UpdateAccountRequest {

    @Size(min = 3, max = 100, message = "Username phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username chỉ chứa chữ cái, số và dấu gạch dưới")
    private String username;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^(TRAINER|MEMBER)?$", message = "Role chỉ được là TRAINER hoặc MEMBER")
    private String role;

    private String fullName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải từ 10 đến 15 chữ số")
    private String phone;

    public UpdateAccountRequest() {
    }

    public UpdateAccountRequest(String username, String email, String role, String fullName, String phone) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
