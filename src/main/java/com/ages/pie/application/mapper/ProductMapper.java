package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.product.ProductRequestDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
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
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.isActive(),
            companyName,
            product.getCreatedAt()
        );
    }

    public Product toEntity(ProductRequestDTO dto, Company company) {
        Product product = new Product(company, dto.name());
        product.setDescription(dto.description());
        product.setCategory(dto.category());
        product.setPrice(dto.price());
        product.setImageUrl(dto.imageUrl());
        product.setPurchaseUrl(dto.purchaseUrl());
        return product;
    }

    public void updateEntityFromDto(ProductUpdateDTO dto, Product product) {
        if (dto.name() != null) product.setName(dto.name());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.category() != null) product.setCategory(dto.category());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.imageUrl() != null) product.setImageUrl(dto.imageUrl());
        if (dto.purchaseUrl() != null) product.setPurchaseUrl(dto.purchaseUrl());
    }
}
