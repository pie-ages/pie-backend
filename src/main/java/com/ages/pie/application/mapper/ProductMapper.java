package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.product.ProductCatalogItemDTO;
import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductCatalogItemDTO toCatalogItemDTO(Product product) {
        return new ProductCatalogItemDTO(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.getCompany().getName()
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
}
