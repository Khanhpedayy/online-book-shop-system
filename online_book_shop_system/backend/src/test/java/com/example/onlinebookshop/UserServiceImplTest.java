package com.example.onlinebookshop;

import com.example.onlinebookshop.entity.Role;
import com.example.onlinebookshop.entity.User;
import com.example.onlinebookshop.repository.RoleRepository;
import com.example.onlinebookshop.repository.UserRepository;
import com.example.onlinebookshop.service.UserServiceImpl;
import com.example.onlinebookshop.dto.CreateUserRequest;
import com.example.onlinebookshop.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private Role adminRole;
    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        adminRole = new Role();
        adminRole.setId(1);
        adminRole.setCode("ADMIN");
        adminRole.setName("System Administrator");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(adminRole);
        adminUser.setEmail("admin@test.com");
        adminUser.setFullName("Admin User");
        adminUser.setPhone("0123456789");
        adminUser.setStatus("ACTIVE");

        validRequest = new CreateUserRequest();
        validRequest.setEmail("newuser@test.com");
        validRequest.setPassword("StrongPass123!");
        validRequest.setFullName("New User");
        validRequest.setPhone("0987654321");
        validRequest.setRoleCode("ADMIN");
    }

    // 1. searchUsers_withoutFilter_returnAllUsers
    @Test
    void searchUsers_withoutFilter_returnAllUsers() {
        when(userRepository.searchUsers(null, null, null)).thenReturn(List.of(adminUser));

        List<UserDTO> result = userService.searchUsers(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
        verify(userRepository).searchUsers(null, null, null);
    }

    // 2. searchUsers_withSearchKeyword_returnMatched
    @Test
    void searchUsers_withSearchKeyword_returnMatched() {
        when(userRepository.searchUsers("admin", null, null)).thenReturn(List.of(adminUser));

        List<UserDTO> result = userService.searchUsers("admin", null, null);

        assertThat(result).hasSize(1);
        verify(userRepository).searchUsers("admin", null, null);
    }

    // 3. searchUsers_withRoleFilter_returnMatched
    @Test
    void searchUsers_withRoleFilter_returnMatched() {
        when(userRepository.searchUsers(null, "ADMIN", null)).thenReturn(List.of(adminUser));

        List<UserDTO> result = userService.searchUsers(null, "ADMIN", null);

        assertThat(result).hasSize(1);
        verify(userRepository).searchUsers(null, "ADMIN", null);
    }

    // 4. searchUsers_withStatusFilter_returnMatched
    @Test
    void searchUsers_withStatusFilter_returnMatched() {
        when(userRepository.searchUsers(null, null, "ACTIVE")).thenReturn(List.of(adminUser));

        List<UserDTO> result = userService.searchUsers(null, null, "ACTIVE");

        assertThat(result).hasSize(1);
        verify(userRepository).searchUsers(null, null, "ACTIVE");
    }

    // 5. getUserById_whenNotFound_throw404
    @Test
    void getUserById_whenNotFound_throw404() {
        when(userRepository.findByIdWithRole(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Không tìm thấy người dùng với ID");
    }

    // 6. createUser_whenValidRequest_success
    @Test
    void createUser_whenValidRequest_success() {
        when(userRepository.findByEmailAndDeletedAtIsNull(validRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByFullNameIgnoreCaseAndDeletedAtIsNull(validRequest.getFullName()))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneAndDeletedAtIsNull(validRequest.getPhone())).thenReturn(Optional.empty());
        when(roleRepository.findByCode(validRequest.getRoleCode())).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail(validRequest.getEmail());
        savedUser.setFullName(validRequest.getFullName());
        savedUser.setPhone(validRequest.getPhone());
        savedUser.setRole(adminRole);
        savedUser.setStatus("ACTIVE");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.createUser(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("newuser@test.com");
        verify(userRepository).save(any(User.class));
    }

    // 7. createUser_whenEmailExists_throwConflict
    @Test
    void createUser_whenEmailExists_throwConflict() {
        when(userRepository.findByEmailAndDeletedAtIsNull(validRequest.getEmail())).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email này đã được đăng ký");

        verify(userRepository, never()).save(any(User.class));
    }

    // 8. createUser_whenFullNameExists_throwError
    @Test
    void createUser_whenFullNameExists_throwError() {
        when(userRepository.findByEmailAndDeletedAtIsNull(validRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByFullNameIgnoreCaseAndDeletedAtIsNull(validRequest.getFullName()))
                .thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Họ tên này đã tồn tại trong hệ thống");

        verify(userRepository, never()).save(any(User.class));
    }

    // 9. createUser_whenPhoneNumberExists_throwError
    @Test
    void createUser_whenPhoneNumberExists_throwError() {
        when(userRepository.findByEmailAndDeletedAtIsNull(validRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByFullNameIgnoreCaseAndDeletedAtIsNull(validRequest.getFullName()))
                .thenReturn(Optional.empty());
        when(userRepository.findByPhoneAndDeletedAtIsNull(validRequest.getPhone())).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.createUser(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Số điện thoại này đã được sử dụng");

        verify(userRepository, never()).save(any(User.class));
    }

    // 10. toggleUserStatus_whenActive_setInactive
    @Test
    void toggleUserStatus_whenActive_setInactive() {
        adminUser.setStatus("ACTIVE");
        when(userRepository.findByIdWithRole(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.toggleUserStatus(1L);

        assertThat(result.getStatus()).isEqualTo("INACTIVE");
        verify(userRepository).save(adminUser);
    }

    // 11. toggleUserStatus_whenInactive_setActive
    @Test
    void toggleUserStatus_whenInactive_setActive() {
        adminUser.setStatus("INACTIVE");
        when(userRepository.findByIdWithRole(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.toggleUserStatus(1L);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(userRepository).save(adminUser);
    }
}
