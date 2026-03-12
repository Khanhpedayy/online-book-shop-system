package com.example.onlinebookshop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ManagerDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public ManagerDataInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            ClassPathResource resource = new ClassPathResource("V1__add_staff_ops_tables.sql");
            String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Split by "IF NOT EXISTS" blocks and execute each statement
            // SQL Server can execute the whole script as one batch with GO-less statements
            String[] blocks = sql.split("(?=IF NOT EXISTS)");
            for (String block : blocks) {
                block = block.trim();
                if (!block.isEmpty()) {
                    try {
                        jdbc.execute(block);
                    } catch (Exception e) {
                        // Ignore errors (table/column already exists)
                        System.out.println("[ManagerDataInitializer] Skipped: " + e.getMessage());
                    }
                }
            }
            System.out.println("[ManagerDataInitializer] Migration V1 applied successfully.");
        } catch (Exception e) {
            System.err.println("[ManagerDataInitializer] Could not apply migration: " + e.getMessage());
        }
    }
}

