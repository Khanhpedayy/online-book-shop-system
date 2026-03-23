package com.example.onlinebookshop.Config;

import com.example.onlinebookshop.Entity.Role;
import com.example.onlinebookshop.Entity.User;
import com.example.onlinebookshop.Repository.RoleRepository;
import com.example.onlinebookshop.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepo, UserRepository userRepo,
                               PasswordEncoder passwordEncoder, JdbcTemplate jdbc) {
        return args -> {
            // === Seed roles ===
            if (roleRepo.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                Role adminRole = new Role();
                adminRole.setCode("ADMIN");
                adminRole.setName("Administrator");
                adminRole.setCreatedAt(now);
                adminRole.setUpdatedAt(now);
                roleRepo.save(adminRole);

                Role customerRole = new Role();
                customerRole.setCode("CUSTOMER");
                customerRole.setName("Customer");
                customerRole.setCreatedAt(now);
                customerRole.setUpdatedAt(now);
                roleRepo.save(customerRole);

                Role staffRole = new Role();
                staffRole.setCode("STAFF");
                staffRole.setName("Staff");
                staffRole.setCreatedAt(now);
                staffRole.setUpdatedAt(now);
                roleRepo.save(staffRole);

                Role managerRole = new Role();
                managerRole.setCode("MANAGER");
                managerRole.setName("Manager");
                managerRole.setCreatedAt(now);
                managerRole.setUpdatedAt(now);
                roleRepo.save(managerRole);
            }

            // === Seed users ===
            if (userRepo.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                Role adminRole = roleRepo.findByCode("ADMIN").orElseThrow();
                Role customerRole = roleRepo.findByCode("CUSTOMER").orElseThrow();
                Role staffRole = roleRepo.findByCode("STAFF").orElseThrow();
                Role managerRole = roleRepo.findByCode("MANAGER").orElseThrow();

                User admin = new User();
                admin.setRole(adminRole);
                admin.setEmail("admin@bookshop.com");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setFullName("Admin");
                admin.setStatus("ACTIVE");
                admin.setCreatedAt(now);
                userRepo.save(admin);

                User customer = new User();
                customer.setRole(customerRole);
                customer.setEmail("customer@example.com");
                customer.setPasswordHash(passwordEncoder.encode("123456"));
                customer.setFullName("Test Customer");
                customer.setStatus("ACTIVE");
                customer.setCreatedAt(now);
                userRepo.save(customer);

                User staff = new User();
                staff.setRole(staffRole);
                staff.setEmail("staff@bookshop.com");
                staff.setPasswordHash(passwordEncoder.encode("staff123"));
                staff.setFullName("Staff User");
                staff.setStatus("ACTIVE");
                staff.setCreatedAt(now);
                userRepo.save(staff);

                User manager = new User();
                manager.setRole(managerRole);
                manager.setEmail("manager@bookshop.com");
                manager.setPasswordHash(passwordEncoder.encode("manager123"));
                manager.setFullName("Manager User");
                manager.setStatus("ACTIVE");
                manager.setCreatedAt(now);
                userRepo.save(manager);
            }

            // === Apply SQL migrations for Manager/Inventory tables ===
            try {
                ClassPathResource resource = new ClassPathResource("V1__add_staff_ops_tables.sql");
                String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String[] blocks = sql.split("(?=IF NOT EXISTS)");
                for (String block : blocks) {
                    block = block.trim();
                    if (!block.isEmpty()) {
                        try {
                            jdbc.execute(block);
                        } catch (Exception e) {
                            System.out.println("[DataInitializer] Skipped: " + e.getMessage());
                        }
                    }
                }
                System.out.println("[DataInitializer] Migration V1 applied successfully.");
            } catch (Exception e) {
                System.err.println("[DataInitializer] Could not apply migration: " + e.getMessage());
            }
        };
    }
}