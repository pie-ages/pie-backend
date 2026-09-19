package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.application.dto.product.ProductRequestDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        product.setColor(requestDTO.color());
        product.setPrice(requestDTO.price());
        product.setImageUrl(requestDTO.imageUrl());
        product.setPurchaseUrl(requestDTO.purchaseUrl());

        Product salvo = productRepository.save(product);
        logger.info("Product criado com id: {}", salvo.getId());
        return productMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public ProductCatalogPageDTO findCatalog(String search, Pageable pageable) {
        String normalizedSearch = normalize(search);
        Page<Product> page = productRepository.findCatalog(normalizedSearch, pageable);
        return productMapper.toCatalogPageDTO(page);
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

    @Transactional(readOnly = true)
    public ProductCatalogPageDTO findByCompany(UUID companyId, ProductStatus status, String search, Pageable pageable) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada");
        }
        String normalizedSearch = normalize(search);
        Page<Product> page = productRepository.findByCompanyFiltered(companyId, status, normalizedSearch, pageable);
        return productMapper.toCatalogPageDTO(page);
    }

    @Transactional
    public ProductResponseDTO publish(UUID productId, UUID authenticatedCompanyId) {
        logger.info("Publicando product {} por empresa {}", productId, authenticatedCompanyId);
        Product product = findAndVerifyOwnership(productId, authenticatedCompanyId);
        ensureActive(product);
        if (product.getStatus() == ProductStatus.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto já está publicado");
        }
        ensurePublishable(product);
        product.setStatus(ProductStatus.PUBLICADO);
        Product salvo = productRepository.save(product);
        logger.info("Product publicado id={}", product.getId());
        return productMapper.toResponseDTO(salvo);
    }

    @Transactional
    public ProductResponseDTO unpublish(UUID productId, UUID authenticatedCompanyId) {
        logger.info("Pausando product {} por empresa {}", productId, authenticatedCompanyId);
        Product product = findAndVerifyOwnership(productId, authenticatedCompanyId);
        ensureActive(product);
        if (product.getStatus() != ProductStatus.PUBLICADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto não está publicado");
        }
        product.setStatus(ProductStatus.PAUSADO);
        Product salvo = productRepository.save(product);
        logger.info("Product pausado id={}", product.getId());
        return productMapper.toResponseDTO(salvo);
    }

    private Product findAndVerifyOwnership(UUID productId, UUID companyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        verifyOwnership(product, companyId);
        return product;
    }

    private void ensureActive(Product product) {
        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Produto desativado não pode ter disponibilidade alterada");
        }
    }

    private void ensurePublishable(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Produto precisa ter nome para ser publicado");
        }
        if (product.getPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Produto precisa ter preço para ser publicado");
        }
    }

    private void verifyOwnership(Product product, UUID authenticatedCompanyId) {
        if (authenticatedCompanyId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        UUID ownerId = product.getCompany() != null ? product.getCompany().getId() : null;
        if (ownerId == null || !ownerId.equals(authenticatedCompanyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas a empresa dona do produto pode alterar a disponibilidade");
        }
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

    private String normalize(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
