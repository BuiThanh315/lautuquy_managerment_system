package com.lautuquy.management.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO nhận dữ liệu đăng ký tài khoản từ form.
 * Sử dụng Jakarta Bean Validation để validate đầu vào.
 * Viết explicit getter/setter thay Lombok @Data để tương thích Java 25.
 */
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;

    @Email(message = "Địa chỉ email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$",
             message = "Số điện thoại không hợp lệ (VD: 0912345678)")
    private String phone;

    // Explicit getters & setters
    public String getUsername()            { return username; }
    public void setUsername(String u)      { this.username = u; }

    public String getPassword()            { return password; }
    public void setPassword(String p)      { this.password = p; }

    public String getFullName()            { return fullName; }
    public void setFullName(String f)      { this.fullName = f; }

    public String getEmail()               { return email; }
    public void setEmail(String e)         { this.email = e; }

    public String getPhone()               { return phone; }
    public void setPhone(String p)         { this.phone = p; }
}
