package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductRequestDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
            CompanyRepository companyRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO requestDTO) {
        logger.info("Criando product: {}", requestDTO.name());

        Company company = companyRepository.findById(requestDTO.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        if (requestDTO.name() == null || requestDTO.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }

        Product product = new Product(company, requestDTO.name());
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
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAllByActiveTrue().stream()
                .map(productMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        return productMapper.toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO update(UUID id, ProductUpdateDTO dto) {
        logger.info("Atualizando product: {}", id);
    
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    
        if (dto.name() != null && dto.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }
    
        productMapper.updateEntityFromDto(dto, product);
    
        if (dto.companyId() != null) {
            product.setCompany(findCompanyOrThrow(dto.companyId()));
        }
    
        Product atualizado = productRepository.save(product);
        logger.info("Product atualizado: {}", atualizado.getId());
        return productMapper.toResponseDTO(atualizado);
    }
    
    private Company findCompanyOrThrow(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
    }

    @Transactional
    public void deactivate(UUID id) {
        logger.info("Desativando product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        product.setActive(false);
        productRepository.save(product);
        logger.info("Product desativado: {}", id);
    }

    @Transactional
    public void delete(UUID id) {
        logger.info("Deletando product: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }

        productRepository.deleteById(id);
        logger.info("Product deletado: {}", id);
    }
}
