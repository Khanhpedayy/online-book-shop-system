package com.example.onlinebookshop.service;

import com.example.onlinebookshop.entity.User;
import com.example.onlinebookshop.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(">>> loadUserByUsername called with email = [" + email + "]");

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> {
                    System.out.println(">>> User not found in DB");
                    return new UsernameNotFoundException("User not found: " + email);
                });

        System.out.println(">>> Found user: " + user.getEmail());
        System.out.println(">>> Status: " + user.getStatus());
        System.out.println(">>> DeletedAt: " + user.getDeletedAt());
        System.out.println(">>> Password hash: " + user.getPasswordHash());

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus()) || user.getDeletedAt() != null) {
            System.out.println(">>> User is not ACTIVE or already deleted");
            throw new UsernameNotFoundException("Account is disabled: " + email);
        }

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "CUSTOMER";
        System.out.println(">>> Role code: " + roleCode);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                List.of(new SimpleGrantedAuthority("ROLE_" + roleCode))
        );
    }
}