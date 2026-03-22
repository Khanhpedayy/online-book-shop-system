package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.Role;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Repository.RoleRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.dto.CreateUserRequest;
import com.example.onlinebookshop.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> searchUsers(String search, String roleCode, String status) {
        List<User> users = userRepository.searchUsers(
                search != null && search.isBlank() ? null : search,
                roleCode != null && roleCode.isBlank() ? null : roleCode,
                status != null && status.isBlank() ? null : status);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        return toDTO(user);
    }

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        // Validate
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên là bắt buộc");
        }

        String password = request.getPassword();
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*")
                || !password.matches(".*[!@#$%^&*(),.?\":{}|<>-_.+=\\[\\]\\\\/;'~`].*")) {
            throw new IllegalArgumentException(
                    "Mật khẩu yếu. Yêu cầu: Tối thiểu 8 ký tự, gồm chữ in HOA, in thường, số và ký tự đặc biệt");
        }

        // Check duplicates
        if (userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email này đã được đăng ký");
        }
        if (userRepository.findByFullNameIgnoreCaseAndDeletedAtIsNull(request.getFullName().trim()).isPresent()) {
            throw new IllegalArgumentException("Họ tên này đã tồn tại trong hệ thống");
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userRepository.findByPhoneAndDeletedAtIsNull(request.getPhone().trim()).isPresent()) {
                throw new IllegalArgumentException("Số điện thoại này đã được sử dụng");
            }
        }

        // Get role
        String roleCode = request.getRoleCode() != null ? request.getRoleCode() : "CUSTOMER";
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("Role không hợp lệ: " + roleCode));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus("ACTIVE");

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            if (!user.getFullName().equalsIgnoreCase(request.getFullName().trim())) {
                if (userRepository.findByFullNameIgnoreCaseAndDeletedAtIsNull(request.getFullName().trim())
                        .isPresent()) {
                    throw new IllegalArgumentException("Họ tên này đã tồn tại trong hệ thống");
                }
            }
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (user.getPhone() == null || !user.getPhone().equals(request.getPhone().trim())) {
                if (userRepository.findByPhoneAndDeletedAtIsNull(request.getPhone().trim()).isPresent()) {
                    throw new IllegalArgumentException("Số điện thoại này đã được sử dụng");
                }
            }
            user.setPhone(request.getPhone().trim());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
                if (userRepository.findByEmailAndDeletedAtIsNull(request.getEmail().trim()).isPresent()) {
                    throw new IllegalArgumentException("Email này đã được đăng ký");
                }
            }
            user.setEmail(request.getEmail().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String password = request.getPassword();
            if (password.length() < 8
                    || !password.matches(".*[A-Z].*")
                    || !password.matches(".*[a-z].*")
                    || !password.matches(".*[0-9].*")
                    || !password.matches(".*[!@#$%^&*(),.?\":{}|<>-_.+=\\[\\]\\\\/;'~`].*")) {
                throw new IllegalArgumentException(
                        "Mật khẩu yếu. Yêu cầu: Tối thiểu 8 ký tự, gồm chữ in HOA, in thường, số và ký tự đặc biệt");
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        if (request.getRoleCode() != null && !request.getRoleCode().isBlank()) {
            Role role = roleRepository.findByCode(request.getRoleCode())
                    .orElseThrow(() -> new IllegalArgumentException("Role không hợp lệ: " + request.getRoleCode()));
            user.setRole(role);
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        if ("ACTIVE".equals(user.getStatus())) {
            user.setStatus("INACTIVE");
        } else {
            user.setStatus("ACTIVE");
        }

        return toDTO(userRepository.save(user));
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
