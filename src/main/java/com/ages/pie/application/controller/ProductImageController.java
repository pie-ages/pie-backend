package com.ages.pie.application.controller;

import com.ages.pie.application.dto.product.ProductImageResponseDTO;
import com.ages.pie.application.service.ProductImageService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService productImageService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProductImageController(ProductImageService productImageService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.productImageService = productImageService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductImageResponseDTO> upload(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file) {
        UUID companyId = authenticatedUserProvider.id();
        ProductImageResponseDTO response = productImageService.uploadImage(productId, companyId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> remove(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {
        UUID companyId = authenticatedUserProvider.id();
        productImageService.removeImage(productId, imageId, companyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<ProductImageResponseDTO> setPrimary(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {
        UUID companyId = authenticatedUserProvider.id();
        ProductImageResponseDTO response = productImageService.setPrimaryImage(productId, imageId, companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductImageResponseDTO>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(productImageService.listImages(productId));
    }
}
