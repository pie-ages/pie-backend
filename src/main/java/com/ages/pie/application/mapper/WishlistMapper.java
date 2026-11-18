package com.ages.pie.application.mapper;

import java.util.List;

import com.ages.pie.application.dto.wishlist.WishlistItemResponseDTO;
import com.ages.pie.application.dto.wishlist.WishlistResponseDTO;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.Wishlist;
import com.ages.pie.domain.entity.WishlistItem;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponseDTO toResponseDTO(Wishlist wishlist) {
        List<WishlistItemResponseDTO> items = wishlist.getItems()
                .stream()
                .map(this::toItemDTO)
                .toList();

        return new WishlistResponseDTO(
            wishlist.getId(),
            wishlist.getName(),
            items,
            wishlist.getCreatedAt(),
            wishlist.getUpdatedAt()
        );
    }

    public WishlistResponseDTO empty() {
        return new WishlistResponseDTO(null, null, List.of(), null, null);
    }

    public WishlistItemResponseDTO toItemDTO(WishlistItem item) {
        Product product = item.getProduct();

        return new WishlistItemResponseDTO(
            item.getId(),
            product.getId(),
            product.getName(),
            product.getColor(),
            product.getPrice(),
            product.getImageUrl(),
            product.getPurchaseUrl(),
            product.isActive(),
            item.getCreatedAt()
        );
    }
}
