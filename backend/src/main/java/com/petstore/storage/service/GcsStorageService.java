package com.petstore.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcs", matchIfMissing = true)
public class GcsStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(GcsStorageService.class);

    private final String bucketName;
    private final String baseUrl;

    public GcsStorageService(
            @Value("${app.storage.bucket-name:pet-store-bucket}") String bucketName,
            @Value("${app.storage.base-url:https://storage.googleapis.com/pet-store-bucket}") String baseUrl) {
        this.bucketName = bucketName;
        this.baseUrl = baseUrl;
    }

    @Override
    public String resolveImageUrl(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        return baseUrl + "/" + cleanPath;
    }

    @Override
    public String uploadImage(byte[] data, String fileName, String contentType) {
        logger.info("Uploading image {} to GCS bucket {}", fileName, bucketName);
        // Returns the formatted GCS URL
        return resolveImageUrl(fileName);
    }

    @Override
    public void deleteImage(String path) {
        logger.info("Deleting image {} from GCS bucket {}", path, bucketName);
    }
}
