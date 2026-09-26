package com.ages.pie.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImageStorageService {

    /**
     * Uploads a product image. Returns the storage key (path within bucket).
     */
    String upload(MultipartFile file, UUID productId);

    /**
     * Uploads a wardrobe item image. Returns the storage key (path within the wardrobe bucket).
     */
    String uploadForWardrobe(MultipartFile file, UUID wardrobeItemId);

    void delete(String storageKey);

    String toPublicUrl(String storageKey);
}
