package com.example.onlinebookshop.auth;

import com.example.onlinebookshop.config.JwtUtils;
import com.example.onlinebookshop.dto.LoginRequest;
import com.example.onlinebookshop.dto.LoginResponse;
import com.example.onlinebookshop.dto.RegisterRequest;
import com.example.onlinebookshop.entity.Role;
import com.example.onlinebookshop.entity.User;
import com.example.onlinebookshop.repository.RoleRepository;
import com.example.onlinebookshop.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/login.html")
    public String legacyLoginRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STAFF".equals(a.getAuthority()));

        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));

        if (isStaff || isManager) {
            return "redirect:/staff/dashboard";
        }

        if (isAdmin) {
            return "redirect:/admin-entry";
        }

        if (isCustomer) {
            return "redirect:/index.html";
        }

        return "redirect:/login?error";
    }

    @GetMapping("/admin-entry")
    public String adminEntry(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            return "redirect:/post-login";
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(authentication.getName())
                .orElse(null);

        if (user == null) {
            return "redirect:/login?error";
        }

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "CUSTOMER";
        String token = jwtUtils.generateToken(user.getEmail(), roleCode);

        model.addAttribute("token", token);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", roleCode);

        return "auth/admin-entry";
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email hoặc mật khẩu không đúng"));
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "CUSTOMER";
        String token = jwtUtils.generateToken(user.getEmail(), roleCode);

        return ResponseEntity.ok(
                LoginResponse.builder()
                        .token(token)
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(roleCode)
                        .build()
        );
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email không được để trống"));
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu phải có ít nhất 6 ký tự"));
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
        }

        Optional<Role> customerRole = roleRepository.findByCode("CUSTOMER");
        if (customerRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không tìm thấy role CUSTOMER"));
        }

        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : "Customer");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(customerRole.get());
        user.setStatus("ACTIVE");

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công"));
    }
}