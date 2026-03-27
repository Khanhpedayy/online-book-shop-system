package com.example.onlinebookshop.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload")
public class UploadController {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final Cloudinary cloudinary;

    public UploadController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PostMapping("/image")
    @Operation(summary = "Upload image", description = "Upload an image to Cloudinary (max 5MB, JPG/PNG/WebP/GIF)")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        // Validate
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

        // Upload to Cloudinary
        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "bookshop",
                        "resource_type", "image"
                ));

        String url = (String) result.get("secure_url");
        String publicId = (String) result.get("public_id");

        return ResponseEntity.ok(Map.of(
                "url", url,
                "publicId", publicId
        ));
    }

    @DeleteMapping("/image")
    @Operation(summary = "Delete image", description = "Delete an image from Cloudinary by publicId")
    public ResponseEntity<?> deleteImage(@RequestParam("publicId") String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
    }
}
