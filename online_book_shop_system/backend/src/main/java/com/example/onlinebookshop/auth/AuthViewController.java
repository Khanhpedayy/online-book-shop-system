package com.example.onlinebookshop.auth;

import com.example.onlinebookshop.Config.JwtUtils;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public AuthViewController(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/index.html";
        }
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

        if (isStaff) {
            return "redirect:/staff/dashboard";
        }
        if (isManager) {
            return "redirect:/pages/dashboard.html";
        }
        if (isAdmin) {
            return "redirect:/admin-entry";
        }

        if (isCustomer) {
            return "redirect:/index.html";
        }

        return "redirect:/index.html";
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
}
