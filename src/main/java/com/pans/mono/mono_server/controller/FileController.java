package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.model.User;
import com.pans.mono.mono_server.repository.UserRepository;
import com.pans.mono.mono_server.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and download")
public class FileController {

    private final StorageService storageService;
    private final UserRepository userRepository;

    @Operation(summary = "Upload a file (max 10 MB)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Returns filename, originalName and fileUrl"),
        @ApiResponse(responseCode = "400", description = "File empty or too large"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestHeader("Authorization") String authHeader) {
        if (resolveUser(authHeader) == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File too large (max 10 MB)");
        }

        try {
            String filename = storageService.store(file);
            return ResponseEntity.ok(Map.of(
                    "filename", filename,
                    "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : filename,
                    "fileUrl", "/api/files/" + filename
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Download a file by filename")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File content as octet-stream"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{filename}")
    public ResponseEntity<?> download(@PathVariable String filename,
                                      @RequestHeader("Authorization") String authHeader) {
        if (resolveUser(authHeader) == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Resource resource = storageService.load(filename);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private User resolveUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return userRepository.findBySessionToken(authHeader.substring(7)).orElse(null);
    }
}
