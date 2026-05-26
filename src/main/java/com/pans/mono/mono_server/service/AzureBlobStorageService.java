package com.pans.mono.mono_server.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Profile("prod")
public class AzureBlobStorageService implements StorageService {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageService(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {
        this.containerClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(containerName);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String blobName = UUID.randomUUID() + ext;

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        return blobName;
    }

    @Override
    public Resource load(String filename) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(filename);
            if (!blobClient.exists()) return null;
            byte[] data = blobClient.downloadContent().toBytes();
            return new ByteArrayResource(data);
        } catch (Exception e) {
            return null;
        }
    }
}
