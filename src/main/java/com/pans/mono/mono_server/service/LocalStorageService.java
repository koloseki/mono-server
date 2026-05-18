package com.pans.mono.mono_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("!prod")
public class LocalStorageService implements StorageService {

    private final Path uploadDir;

    public LocalStorageService(@Value("${storage.local.path:./uploads}") String path) throws IOException {
        this.uploadDir = Paths.get(path).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + ext;

        Path target = uploadDir.resolve(filename);
        file.transferTo(target);

        return filename;
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
