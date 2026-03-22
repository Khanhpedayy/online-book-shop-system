package com.example.onlinebookshop.upload;

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

        // Determine upload directory â€” inside static/uploads so Spring serves them
        String staticPath = getClass().getClassLoader().getResource("static").getPath();
        // Fix Windows path (remove leading /)
        if (staticPath.startsWith("/") && staticPath.contains(":")) {
            staticPath = staticPath.substring(1);
        }
        Path uploadDir = Paths.get(staticPath, "uploads");
        Files.createDirectories(uploadDir);

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

