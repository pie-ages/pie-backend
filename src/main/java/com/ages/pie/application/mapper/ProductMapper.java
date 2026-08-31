package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.ProductResponseDTO;
import com.ages.pie.domain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.isActive(),
            product.getCompany().getName(),
            product.getCreatedAt()
        );
    }
}
