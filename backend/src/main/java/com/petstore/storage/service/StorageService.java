package com.petstore.storage.service;

public interface StorageService {

    /**
     * Resolves a public URL for a given relative or absolute image path.
     */
    String resolveImageUrl(String path);

    /**
     * Uploads an image binary and returns the public accessible URL.
     */
    String uploadImage(byte[] data, String fileName, String contentType);

    /**
     * Deletes an image from storage.
     */
    void deleteImage(String path);
}
