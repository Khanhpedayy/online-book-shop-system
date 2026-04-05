package com.example.onlinebookshop.Service;

import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Entity.UserAddress;
import com.example.onlinebookshop.Repository.UserAddressRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import com.example.onlinebookshop.dto.*;
import com.example.onlinebookshop.util.VietnamPhoneUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerAccountServiceImpl implements CustomerAccountService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAccountServiceImpl(UserRepository userRepository,
                                      UserAddressRepository addressRepository,
                                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User requireUser(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfile(String email) {
        return toUserDto(requireUser(email));
    }

    @Override
    @Transactional
    public UserDTO updateProfile(String email, ProfileUpdateRequest request) {
        User user = requireUser(email);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            String fn = request.getFullName().trim();
            if (!fn.equalsIgnoreCase(user.getFullName())) {
                userRepository.findByFullNameIgnoreCaseAndDeletedAtIsNull(fn)
                        .filter(other -> !other.getId().equals(user.getId()))
                        .ifPresent(x -> {
                            throw new IllegalArgumentException("Họ tên này đã được dùng bởi tài khoản khác");
                        });
            }
            user.setFullName(fn);
        }
        if (request.getPhone() != null) {
            String ph = request.getPhone().trim();
            if (ph.isEmpty()) {
                user.setPhone(null);
            } else {
                String norm = VietnamPhoneUtils.normalizeVnPhone(ph);
                if (!VietnamPhoneUtils.isValidVnPhone(norm)) {
                    throw new IllegalArgumentException("Số điện thoại không hợp lệ (VD: 09xxxxxxxx).");
                }
                if (user.getPhone() == null || !user.getPhone().equals(norm)) {
                    userRepository.findByPhoneAndDeletedAtIsNull(norm)
                            .filter(other -> !other.getId().equals(user.getId()))
                            .ifPresent(x -> {
                                throw new IllegalArgumentException("Số điện thoại đã được dùng");
                            });
                }
                user.setPhone(norm);
            }
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().isBlank() ? null : request.getAvatarUrl().trim());
        }
        return toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getNewPassword() == null) {
            throw new IllegalArgumentException("currentPassword và newPassword là bắt buộc");
        }
        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới tối thiểu 6 ký tự");
        }
        User user = requireUser(email);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> listAddresses(String email) {
        User user = requireUser(email);
        return addressRepository.findByUser_IdAndDeletedAtIsNullOrderByDefaultAddressDescIdAsc(user.getId()).stream()
                .map(this::toAddressDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO createAddress(String email, AddressRequest request) {
        validateAddressRequest(request);
        User user = requireUser(email);
        boolean asDefault = Boolean.TRUE.equals(request.getDefaultAddress());
        if (asDefault || addressRepository.findByUser_IdAndDeletedAtIsNull(user.getId()).isEmpty()) {
            clearDefaults(user.getId());
            asDefault = true;
        }
        UserAddress a = new UserAddress();
        a.setUser(user);
        fillAddress(a, request);
        a.setDefaultAddress(asDefault);
        return toAddressDto(addressRepository.save(a));
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(String email, Long addressId, AddressRequest request) {
        validateAddressRequest(request);
        User user = requireUser(email);
        UserAddress a = addressRepository.findByIdAndUser_IdAndDeletedAtIsNull(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));
        fillAddress(a, request);
        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            clearDefaults(user.getId());
            a.setDefaultAddress(true);
        } else if (Boolean.FALSE.equals(request.getDefaultAddress())) {
            a.setDefaultAddress(false);
        }
        return toAddressDto(addressRepository.save(a));
    }

    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = requireUser(email);
        UserAddress a = addressRepository.findByIdAndUser_IdAndDeletedAtIsNull(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));
        boolean wasDefault = a.isDefaultAddress();
        a.setDeletedAt(LocalDateTime.now());
        addressRepository.save(a);
        if (wasDefault) {
            List<UserAddress> rest = addressRepository.findByUser_IdAndDeletedAtIsNullOrderByDefaultAddressDescIdAsc(user.getId());
            if (!rest.isEmpty()) {
                UserAddress first = rest.get(0);
                first.setDefaultAddress(true);
                addressRepository.save(first);
            }
        }
    }

    @Override
    @Transactional
    public AddressDTO setDefaultAddress(String email, Long addressId) {
        User user = requireUser(email);
        UserAddress a = addressRepository.findByIdAndUser_IdAndDeletedAtIsNull(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));
        clearDefaults(user.getId());
        a.setDefaultAddress(true);
        return toAddressDto(addressRepository.save(a));
    }

    private void clearDefaults(Long userId) {
        for (UserAddress x : addressRepository.findByUser_IdAndDeletedAtIsNull(userId)) {
            if (x.isDefaultAddress()) {
                x.setDefaultAddress(false);
                addressRepository.save(x);
            }
        }
    }

    private static void validateAddressRequest(AddressRequest request) {
        if (request.getRecipientName() == null || request.getRecipientName().isBlank()) {
            throw new IllegalArgumentException("recipientName là bắt buộc");
        }
        if (request.getLine1() == null || request.getLine1().isBlank()) {
            throw new IllegalArgumentException("line1 (địa chỉ) là bắt buộc");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String n = VietnamPhoneUtils.normalizeVnPhone(request.getPhone().trim());
            if (!VietnamPhoneUtils.isValidVnPhone(n)) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ (VD: 09xxxxxxxx).");
            }
        }
    }

    private static void fillAddress(UserAddress a, AddressRequest request) {
        if (request.getLabel() != null) {
            a.setLabel(request.getLabel().isBlank() ? null : request.getLabel().trim());
        }
        a.setRecipientName(request.getRecipientName().trim());
        if (request.getPhone() != null) {
            if (request.getPhone().isBlank()) {
                a.setPhone(null);
            } else {
                a.setPhone(VietnamPhoneUtils.normalizeVnPhone(request.getPhone().trim()));
            }
        }
        a.setLine1(request.getLine1().trim());
        if (request.getLine2() != null) {
            a.setLine2(request.getLine2().isBlank() ? null : request.getLine2().trim());
        }
        if (request.getCity() != null) {
            a.setCity(request.getCity().isBlank() ? null : request.getCity().trim());
        }
    }

    private AddressDTO toAddressDto(UserAddress a) {
        return AddressDTO.builder()
                .id(a.getId())
                .label(a.getLabel())
                .recipientName(a.getRecipientName())
                .phone(a.getPhone())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .defaultAddress(a.isDefaultAddress())
                .build();
    }

    private UserDTO toUserDto(User user) {
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
