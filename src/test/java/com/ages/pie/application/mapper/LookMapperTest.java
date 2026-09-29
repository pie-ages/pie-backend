package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookSuggestionDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Look;
import com.ages.pie.domain.entity.LookProduct;
import com.ages.pie.domain.entity.LookWardrobeItem;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.WardrobeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LookMapperTest {

    private final LookMapper lookMapper = new LookMapper();

    private UUID lookId;
    private UUID productId;
    private UUID wardrobeItemId;
    private User user;
    private Product product;
    private WardrobeItem wardrobeItem;
    private Look look;

    @BeforeEach
    void setUp() {
        lookId = UUID.randomUUID();
        productId = UUID.randomUUID();
        wardrobeItemId = UUID.randomUUID();

        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Company company = new Company(UUID.randomUUID(), "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", "hash(senha123)", null, null);
        product = new Product(company, "Blazer Social Feminino");
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(product, "category", "Casacos");
        ReflectionTestUtils.setField(product, "color", "Bege");
        ReflectionTestUtils.setField(product, "imageUrl", "https://loja.com/blazer.jpg");

        wardrobeItem = new WardrobeItem(user, null, "Camiseta branca", "Camisetas", "casual", "Branco");
        ReflectionTestUtils.setField(wardrobeItem, "id", wardrobeItemId);

        look = new Look(user, "Look de trabalho", "Formal", "Trabalho");
        ReflectionTestUtils.setField(look, "id", lookId);
    }

    @Test
    void toItemDTO_shouldUseWardrobeItemName_whenWardrobeItemHasNoProduct() {
        wardrobeItem.setImageReference("https://storage/peca.jpg", "wardrobe/x.jpg");

        LookItemDTO dto = lookMapper.toItemDTO(wardrobeItem);

        assertThat(dto.wardrobeItemId()).isEqualTo(wardrobeItemId);
        assertThat(dto.productId()).isNull();
        assertThat(dto.name()).isEqualTo("Camiseta branca");
        assertThat(dto.category()).isEqualTo("Camisetas");
        assertThat(dto.color()).isEqualTo("Branco");
        assertThat(dto.imageUrl()).isEqualTo("https://storage/peca.jpg");
    }

    @Test
    void toItemDTO_shouldUseProductNameAndImage_whenWardrobeItemHasProductAndNoPhoto() {
        WardrobeItem comProduto = new WardrobeItem(user, product, "Blazer", "Casacos", "classic", "Bege");
        ReflectionTestUtils.setField(comProduto, "id", wardrobeItemId);

        LookItemDTO dto = lookMapper.toItemDTO(comProduto);

        assertThat(dto.name()).isEqualTo("Blazer");
        assertThat(dto.imageUrl()).isEqualTo("https://loja.com/blazer.jpg");
        assertThat(dto.productId()).isNull();
    }

    @Test
    void toItemDTO_shouldPreferOwnPhoto_whenWardrobeItemHasProductAndPhoto() {
        WardrobeItem comProduto = new WardrobeItem(user, product, "Blazer", "Casacos", "classic", "Bege");
        ReflectionTestUtils.setField(comProduto, "id", wardrobeItemId);
        comProduto.setImageReference("https://storage/minha-foto.jpg", "wardrobe/y.jpg");

        LookItemDTO dto = lookMapper.toItemDTO(comProduto);

        assertThat(dto.imageUrl()).isEqualTo("https://storage/minha-foto.jpg");
    }

    @Test
    void toItemDTO_shouldMapProduct() {
        LookItemDTO dto = lookMapper.toItemDTO(product);

        assertThat(dto.productId()).isEqualTo(productId);
        assertThat(dto.wardrobeItemId()).isNull();
        assertThat(dto.name()).isEqualTo("Blazer Social Feminino");
        assertThat(dto.category()).isEqualTo("Casacos");
        assertThat(dto.color()).isEqualTo("Bege");
        assertThat(dto.imageUrl()).isEqualTo("https://loja.com/blazer.jpg");
    }

    @Test
    void toResponseDTO_shouldJoinWardrobeItemsAndProducts() {
        look.getWardrobeItems().add(new LookWardrobeItem(look, wardrobeItem));
        look.getProducts().add(new LookProduct(look, product));

        LookResponseDTO dto = lookMapper.toResponseDTO(look);

        assertThat(dto.id()).isEqualTo(lookId);
        assertThat(dto.title()).isEqualTo("Look de trabalho");
        assertThat(dto.description()).isEqualTo("Formal");
        assertThat(dto.occasion()).isEqualTo("Trabalho");
        assertThat(dto.aiGenerated()).isFalse();
        assertThat(dto.items()).hasSize(2);
        assertThat(dto.items()).extracting(LookItemDTO::wardrobeItemId).containsExactly(wardrobeItemId, null);
        assertThat(dto.items()).extracting(LookItemDTO::productId).containsExactly(null, productId);
    }

    @Test
    void toResponseDTO_shouldExposePhotoUrlWithoutStorageKey() {
        look.updatePhoto("https://storage/look.jpg", "looks/look.jpg");

        LookResponseDTO dto = lookMapper.toResponseDTO(look);

        assertThat(dto.photoUrl()).isEqualTo("https://storage/look.jpg");
        assertThat(LookResponseDTO.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("photoStorageKey");
    }

    @Test
    void toResponseDTO_shouldUseProvidedItems_whenListIsPassedExplicitly() {
        LookItemDTO item = lookMapper.toItemDTO(product);

        LookResponseDTO dto = lookMapper.toResponseDTO(look, List.of(item));

        assertThat(dto.items()).containsExactly(item);
    }

    @Test
    void toResponseDTO_shouldReturnEmptyItems_whenLookHasNoPieces() {
        assertThat(lookMapper.toResponseDTO(look).items()).isEmpty();
    }

    @Test
    void toSuggestionDTO_shouldMixWardrobeItemsAndProducts() {
        LookSuggestionDTO dto = lookMapper.toSuggestionDTO(List.of(wardrobeItem), List.of(product));

        assertThat(dto.items()).hasSize(2);
        assertThat(dto.items().get(0).wardrobeItemId()).isEqualTo(wardrobeItemId);
        assertThat(dto.items().get(1).productId()).isEqualTo(productId);
    }

    @Test
    void toSuggestionDTO_shouldReturnEmptyItems_whenNothingIsAvailable() {
        assertThat(lookMapper.toSuggestionDTO(List.of(), List.of()).items()).isEmpty();
    }
}
