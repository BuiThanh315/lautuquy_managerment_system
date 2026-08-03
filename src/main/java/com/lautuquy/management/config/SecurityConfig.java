package com.lautuquy.management.config;

import com.lautuquy.management.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Cấu hình Spring Security:
 * - Phân quyền URL theo 3 vai trò: ADMIN, STAFF, CUSTOMER
 * - Form-based authentication với custom success handler (redirect theo role)
 * - Xử lý tài khoản LOCKED (DisabledException)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(AccountService accountService, PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh và trang công khai — cho phép tất cả (Cho phép khách không cần đăng nhập vẫn dùng được các chức năng /customer/**)
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/", "/landing", "/customer/**", "/auth/login", "/auth/register", "/error/**").permitAll()
                // Vùng ADMIN — chỉ ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Vùng STAFF — ROLE_STAFF và ROLE_ADMIN
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                // Endpoint REST API
                .requestMatchers("/api/**").permitAll()
                // Tất cả request còn lại phải đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(authenticationSuccessHandler())  // Redirect theo role
                .failureUrl("/auth/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/landing")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                // Chuyển hướng về trang 403 khi không có quyền truy cập
                .accessDeniedPage("/error/403")
            )
            // Kích hoạt CSRF (mặc định). Ngoại trừ các AJAX endpoint của /api/** và /customer/**
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/customer/**")  // Cho AJAX endpoint
            );

        return http.build();
    }

    /**
     * Redirect sau đăng nhập thành công theo vai trò (role) của tài khoản.
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/";

            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/admin/dashboard";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
                redirectUrl = "/staff/bookings";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                redirectUrl = "/customer/landing";
            }

            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }
}
