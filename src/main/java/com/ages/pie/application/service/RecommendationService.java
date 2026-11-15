package com.ages.pie.application.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ages.pie.application.dto.RecommendedProductDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.domain.service.RecommendationScorer;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;


@Service
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 20;

    private final UserRepository userRepository;
    private final BodyProfileRepository bodyProfileRepository;
    private final ProductRepository productRepository;
    private final RecommendationScorer scorer;

    public RecommendationService(UserRepository userRepository,
                                 BodyProfileRepository bodyProfileRepository,
                                 ProductRepository productRepository,
                                 RecommendationScorer scorer) {
        this.userRepository = userRepository;
        this.bodyProfileRepository = bodyProfileRepository;
        this.productRepository = productRepository;
        this.scorer = scorer;
    }

    @Transactional(readOnly = true)
    public List<RecommendedProductDTO> recommendForUser(UUID userId) {
        return recommendForUser(userId, DEFAULT_LIMIT);
    }

    @Transactional(readOnly = true)
    public List<RecommendedProductDTO> recommendForUser(UUID userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BodyProfile profile = bodyProfileRepository.findByCustomerId(user.getId()).orElse(null);
        Set<String> userStyleIds = collectUserStyleIds(user, profile);
        Set<String> favoriteColors = collectFavoriteColors(profile);

        if (userStyleIds.isEmpty() && favoriteColors.isEmpty()) {
            return List.of();
        }

        return productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED).stream()
                .map(product -> new ScoredProduct(product, scorer.score(userStyleIds, favoriteColors, product)))
                .filter(scored -> scored.score > 0.0)
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed()
                        .thenComparing(Comparator.comparing(ScoredProduct::createdAt).reversed()))
                .limit(limit)
                .map(RecommendationService::toDTO)
                .toList();
    }

    /**
     * Ids dos produtos compatíveis com o perfil do usuário, pelos mesmos critérios de
     * {@link #recommendForUser(UUID)} (score &gt; 0). Retorna vazio quando o usuário não existe
     * ou não tem estilos/cores suficientes para avaliar compatibilidade.
     */
    @Transactional(readOnly = true)
    public Optional<Set<UUID>> findCompatibleProductIds(UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        BodyProfile profile = bodyProfileRepository.findByCustomerId(userId).orElse(null);
        Set<String> userStyleIds = collectUserStyleIds(user.get(), profile);
        Set<String> favoriteColors = new HashSet<>(collectFavoriteColors(profile));
        if (user.get().getFavoriteColors() != null) {
            user.get().getFavoriteColors().forEach(c -> favoriteColors.add(c.getId()));
        }
        if (userStyleIds.isEmpty() && favoriteColors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED).stream()
                .filter(product -> scorer.score(userStyleIds, favoriteColors, product) > 0.0)
                .map(Product::getId)
                .collect(java.util.stream.Collectors.toSet()));
    }

    private Set<String> collectUserStyleIds(User user, BodyProfile profile) {
        Set<String> ids = new HashSet<>();
        if (user.getStyles() != null) {
            user.getStyles().stream().map(ProductStyle::getId).forEach(ids::add);
        }
        if (profile != null && profile.getIdentifiedStyle() != null) {
            String identified = profile.getIdentifiedStyle().trim().toLowerCase();
            if (ProductStyle.isValid(identified)) {
                ids.add(identified);
            }
        }
        return ids;
    }

    private Set<String> collectFavoriteColors(BodyProfile profile) {
        Set<String> colors = new HashSet<>();
        if (profile != null && profile.getFavoriteColors() != null) {
            for (String color : profile.getFavoriteColors()) {
                if (color != null && !color.isBlank()) {
                    colors.add(color.trim().toLowerCase());
                }
            }
        }
        return colors;
    }

    private static RecommendedProductDTO toDTO(ScoredProduct scored) {
        Product product = scored.product;
        return new RecommendedProductDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getColor(),
                product.getPrice(),
                product.getImageUrl(),
                scored.score);
    }

    private record ScoredProduct(Product product, double score) {
        java.time.OffsetDateTime createdAt() {
            return product.getCreatedAt();
        }
    }
}
