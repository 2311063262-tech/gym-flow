package dh13c8.nhom4.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO request cho tạo tài khoản mới bởi admin.
 */
public class CreateAccountRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100, message = "Username phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ chứa chữ cái, số và dấu gạch dưới")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, max = 50, message = "Password phải từ 6 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Role không được để trống")
    @Pattern(regexp = "^(TRAINER|MEMBER)$", message = "Role chỉ được là TRAINER hoặc MEMBER")
    private String role;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String fullName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại phải từ 10 đến 15 chữ số")
    private String phone;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(String username, String password, String role, String email, 
                               String fullName, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
