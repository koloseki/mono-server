package com.pans.mono.mono_server.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String store(MultipartFile file) throws IOException;
    Resource load(String filename);
}
