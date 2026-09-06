package com.petstore.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
public class LocalStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    private final String baseUrl;

    public LocalStorageService(@Value("${app.storage.base-url:http://localhost:8080/storage}") String baseUrl) {
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
        logger.info("Saving image {} locally", fileName);
        return resolveImageUrl(fileName);
    }

    @Override
    public void deleteImage(String path) {
        logger.info("Deleting local image {}", path);
    }
}
