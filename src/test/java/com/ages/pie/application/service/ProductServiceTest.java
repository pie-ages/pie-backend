package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductRequestDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.dto.product.ProductUpdateDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private UUID companyId;
    private UUID productId;
    private Company company;
    private Product product;
    private ProductResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        productId = UUID.randomUUID();
        company = new Company(companyId, "Loja X", "12345678000199", "Loja X LTDA", "Maria",
                "loja@email.com", "hash(senha123)", null, null);
        product = new Product(company, "Camiseta");
        product.setDescription("Camiseta 100% algodão");
        product.setCategory("Roupas");
        product.setPrice(new BigDecimal("49.90"));
        product.setImageUrl("https://exemplo.com/camiseta.jpg");
        product.setPurchaseUrl("https://loja.exemplo.com/camiseta");
        responseDTO = new ProductResponseDTO(productId, "Camiseta", "Camiseta 100% algodão",
                "Roupas", new BigDecimal("49.90"), "https://exemplo.com/camiseta.jpg",
                "https://loja.exemplo.com/camiseta", true, "Loja X", OffsetDateTime.now());
        lenient().doCallRealMethod().when(productMapper).updateEntityFromDto(any(), any());
    }

    private ProductRequestDTO requestDTO() {
        return new ProductRequestDTO("Camiseta", "Camiseta 100% algodão", "Roupas",
                new BigDecimal("49.90"), "https://exemplo.com/camiseta.jpg",
                "https://loja.exemplo.com/camiseta", companyId);
    }

    private ProductUpdateDTO updateDTO() {
        return new ProductUpdateDTO("Camiseta Premium", null, null,
                new BigDecimal("59.90"), null, null, null);
    }

    private static int statusOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        return ((ResponseStatusException) thrown).getStatusCode().value();
    }

    private Product savedProduct() {
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        return captor.getValue();
    }

    private void stubSavePassthrough() {
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_shouldReturnResponseDTO_whenDataIsValid() {
        ProductRequestDTO dto = requestDTO();
        stubSavePassthrough();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(productMapper.toResponseDTO(any())).thenReturn(responseDTO);

        ProductResponseDTO result = productService.create(dto);

        assertThat(result).isEqualTo(responseDTO);
        Product saved = savedProduct();
        assertThat(saved.getName()).isEqualTo("Camiseta");
        assertThat(saved.getPrice()).isEqualByComparingTo("49.90");
        assertThat(saved.getCompany()).isEqualTo(company);
    }

    @Test
    void create_shouldThrowNotFound_whenCompanyDoesNotExist() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> productService.create(requestDTO()));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenNameIsBlank() {
        ProductRequestDTO dto = new ProductRequestDTO("  ", null, null,
                new BigDecimal("49.90"), null, null, companyId);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        Throwable thrown = catchThrowable(() -> productService.create(dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(productRepository, never()).save(any());
    }

    @Test
    void findAll_shouldReturnMappedActiveProducts() {
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        assertThat(productService.findAll()).containsExactly(responseDTO);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoActiveProducts() {
        when(productRepository.findAllByActiveTrue()).thenReturn(List.of());

        assertThat(productService.findAll()).isEmpty();
    }

    @Test
    void findById_shouldReturnResponseDTO_whenProductExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        assertThat(productService.findById(productId)).isEqualTo(responseDTO);
    }

    @Test
    void findById_shouldThrowNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> productService.findById(productId));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void update_shouldApplyOnlyProvidedFields_whenProductExists() {
        stubSavePassthrough();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(any())).thenReturn(responseDTO);

        ProductResponseDTO result = productService.update(productId, updateDTO());

        assertThat(result).isEqualTo(responseDTO);
        Product saved = savedProduct();
        assertThat(saved.getName()).isEqualTo("Camiseta Premium");
        assertThat(saved.getPrice()).isEqualByComparingTo("59.90");
        assertThat(saved.getDescription()).isEqualTo("Camiseta 100% algodão");
        assertThat(saved.getCompany()).isEqualTo(company);
    }

    @Test
    void update_shouldResolveNewCompany_whenCompanyIdChanges() {
        UUID otherCompanyId = UUID.randomUUID();
        Company otherCompany = new Company(otherCompanyId, "Loja Y", "98765432000188", "Loja Y LTDA",
                "Joao", "outra@email.com", "hash(senha456)", null, null);
        ProductUpdateDTO dto = new ProductUpdateDTO(null, null, null, null, null, null, otherCompanyId);
        stubSavePassthrough();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(companyRepository.findById(otherCompanyId)).thenReturn(Optional.of(otherCompany));
        when(productMapper.toResponseDTO(any())).thenReturn(responseDTO);

        productService.update(productId, dto);

        assertThat(savedProduct().getCompany()).isEqualTo(otherCompany);
    }

    @Test
    void update_shouldThrowNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> productService.update(productId, updateDTO()));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowNotFound_whenNewCompanyDoesNotExist() {
        UUID unknownCompanyId = UUID.randomUUID();
        ProductUpdateDTO dto = new ProductUpdateDTO(null, null, null, null, null, null, unknownCompanyId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(companyRepository.findById(unknownCompanyId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> productService.update(productId, dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowBadRequest_whenNameIsBlank() {
        ProductUpdateDTO dto = new ProductUpdateDTO("  ", null, null, null, null, null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Throwable thrown = catchThrowable(() -> productService.update(productId, dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deactivate_shouldDeactivateProduct_whenProductExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deactivate(productId);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void deactivate_shouldThrowNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> productService.deactivate(productId));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(productRepository, never()).save(any());
    }
}
