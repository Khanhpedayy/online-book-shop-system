package com.example.onlinebookshop.staff.controller;

import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice for all staff controllers to provide common attributes
 * such as the staff's full name for the sidebar.
 */
@ControllerAdvice(basePackages = "com.example.onlinebookshop.staff.controller")
public class StaffCommonAdvice {

    private final UserRepository userRepository;

    public StaffCommonAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model, Authentication authentication) {
        String displayName = "STAFF";
        if (authentication != null && authentication.isAuthenticated()) {
            displayName = userRepository.findByEmailAndDeletedAtIsNull(authentication.getName())
                    .map(u -> u.getFullName())
                    .orElse(authentication.getName());
        }
        model.addAttribute("username", displayName);
        model.addAttribute("role", "STAFF");
    }
}
