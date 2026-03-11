package com.example.onlinebookshop;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "staff123";

        // 1) Tạo hash mới
        String newHash = encoder.encode(rawPassword);
        System.out.println("Raw password: " + rawPassword);
        System.out.println("New BCrypt hash: " + newHash);

        // 2) Test hash mới vừa tạo
        boolean matchNewHash = encoder.matches(rawPassword, newHash);
        System.out.println("Matches new hash: " + matchNewHash);

        // 3) Dán hash đang có trong DB vào đây để test
        String dbHash = "$2a$10$QEbH02WN0LOpvH9z6WKQW.mDnofRG1CNIt1cy2j9rWJdoeiO4jdb2";

        boolean matchDbHash = encoder.matches(rawPassword, dbHash);
        System.out.println("Matches DB hash: " + matchDbHash);
    }
}