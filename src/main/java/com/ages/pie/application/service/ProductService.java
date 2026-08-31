package com.ages.pie.application.service;

import com.ages.pie.application.dto.ProductRequestDTO;
import com.ages.pie.application.dto.ProductResponseDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponseDTO criar(ProductRequestDTO requestDTO) {
        logger.info("Criando product: {}", requestDTO.name());


        Product product = new Product(requestDTO.name());
        product.setDescription(requestDTO.description());
        product.setCategory(requestDTO.category());
        product.setPrice(requestDTO.price());
        product.setImageUrl(requestDTO.imageUrl());
        product.setPurchaseUrl(requestDTO.purchaseUrl());

        Product salvo = productRepository.save(product);
        logger.info("Product criado com id: {}", salvo.getId());
        return productMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listarTodos() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO buscarPorId(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        return productMapper.toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO atualizar(UUID id, ProductRequestDTO requestDTO) {
        logger.info("Atualizando product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        product.setName(requestDTO.name());
        product.setDescription(requestDTO.description());
        product.setCategory(requestDTO.category());
        product.setPrice(requestDTO.price());
        product.setImageUrl(requestDTO.imageUrl());
        product.setPurchaseUrl(requestDTO.purchaseUrl());
        
        Product atualizado = productRepository.save(product);
        logger.info("Product atualizado: {}", atualizado.getId());
        return productMapper.toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(UUID id) {
        logger.info("Deletando product: {}", id);

        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Produto não encontrado");
        }

        productRepository.deleteById(id);
        logger.info("Product deletado: {}", id);
    }
}
