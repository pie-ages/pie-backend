package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public ProductCatalogPageDTO findCatalog(String search, Pageable pageable) {
        String normalizedSearch = normalize(search);
        Page<Product> page = productRepository.findCatalog(normalizedSearch, pageable);
        return productMapper.toCatalogPageDTO(page);
    }

    private String normalize(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
