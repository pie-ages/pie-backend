package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductImageResponseDTO;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.ProductImage;
import com.ages.pie.infrastructure.repository.ProductImageRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageService.class);

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;

    public ProductImageService(ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ImageStorageService imageStorageService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public ProductImageResponseDTO uploadImage(UUID productId, UUID companyId, MultipartFile file) {
        Product product = findProductAndVerifyOwnership(productId, companyId);

        boolean isFirst = !productImageRepository.existsByProductId(productId);
        long nextOrder = productImageRepository.countByProductId(productId);

        String storageKey = imageStorageService.upload(file, productId);
        String url = imageStorageService.toPublicUrl(storageKey);

        ProductImage image = new ProductImage(product, url, storageKey, isFirst, (int) nextOrder);
        ProductImage saved = productImageRepository.save(image);

        if (isFirst) {
            syncProductImageUrl(product, List.of(saved));
        }

        logger.info("Imagem {} adicionada ao produto {}", saved.getId(), productId);
        return toDTO(saved);
    }

    @Transactional
    public void removeImage(UUID productId, UUID imageId, UUID companyId) {
        Product product = findProductAndVerifyOwnership(productId, companyId);

        ProductImage image = productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada."));

        boolean wasPrimary = image.isPrimary();
        imageStorageService.delete(image.getStorageKey());
        productImageRepository.delete(image);

        List<ProductImage> remaining = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        if (wasPrimary && !remaining.isEmpty()) {
            remaining.get(0).setPrimary(true);
            productImageRepository.save(remaining.get(0));
        }

        syncProductImageUrl(product, remaining);
        logger.info("Imagem {} removida do produto {}", imageId, productId);
    }

    @Transactional
    public ProductImageResponseDTO setPrimaryImage(UUID productId, UUID imageId, UUID companyId) {
        Product product = findProductAndVerifyOwnership(productId, companyId);

        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        ProductImage target = images.stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada."));

        images.forEach(img -> img.setPrimary(false));
        target.setPrimary(true);
        productImageRepository.saveAll(images);

        syncProductImageUrl(product, images);
        logger.info("Imagem {} definida como principal do produto {}", imageId, productId);
        return toDTO(target);
    }

    public List<ProductImageResponseDTO> listImages(UUID productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private Product findProductAndVerifyOwnership(UUID productId, UUID companyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        UUID ownerId = product.getCompany() != null ? product.getCompany().getId() : null;
        if (companyId == null || !companyId.equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas a empresa dona do produto pode gerenciar suas imagens");
        }

        return product;
    }

    private void syncProductImageUrl(Product product, List<ProductImage> images) {
        String url = images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(images.isEmpty() ? null : images.get(0).getUrl());

        product.setImageUrl(url);
        productRepository.save(product);
    }

    private ProductImageResponseDTO toDTO(ProductImage image) {
        return new ProductImageResponseDTO(
                image.getId(),
                image.getUrl(),
                image.isPrimary(),
                image.getDisplayOrder()
        );
    }
}
