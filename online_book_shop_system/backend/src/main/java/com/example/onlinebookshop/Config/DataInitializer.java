package com.example.onlinebookshop.Config;

import com.example.onlinebookshop.Entity.Role;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Repository.RoleRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds initial data if not present. Run main bookstore schema first, then
 * carts script.
 * Skipped during tests.
 */
@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (roleRepo.count() == 0) {
                roleRepo.save(new Role(null, "ADMIN", "Administrator", null, null, null, null));
                roleRepo.save(new Role(null, "CUSTOMER", "Customer", null, null, null, null));
                roleRepo.save(new Role(null, "STAFF", "Staff", null, null, null, null));
                roleRepo.save(new Role(null, "MANAGER", "Manager", null, null, null, null));
            }
            if (userRepo.count() == 0) {
                // Seed admin user
                Role adminRole = roleRepo.findByCode("ADMIN").orElseThrow();
                User admin = new User();
                admin.setRole(adminRole);
                admin.setEmail("admin@bookshop.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setFullName("Admin");
                admin.setStatus("ACTIVE");
                userRepo.save(admin);

                // Seed test customer
                Role customerRole = roleRepo.findByCode("CUSTOMER").orElseThrow();
                User customer = new User();
                customer.setRole(customerRole);
                customer.setEmail("customer@example.com");
                customer.setPasswordHash(passwordEncoder.encode("123456"));
                customer.setFullName("Test Customer");
                customer.setStatus("ACTIVE");
                userRepo.save(customer);
            }
        };
    }
}
