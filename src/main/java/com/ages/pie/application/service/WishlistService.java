package com.ages.pie.application.service;

import java.util.UUID;

import com.ages.pie.application.dto.wishlist.WishlistItemResponseDTO;
import com.ages.pie.application.dto.wishlist.WishlistResponseDTO;
import com.ages.pie.application.mapper.WishlistMapper;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.Wishlist;
import com.ages.pie.domain.entity.WishlistItem;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WishlistItemRepository;
import com.ages.pie.infrastructure.repository.WishlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WishlistService {

    private static final String DEFAULT_WISHLIST_NAME = "Minha wishlist";

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistMapper wishlistMapper;

    public WishlistService(WishlistRepository wishlistRepository,
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            WishlistMapper wishlistMapper) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.wishlistMapper = wishlistMapper;
    }

    @Transactional(readOnly = true)
    public WishlistResponseDTO findByUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw userNotFound(userId);
        }

        return wishlistRepository.findByCustomerId(userId)
                .map(wishlistMapper::toResponseDTO)
                .orElseGet(wishlistMapper::empty);
    }

    @Transactional
    public WishlistItemResponseDTO addItem(UUID userId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produto não encontrado: " + productId));

        Wishlist wishlist = wishlistRepository.findByCustomerId(userId)
                .orElseGet(() -> createFor(userId));

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Produto já está na wishlist: " + productId);
        }

        WishlistItem item = wishlistItemRepository.save(new WishlistItem(wishlist, product));
        return wishlistMapper.toItemDTO(item);
    }

    @Transactional
    public void removeItem(UUID userId, UUID productId) {
        Wishlist wishlist = wishlistRepository.findByCustomerId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Wishlist não encontrada para o usuário: " + userId));

        WishlistItem item = wishlistItemRepository
                .findByWishlistIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produto não está na wishlist: " + productId));

        wishlistItemRepository.delete(item);
    }

    private Wishlist createFor(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));

        return wishlistRepository.save(new Wishlist(user, DEFAULT_WISHLIST_NAME));
    }

    private ResponseStatusException userNotFound(UUID userId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + userId);
    }
}
