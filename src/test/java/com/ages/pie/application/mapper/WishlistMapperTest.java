package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.wishlist.WishlistItemResponseDTO;
import com.ages.pie.application.dto.wishlist.WishlistResponseDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.Wishlist;
import com.ages.pie.domain.entity.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistMapperTest {

    private final WishlistMapper wishlistMapper = new WishlistMapper();

    private UUID productId;
    private Product product;
    private Wishlist wishlist;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        Company company = new Company(UUID.randomUUID(), "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", "hash(senha123)", null, null);
        product = new Product(company, "Blazer Social Feminino");
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(product, "price", new BigDecimal("279.90"));
        ReflectionTestUtils.setField(product, "imageUrl", "https://loja.com/blazer.jpg");
        ReflectionTestUtils.setField(product, "purchaseUrl", "https://loja.com/comprar/blazer");

        User user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        wishlist = new Wishlist(user, "Minha wishlist");
        ReflectionTestUtils.setField(wishlist, "id", UUID.randomUUID());
    }

    @Test
    void toItemDTO_shouldMapProductData_whenProductIsActive() {
        WishlistItemResponseDTO result = wishlistMapper.toItemDTO(new WishlistItem(wishlist, product));

        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.name()).isEqualTo("Blazer Social Feminino");
        assertThat(result.price()).isEqualByComparingTo("279.90");
        assertThat(result.imageUrl()).isEqualTo("https://loja.com/blazer.jpg");
        assertThat(result.purchaseUrl()).isEqualTo("https://loja.com/comprar/blazer");
        assertThat(result.available()).isTrue();
    }

    @Test
    void toItemDTO_shouldReturnNullColor_whenProductHasNoColor() {
        WishlistItemResponseDTO result = wishlistMapper.toItemDTO(new WishlistItem(wishlist, product));

        assertThat(result.color()).isNull();
    }

    @Test
    void toItemDTO_shouldMarkItemAsUnavailable_whenProductIsInactive() {
        ReflectionTestUtils.setField(product, "active", false);

        WishlistItemResponseDTO result = wishlistMapper.toItemDTO(new WishlistItem(wishlist, product));

        assertThat(result.available()).isFalse();
        assertThat(result.name()).isEqualTo("Blazer Social Feminino");
    }

    @Test
    void toResponseDTO_shouldMapWishlistWithItems() {
        wishlist.getItems().add(new WishlistItem(wishlist, product));

        WishlistResponseDTO result = wishlistMapper.toResponseDTO(wishlist);

        assertThat(result.id()).isEqualTo(wishlist.getId());
        assertThat(result.name()).isEqualTo("Minha wishlist");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().productId()).isEqualTo(productId);
    }

    @Test
    void empty_shouldReturnWishlistWithoutItems() {
        WishlistResponseDTO result = wishlistMapper.empty();

        assertThat(result.id()).isNull();
        assertThat(result.name()).isNull();
        assertThat(result.items()).isEqualTo(List.of());
    }
}
