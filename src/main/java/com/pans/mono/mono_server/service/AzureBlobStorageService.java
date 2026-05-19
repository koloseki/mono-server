package com.pans.mono.mono_server.service;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Profile("prod")
public class AzureBlobStorageService implements StorageService {

    @Override
    public String store(MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("Azure Blob Storage not implemented yet");
    }

    @Override
    public Resource load(String filename) {
        throw new UnsupportedOperationException("Azure Blob Storage not implemented yet");
    }
}
