package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.product.ProductCatalogItemDTO;
import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.application.dto.product.ProductPublicDetailDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponseDTO(Product product) {
        String companyName = product.getCompany() != null ? product.getCompany().getName() : null;
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getColor(),
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.getStatus(),
            companyName,
            product.getCreatedAt(),
            product.getUpdatedAt(),
            product.getStyles(),
            product.getSizes(),
            product.getMaterials()
        );
    }

    public void updateEntityFromDto(ProductUpdateDTO dto, Product product) {
        if (dto.name() != null) product.setName(dto.name());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.category() != null) product.setCategory(dto.category());
        if (dto.color() != null) product.setColor(dto.color());
        if (dto.styles() != null) product.setStyles(dto.styles());
        if (dto.materials() != null) product.setMaterials(dto.materials());
        if (dto.sizes() != null) product.setSizes(dto.sizes());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.imageUrl() != null) product.setImageUrl(dto.imageUrl());
        if (dto.purchaseUrl() != null) product.setPurchaseUrl(dto.purchaseUrl());
    }

    public ProductCatalogItemDTO toCatalogItemDTO(Product product) {
        return new ProductCatalogItemDTO(
            product.getId(),
            product.getName(),
            product.getCategory(),
            product.getColor(),
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.getCompany() != null ? product.getCompany().getName() : null,
            product.getStatus(),
            product.getStyles(),
            product.getSizes(),
            product.getMaterials()
        );
    }

    public ProductCatalogPageDTO toCatalogPageDTO(Page<Product> page) {
        return new ProductCatalogPageDTO(
            page.getContent().stream().map(this::toCatalogItemDTO).toList(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize()
        );
    }

    public ProductPublicDetailDTO toPublicDetailDTO(Product product) {
        boolean available = product.isActive() && product.getStatus() == ProductStatus.PUBLISHED;
        return new ProductPublicDetailDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getColor(),
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.getCompany() != null ? product.getCompany().getName() : null,
            product.getStyles(),
            product.getSizes(),
            product.getMaterials(),
            available
        );
    }
}
