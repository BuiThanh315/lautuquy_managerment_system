package com.lautuquy.management.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entity mapping bảng `accounts`.
 * Implements UserDetails để tích hợp trực tiếp với Spring Security.
 * Note: Viết explicit getter/setter để tránh conflict với Lombok + UserDetails trên Java 25.
 */
@Entity
@Table(name = "accounts")
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String phone;

    /** Vai trò: CUSTOMER | STAFF | ADMIN */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    /** Trạng thái: ACTIVE | LOCKED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // =====================================================================
    // Constructors
    // =====================================================================

    public Account() {}

    public Account(Long id, String username, String password, String fullName,
                   String email, String phone, Role role, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
        if (this.role == null) {
            this.role = Role.CUSTOMER;
        }
    }

    // =====================================================================
    // Enums
    // =====================================================================

    public enum Role {
        CUSTOMER, STAFF, ADMIN
    }

    public enum Status {
        ACTIVE, LOCKED
    }

    // =====================================================================
    // Builder Pattern (thay thế Lombok @Builder)
    // =====================================================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String password;
        private String fullName;
        private String email;
        private String phone;
        private Role role;
        private Status status;
        private LocalDateTime createdAt;

        public Builder id(Long id)               { this.id = id; return this; }
        public Builder username(String u)        { this.username = u; return this; }
        public Builder password(String p)        { this.password = p; return this; }
        public Builder fullName(String f)        { this.fullName = f; return this; }
        public Builder email(String e)           { this.email = e; return this; }
        public Builder phone(String p)           { this.phone = p; return this; }
        public Builder role(Role r)              { this.role = r; return this; }
        public Builder status(Status s)          { this.status = s; return this; }
        public Builder createdAt(LocalDateTime t){ this.createdAt = t; return this; }

        public Account build() {
            return new Account(id, username, password, fullName, email, phone, role, status, createdAt);
        }
    }

    // =====================================================================
    // Explicit Getters & Setters (tránh conflict Lombok + UserDetails)
    // =====================================================================

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    // Override getUsername() từ UserDetails — trả về username field
    @Override
    public String getUsername()            { return username; }
    public void setUsername(String u)      { this.username = u; }

    // Override getPassword() từ UserDetails
    @Override
    public String getPassword()            { return password; }
    public void setPassword(String p)      { this.password = p; }

    public String getFullName()            { return fullName; }
    public void setFullName(String f)      { this.fullName = f; }

    public String getEmail()               { return email; }
    public void setEmail(String e)         { this.email = e; }

    public String getPhone()               { return phone; }
    public void setPhone(String p)         { this.phone = p; }

    public Role getRole()                  { return role; }
    public void setRole(Role r)            { this.role = r; }

    public Status getStatus()              { return status; }
    public void setStatus(Status s)        { this.status = s; }

    public LocalDateTime getCreatedAt()    { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    // =====================================================================
    // UserDetails Implementation — tích hợp Spring Security
    // =====================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public boolean isAccountNonExpired()     { return true; }

    @Override
    public boolean isAccountNonLocked()      { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * Tài khoản ACTIVE → true; LOCKED → false.
     * Spring Security ném DisabledException khi isEnabled() = false.
     */
    @Override
    public boolean isEnabled() {
        return this.status == Status.ACTIVE;
    }
}
