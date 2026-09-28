package com.ages.pie.application.mapper;

import java.util.List;
import java.util.stream.Stream;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookSuggestionDTO;
import com.ages.pie.domain.entity.Look;
import com.ages.pie.domain.entity.LookProduct;
import com.ages.pie.domain.entity.LookWardrobeItem;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.WardrobeItem;
import org.springframework.stereotype.Component;

@Component
public class LookMapper {

    public LookResponseDTO toResponseDTO(Look look) {
        List<LookItemDTO> items = Stream.concat(
            look.getWardrobeItems().stream().map(this::toItemDTO),
            look.getProducts().stream().map(this::toItemDTO)
        ).toList();

        return toResponseDTO(look, items);
    }

    public LookResponseDTO toResponseDTO(Look look, List<LookItemDTO> items) {
        return new LookResponseDTO(
            look.getId(),
            look.getTitle(),
            look.getDescription(),
            look.getOccasion(),
            look.getPhotoUrl(),
            look.isAiGenerated(),
            items,
            look.getCreatedAt(),
            look.getUpdatedAt()
        );
    }

    public LookSuggestionDTO toSuggestionDTO(List<WardrobeItem> wardrobeItems, List<Product> products) {
        return new LookSuggestionDTO(Stream.concat(
            wardrobeItems.stream().map(this::toItemDTO),
            products.stream().map(this::toItemDTO)
        ).toList());
    }

    public LookItemDTO toItemDTO(LookWardrobeItem link) {
        return toItemDTO(link.getWardrobeItem());
    }

    public LookItemDTO toItemDTO(LookProduct link) {
        return toItemDTO(link.getProduct());
    }

    public LookItemDTO toItemDTO(WardrobeItem wardrobeItem) {
        Product product = wardrobeItem.getProduct();

        return new LookItemDTO(
            wardrobeItem.getId(),
            null,
            product != null ? product.getName() : wardrobeItem.getCategory(),
            wardrobeItem.getCategory(),
            wardrobeItem.getColor(),
            wardrobeItem.getPhotoUrl() != null || product == null
                ? wardrobeItem.getPhotoUrl()
                : product.getImageUrl()
        );
    }

    public LookItemDTO toItemDTO(Product product) {
        return new LookItemDTO(
            null,
            product.getId(),
            product.getName(),
            product.getCategory(),
            product.getColor(),
            product.getImageUrl()
        );
    }
}
