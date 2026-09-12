package com.ages.pie.application.controller;

import java.util.UUID;

import com.ages.pie.application.dto.wishlist.AddWishlistItemRequestDTO;
import com.ages.pie.application.dto.wishlist.WishlistItemResponseDTO;
import com.ages.pie.application.dto.wishlist.WishlistResponseDTO;
import com.ages.pie.application.service.WishlistService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthenticatedUserProvider authenticatedUser;

    public WishlistController(WishlistService wishlistService,
            AuthenticatedUserProvider authenticatedUser) {
        this.wishlistService = wishlistService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public ResponseEntity<WishlistResponseDTO> find() {
        return ResponseEntity.ok(wishlistService.findByUser(authenticatedUser.id()));
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistItemResponseDTO> addItem(
            @Valid @RequestBody AddWishlistItemRequestDTO dto) {
        WishlistItemResponseDTO item = wishlistService.addItem(authenticatedUser.id(), dto.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID productId) {
        wishlistService.removeItem(authenticatedUser.id(), productId);
        return ResponseEntity.noContent().build();
    }
}
