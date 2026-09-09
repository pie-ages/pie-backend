package com.ages.pie.application.service;

import com.ages.pie.application.dto.wishlist.WishlistItemResponseDTO;
import com.ages.pie.application.dto.wishlist.WishlistResponseDTO;
import com.ages.pie.application.mapper.WishlistMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.Wishlist;
import com.ages.pie.domain.entity.WishlistItem;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WishlistItemRepository;
import com.ages.pie.infrastructure.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistService wishlistService;

    private UUID userId;
    private UUID productId;
    private UUID wishlistId;
    private User user;
    private Product product;
    private Wishlist wishlist;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        wishlistId = UUID.randomUUID();

        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        ReflectionTestUtils.setField(user, "id", userId);

        Company company = new Company(UUID.randomUUID(), "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", "hash(senha123)", null, null);
        product = new Product(company, "Blazer Social Feminino");
        ReflectionTestUtils.setField(product, "id", productId);

        wishlist = new Wishlist(user, "Minha wishlist");
        ReflectionTestUtils.setField(wishlist, "id", wishlistId);
    }

    private WishlistItemResponseDTO itemDTO() {
        return new WishlistItemResponseDTO(UUID.randomUUID(), productId, "Blazer Social Feminino",
                null, new BigDecimal("279.90"), null, null, true, null);
    }

    private static int statusOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        return ((ResponseStatusException) thrown).getStatusCode().value();
    }

    @Test
    void findByUser_shouldReturnMappedWishlist_whenWishlistExists() {
        WishlistResponseDTO dto = new WishlistResponseDTO(wishlistId, "Minha wishlist", List.of(), null, null);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toResponseDTO(wishlist)).thenReturn(dto);

        WishlistResponseDTO result = wishlistService.findByUser(userId);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void findByUser_shouldReturnEmptyWishlist_whenUserHasNoWishlist() {
        WishlistResponseDTO empty = new WishlistResponseDTO(null, null, List.of(), null, null);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.empty());
        when(wishlistMapper.empty()).thenReturn(empty);

        WishlistResponseDTO result = wishlistService.findByUser(userId);

        assertThat(result).isEqualTo(empty);
    }

    @Test
    void findByUser_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        Throwable thrown = catchThrowable(() -> wishlistService.findByUser(userId));

        assertThat(statusOf(thrown)).isEqualTo(404);
    }

    @Test
    void addItem_shouldCreateWishlist_whenUserHasNoWishlistYet() {
        WishlistItemResponseDTO dto = itemDTO();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);
        when(wishlistItemRepository.existsByWishlistIdAndProductId(wishlistId, productId)).thenReturn(false);
        when(wishlistItemRepository.save(any(WishlistItem.class))).thenAnswer(call -> call.getArgument(0));
        when(wishlistMapper.toItemDTO(any(WishlistItem.class))).thenReturn(dto);

        WishlistItemResponseDTO result = wishlistService.addItem(userId, productId);

        assertThat(result).isEqualTo(dto);
        verify(wishlistRepository).save(any(Wishlist.class));
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    void addItem_shouldReuseWishlist_whenWishlistAlreadyExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.existsByWishlistIdAndProductId(wishlistId, productId)).thenReturn(false);
        when(wishlistItemRepository.save(any(WishlistItem.class))).thenAnswer(call -> call.getArgument(0));
        when(wishlistMapper.toItemDTO(any(WishlistItem.class))).thenReturn(itemDTO());

        wishlistService.addItem(userId, productId);

        verify(wishlistRepository, never()).save(any(Wishlist.class));
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    void addItem_shouldThrowNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> wishlistService.addItem(userId, productId));

        assertThat(statusOf(thrown)).isEqualTo(404);
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    void addItem_shouldThrowConflict_whenProductAlreadyInWishlist() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.existsByWishlistIdAndProductId(wishlistId, productId)).thenReturn(true);

        Throwable thrown = catchThrowable(() -> wishlistService.addItem(userId, productId));

        assertThat(statusOf(thrown)).isEqualTo(409);
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    void addItem_shouldThrowNotFound_whenUserDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> wishlistService.addItem(userId, productId));

        assertThat(statusOf(thrown)).isEqualTo(404);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void removeItem_shouldDeleteItem_whenProductIsInWishlist() {
        WishlistItem item = new WishlistItem(wishlist, product);
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findByWishlistIdAndProductId(wishlistId, productId))
                .thenReturn(Optional.of(item));

        wishlistService.removeItem(userId, productId);

        verify(wishlistItemRepository).delete(item);
    }

    @Test
    void removeItem_shouldThrowNotFound_whenUserHasNoWishlist() {
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> wishlistService.removeItem(userId, productId));

        assertThat(statusOf(thrown)).isEqualTo(404);
        verify(wishlistItemRepository, never()).delete(any());
    }

    @Test
    void removeItem_shouldThrowNotFound_whenProductIsNotInWishlist() {
        when(wishlistRepository.findByCustomerId(userId)).thenReturn(Optional.of(wishlist));
        when(wishlistItemRepository.findByWishlistIdAndProductId(wishlistId, productId))
                .thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> wishlistService.removeItem(userId, productId));

        assertThat(statusOf(thrown)).isEqualTo(404);
        verify(wishlistItemRepository, never()).delete(any());
    }
}
