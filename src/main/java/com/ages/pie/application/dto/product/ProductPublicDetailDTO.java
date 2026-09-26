package com.ages.pie.application.dto.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductPublicDetailDTO(
    UUID id,
    String name,
    String description,
    String category,
    String color,
    BigDecimal price,
    String imageUrl,
    String purchaseUrl,
    String companyName,
    List<String> styles,
    List<String> sizes,
    List<String> materials,
    boolean available,
    List<ProductImageResponseDTO> images,
    boolean inWishlist
) {}
