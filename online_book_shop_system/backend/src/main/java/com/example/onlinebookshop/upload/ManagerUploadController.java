package com.example.onlinebookshop.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class ManagerUploadController {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    /**
     * Upload directory — uses a persistent folder on disk, NOT inside the JAR.
     * Defaults to ./uploads in the working directory.
     */
    @Value("${upload.dir:#{null}}")
    private String configuredUploadDir;

    private Path getUploadDir() throws IOException {
        Path uploadDir;
        if (configuredUploadDir != null && !configuredUploadDir.isBlank()) {
            uploadDir = Paths.get(configuredUploadDir);
        } else {
            // Default: create uploads/ folder in the working directory
            uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
        }
        Files.createDirectories(uploadDir);
        return uploadDir;
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File exceeds 5MB limit"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only JPG, PNG, WebP, GIF allowed"));
        }

        Path uploadDir = getUploadDir();

        // Generate unique filename
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().substring(0, 12) + ext;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());

        return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
    }

    private String getExtension(String filename) {
        if (filename == null)
            return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
