package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductImageResponseDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.ProductImage;
import com.ages.pie.infrastructure.repository.ProductImageRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ProductImageService productImageService;

    private UUID productId;
    private UUID companyId;
    private Company company;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        company = new Company(companyId, "Loja Teste", "12345678000100", "Loja Teste LTDA",
                "Ana", "loja@teste.com", "hash", null, null);
        setId(company, companyId);
        product = new Product(company, "Produto Teste");
        setId(product, productId);
    }

    @Test
    void uploadImage_firstImage_shouldBePrimaryAndSyncImageUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[100]);
        String storageKey = "products/" + productId + "/abc.jpg";
        String url = "https://supabase.co/storage/v1/object/public/bucket/" + storageKey;

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.existsByProductId(productId)).thenReturn(false);
        when(productImageRepository.countByProductId(productId)).thenReturn(0L);
        when(imageStorageService.upload(file, productId)).thenReturn(storageKey);
        when(imageStorageService.toPublicUrl(storageKey)).thenReturn(url);
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductImageResponseDTO result = productImageService.uploadImage(productId, companyId, file);

        assertThat(result.isPrimary()).isTrue();
        assertThat(result.url()).isEqualTo(url);
        verify(productRepository).save(product);
        assertThat(product.getImageUrl()).isEqualTo(url);
    }

    @Test
    void uploadImage_secondImage_shouldNotBePrimary() {
        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", new byte[100]);
        String storageKey = "products/" + productId + "/def.png";
        String url = "https://supabase.co/storage/v1/object/public/bucket/" + storageKey;

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.existsByProductId(productId)).thenReturn(true);
        when(productImageRepository.countByProductId(productId)).thenReturn(1L);
        when(imageStorageService.upload(file, productId)).thenReturn(storageKey);
        when(imageStorageService.toPublicUrl(storageKey)).thenReturn(url);
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductImageResponseDTO result = productImageService.uploadImage(productId, companyId, file);

        assertThat(result.isPrimary()).isFalse();
        verify(productRepository, never()).save(any());
    }

    @Test
    void uploadImage_wrongOwner_shouldThrow403() {
        UUID otherCompanyId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[100]);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productImageService.uploadImage(productId, otherCompanyId, file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                        .isEqualTo(HttpStatus.FORBIDDEN.value()));
        verify(imageStorageService, never()).upload(any(), any());
    }

    @Test
    void uploadImage_productNotFound_shouldThrow404() {
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[100]);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.uploadImage(productId, companyId, file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void removeImage_nonPrimary_shouldNotPromoteOthers() {
        UUID imageId = UUID.randomUUID();
        ProductImage image = buildImage(imageId, false, 1);
        ProductImage primaryImage = buildImage(UUID.randomUUID(), true, 0);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.findByIdAndProductId(imageId, productId)).thenReturn(Optional.of(image));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId))
                .thenReturn(List.of(primaryImage));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productImageService.removeImage(productId, imageId, companyId);

        verify(imageStorageService).delete(image.getStorageKey());
        verify(productImageRepository).delete(image);
        assertThat(primaryImage.isPrimary()).isTrue();
    }

    @Test
    void removeImage_primary_shouldPromoteNext() {
        UUID primaryImageId = UUID.randomUUID();
        UUID secondImageId = UUID.randomUUID();
        ProductImage primaryImage = buildImage(primaryImageId, true, 0);
        ProductImage secondImage = buildImage(secondImageId, false, 1);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.findByIdAndProductId(primaryImageId, productId))
                .thenReturn(Optional.of(primaryImage));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId))
                .thenReturn(List.of(secondImage));
        when(productImageRepository.save(secondImage)).thenReturn(secondImage);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productImageService.removeImage(productId, primaryImageId, companyId);

        assertThat(secondImage.isPrimary()).isTrue();
        verify(productImageRepository).save(secondImage);
    }

    @Test
    void removeImage_notFound_shouldThrow404() {
        UUID imageId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.findByIdAndProductId(imageId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.removeImage(productId, imageId, companyId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value())
                        .isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void setPrimaryImage_shouldSwapPrimary() {
        UUID newPrimaryId = UUID.randomUUID();
        UUID oldPrimaryId = UUID.randomUUID();
        ProductImage oldPrimary = buildImage(oldPrimaryId, true, 0);
        ProductImage newPrimary = buildImage(newPrimaryId, false, 1);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId))
                .thenReturn(List.of(oldPrimary, newPrimary));
        when(productImageRepository.saveAll(any())).thenReturn(List.of(oldPrimary, newPrimary));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductImageResponseDTO result = productImageService.setPrimaryImage(productId, newPrimaryId, companyId);

        assertThat(result.isPrimary()).isTrue();
        assertThat(oldPrimary.isPrimary()).isFalse();
        assertThat(newPrimary.isPrimary()).isTrue();
    }

    private ProductImage buildImage(UUID id, boolean isPrimary, int order) {
        ProductImage img = new ProductImage(product, "https://example.com/" + id, "products/" + productId + "/" + id + ".jpg", isPrimary, order);
        setId(img, id);
        return img;
    }

    private static void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
