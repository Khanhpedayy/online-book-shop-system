package com.example.onlinebookshop.Controller;

import com.example.onlinebookshop.Config.JwtUtils;
import com.example.onlinebookshop.Entity.Role;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Repository.RoleRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.dto.LoginRequest;
import com.example.onlinebookshop.dto.LoginResponse;
import com.example.onlinebookshop.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import com.example.onlinebookshop.Service.EmailOtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailOtpService emailOtpService;

    public AuthController(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            EmailOtpService emailOtpService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailOtpService = emailOtpService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email hoặc mật khẩu không đúng"));
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "CUSTOMER";
        String token = jwtUtils.generateToken(user.getEmail(), roleCode);

        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleCode)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email là bắt buộc", "field", "email"));
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Họ tên là bắt buộc", "field", "name"));
        }

        // Check duplicate email
        Optional<User> existingUser = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email này đã được sử dụng", "field", "email"));
        }

        // Check duplicate phone
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            Optional<User> existingPhone = userRepository.findByPhoneAndDeletedAtIsNull(request.getPhone());
            if (existingPhone.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Số điện thoại này đã được sử dụng", "field", "phone"));
            }
        }

        // Validate password (cuối cùng)
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Mật khẩu phải có ít nhất 8 ký tự", "field", "password"));
        }
        if (!request.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Mật khẩu chưa đủ mạnh. Cần ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt",
                            "field", "password"));
        }

        // Generate and send OTP
        String otp = emailOtpService.generateAndSendOtp(request.getEmail());
        System.out.println("Generated OTP for " + request.getEmail() + ": " + otp);

        return ResponseEntity.ok(Map.of("message", "Đã gửi mã OTP đến email của bạn", "requireOtp", true));
    }

    @PostMapping("/verify-register-otp")
    public ResponseEntity<?> verifyRegisterOtp(@RequestBody com.example.onlinebookshop.dto.OtpVerifyRequest request) {
        if (request.getOtp() == null || request.getOtp().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã xác thực không được để trống"));
        }

        boolean isValid = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Mã xác thực không hợp lệ hoặc đã hết hạn", "field", "otp"));
        }

        // Get CUSTOMER role
        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));

        // Create user
        User user = new User();
        user.setRole(customerRole);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Tạo tài khoản thành công"));
    }
}
