package com.ages.pie.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImageStorageService {

    /**
     * Uploads file to storage. Returns the storage key (path within bucket).
     */
    String upload(MultipartFile file, UUID productId);

    /**
     * Uploads file under an arbitrary key prefix. Returns the storage key.
     */
    String uploadWithPrefix(MultipartFile file, String keyPrefix);

    void delete(String storageKey);

    String toPublicUrl(String storageKey);
}
