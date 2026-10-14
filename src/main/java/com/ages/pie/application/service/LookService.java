package com.ages.pie.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookPageDTO;
import com.ages.pie.application.dto.look.LookRequestDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookSuggestionDTO;
import com.ages.pie.application.dto.look.LookUpdateDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.BusinessException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.LookMapper;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.Look;
import com.ages.pie.domain.entity.LookProduct;
import com.ages.pie.domain.entity.LookWardrobeItem;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.WardrobeItem;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.LookProductRepository;
import com.ages.pie.infrastructure.repository.LookRepository;
import com.ages.pie.infrastructure.repository.LookWardrobeItemRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WardrobeItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LookService {

    private static final int MAX_LOOK_PIECES = 3;

    private final LookRepository lookRepository;
    private final LookWardrobeItemRepository lookWardrobeItemRepository;
    private final LookProductRepository lookProductRepository;
    private final WardrobeItemRepository wardrobeItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BodyProfileRepository bodyProfileRepository;
    private final PreferenceService preferenceService;
    private final ImageStorageService imageStorageService;
    private final LookMapper lookMapper;

    public LookService(LookRepository lookRepository,
            LookWardrobeItemRepository lookWardrobeItemRepository,
            LookProductRepository lookProductRepository,
            WardrobeItemRepository wardrobeItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            BodyProfileRepository bodyProfileRepository,
            PreferenceService preferenceService,
            ImageStorageService imageStorageService,
            LookMapper lookMapper) {
        this.lookRepository = lookRepository;
        this.lookWardrobeItemRepository = lookWardrobeItemRepository;
        this.lookProductRepository = lookProductRepository;
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.bodyProfileRepository = bodyProfileRepository;
        this.preferenceService = preferenceService;
        this.imageStorageService = imageStorageService;
        this.lookMapper = lookMapper;
    }

    @Transactional
    public LookResponseDTO create(UUID userId, LookRequestDTO dto) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        List<WardrobeItem> wardrobeItems = resolveWardrobeItems(userId, dto.wardrobeItemIds());
        List<Product> products = resolveProducts(dto.productIds());

        Look look = lookRepository.save(
                new Look(customer, dto.title(), dto.description(), dto.occasion()));

        lookWardrobeItemRepository.saveAll(wardrobeItems.stream()
                .map(wardrobeItem -> new LookWardrobeItem(look, wardrobeItem))
                .toList());
        lookProductRepository.saveAll(products.stream()
                .map(product -> new LookProduct(look, product))
                .toList());

        List<LookItemDTO> items = Stream.concat(
            wardrobeItems.stream().map(lookMapper::toItemDTO),
            products.stream().map(lookMapper::toItemDTO)
        ).toList();

        return lookMapper.toResponseDTO(look, items);
    }

    @Transactional(readOnly = true)
    public LookPageDTO findAllByUser(UUID userId, Pageable pageable) {
        Page<Look> looks = lookRepository.findByCustomerId(userId, pageable);
        List<LookResponseDTO> items = looks.getContent().stream()
                .map(lookMapper::toResponseDTO)
                .toList();
        return new LookPageDTO(items, looks.getTotalElements(), looks.getNumber(),
                looks.getSize(), looks.hasNext());
    }

        public List<LookResponseDTO> findAllByUser(UUID userId) {
        return lookRepository.findByCustomerIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(lookMapper::toResponseDTO)
            .toList();
        }

    @Transactional(readOnly = true)
    public LookResponseDTO findById(UUID userId, UUID lookId) {
        return lookMapper.toResponseDTO(findOwnedLook(userId, lookId));
    }

    @Transactional
    public LookResponseDTO update(UUID userId, UUID lookId, LookUpdateDTO dto) {
        Look look = findOwnedLook(userId, lookId);
        look.update(dto.title(), dto.description(), dto.occasion());

        return lookMapper.toResponseDTO(lookRepository.save(look));
    }

    @Transactional
    public void delete(UUID userId, UUID lookId) {
        Look look = findOwnedLook(userId, lookId);

        lookWardrobeItemRepository.deleteByLookId(lookId);
        lookProductRepository.deleteByLookId(lookId);
        lookRepository.delete(look);

        if (look.getPhotoStorageKey() != null) {
            imageStorageService.delete(look.getPhotoStorageKey());
        }
    }

    @Transactional
    public LookResponseDTO updatePhoto(UUID userId, UUID lookId, MultipartFile file) {
        Look look = findOwnedLook(userId, lookId);
        String previousKey = look.getPhotoStorageKey();

        String storageKey = imageStorageService.uploadForLook(file, lookId);
        look.updatePhoto(imageStorageService.toPublicUrl(storageKey), storageKey);
        lookRepository.save(look);

        if (previousKey != null) {
            imageStorageService.delete(previousKey);
        }

        return lookMapper.toResponseDTO(look);
    }

    @Transactional
    public void removePhoto(UUID userId, UUID lookId) {
        Look look = findOwnedLook(userId, lookId);
        String storageKey = look.getPhotoStorageKey();

        if (storageKey == null) {
            throw new ResourceNotFoundException("Look não possui imagem: " + lookId);
        }

        look.clearPhoto();
        lookRepository.save(look);
        imageStorageService.delete(storageKey);
    }

    @Transactional
    public LookItemDTO addWardrobeItem(UUID userId, UUID lookId, UUID wardrobeItemId) {
        Look look = findOwnedLook(userId, lookId);
        WardrobeItem wardrobeItem = findOwnedWardrobeItem(userId, wardrobeItemId);

        if (lookWardrobeItemRepository.existsByLookIdAndWardrobeItemId(lookId, wardrobeItemId)) {
            throw new BusinessException("Peça já está no look: " + wardrobeItemId);
        }

        lookWardrobeItemRepository.save(new LookWardrobeItem(look, wardrobeItem));
        return lookMapper.toItemDTO(wardrobeItem);
    }

    @Transactional
    public void removeWardrobeItem(UUID userId, UUID lookId, UUID wardrobeItemId) {
        findOwnedLook(userId, lookId);

        LookWardrobeItem link = lookWardrobeItemRepository
                .findByLookIdAndWardrobeItemId(lookId, wardrobeItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Peça não está no look: " + wardrobeItemId));

        lookWardrobeItemRepository.delete(link);
    }

    @Transactional
    public LookItemDTO addProduct(UUID userId, UUID lookId, UUID productId) {
        Look look = findOwnedLook(userId, lookId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + productId));

        if (lookProductRepository.existsByLookIdAndProductId(lookId, productId)) {
            throw new BusinessException("Produto já está no look: " + productId);
        }

        lookProductRepository.save(new LookProduct(look, product));
        return lookMapper.toItemDTO(product);
    }

    @Transactional
    public void removeProduct(UUID userId, UUID lookId, UUID productId) {
        findOwnedLook(userId, lookId);

        LookProduct link = lookProductRepository.findByLookIdAndProductId(lookId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto não está no look: " + productId));

        lookProductRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public LookSuggestionDTO suggestion(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
        PreferencesResponseDTO preferences = preferenceService.getPreferences(userId);

        List<WardrobeItem> wardrobe = wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId);
        List<Product> products = productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED);

        Set<String> avoidedColors = normalized(preferences.avoidColors());
        List<SuggestionPiece> pieces = new ArrayList<>(wardrobe.size() + products.size());
        wardrobe.stream()
                .filter(item -> !avoidedColors.contains(normalize(item.getColor())))
                .forEach(item -> pieces.add(new SuggestionPiece(item, null)));
        products.stream()
                .filter(product -> !avoidedColors.contains(normalize(product.getColor())))
                .forEach(product -> pieces.add(new SuggestionPiece(null, product)));

        if (pieces.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não há peças disponíveis para sugerir um look para o usuário: " + userId);
        }

        Set<String> preferredStyles = preferredStyles(user, wardrobe);
        Set<String> preferredColors = normalized(preferences.favoriteColors());
        preferredColors.addAll(normalized(preferences.highlightColors()));

        // Embaralha antes de ordenar para variar o look entre chamadas com a mesma pontuação.
        List<SuggestionPiece> ranked = new ArrayList<>(pieces);
        Collections.shuffle(ranked);
        ranked.sort(Comparator.comparingInt(
                (SuggestionPiece piece) -> score(piece, preferredStyles, preferredColors)).reversed());

        List<SuggestionPiece> picked = new ArrayList<>(MAX_LOOK_PIECES);
        Set<String> usedCategories = new HashSet<>();
        for (SuggestionPiece piece : ranked) {
            String category = normalize(piece.category());
            if (!category.isEmpty() && !usedCategories.add(category)) {
                continue;
            }
            picked.add(piece);
            if (picked.size() == MAX_LOOK_PIECES) {
                break;
            }
        }

        return new LookSuggestionDTO(picked.stream()
                .map(piece -> piece.wardrobeItem() != null? lookMapper.toItemDTO(piece.wardrobeItem()) : lookMapper.toItemDTO(piece.product()))
                .toList());
    }

    private Set<String> preferredStyles(User user, List<WardrobeItem> wardrobe) {
        Set<String> styles = new HashSet<>();
        if (user.getStyles() != null) {
            user.getStyles().stream().map(ProductStyle::getId).forEach(styles::add);
        }
        bodyProfileRepository.findByCustomerId(user.getId())
                .map(BodyProfile::getIdentifiedStyle)
                .filter(ProductStyle::isValid)
                .map(LookService::normalize)
                .ifPresent(styles::add);
        if (!styles.isEmpty()) {
            return styles;
        }

        // fallback: estilo mais frequente do guarda-roupa
        Map<String, Integer> frequency = new HashMap<>();
        for (WardrobeItem item : wardrobe) {
            String style = normalize(item.getStyle());
            if (!style.isEmpty()) {
                frequency.merge(style, 1, Integer::sum);
            }
        }
        frequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .ifPresent(styles::add);
        return styles;
    }

    private int score(SuggestionPiece piece, Set<String> preferredStyles, Set<String> preferredColors) {
        int score = 0;
        if (!preferredStyles.isEmpty() && piece.styles().stream().anyMatch(preferredStyles::contains)) {
            score += 2;
        }
        if (preferredColors.contains(normalize(piece.color()))) {
            score += 1;
        }
        return score;
    }

    private static Set<String> normalized(List<String> values) {
        Set<String> result = new HashSet<>();
        if (values != null) {
            for (String value : values) {
                String normalized = normalize(value);
                if (!normalized.isEmpty()) {
                    result.add(normalized);
                }
            }
        }
        return result;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record SuggestionPiece(WardrobeItem wardrobeItem, Product product) {

        String category() {
            return wardrobeItem != null ? wardrobeItem.getCategory() : product.getCategory();
        }

        String color() {
            return wardrobeItem != null ? wardrobeItem.getColor() : product.getColor();
        }

        List<String> styles() {
            if (wardrobeItem != null) {
                String style = normalize(wardrobeItem.getStyle());
                return style.isEmpty() ? List.of() : List.of(style);
            }
            return product.getStyles() == null ? List.of() : product.getStyles().stream().map(LookService::normalize).toList();
        }
    }

    private Look findOwnedLook(UUID userId, UUID lookId) {
        return lookRepository.findByIdAndCustomerId(lookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Look não encontrado: " + lookId));
    }

    private WardrobeItem findOwnedWardrobeItem(UUID userId, UUID wardrobeItemId) {
        return wardrobeItemRepository.findByIdAndCustomerId(wardrobeItemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Peça não encontrada no guarda-roupa do usuário: " + wardrobeItemId));
    }

    private List<WardrobeItem> resolveWardrobeItems(UUID userId, List<UUID> ids) {
        Set<UUID> requested = distinct(ids);
        if (requested.isEmpty()) {
            return List.of();
        }

        List<WardrobeItem> wardrobeItems = wardrobeItemRepository.findByIdInAndCustomerId(requested, userId);
        if (wardrobeItems.size() != requested.size()) {
            throw new ResourceNotFoundException("Peças não encontradas no guarda-roupa do usuário: "
                    + missing(requested, wardrobeItems.stream().map(WardrobeItem::getId).toList()));
        }

        return wardrobeItems;
    }

    private List<Product> resolveProducts(List<UUID> ids) {
        Set<UUID> requested = distinct(ids);
        if (requested.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findAllById(requested);
        if (products.size() != requested.size()) {
            throw new ResourceNotFoundException("Produtos não encontrados: "
                    + missing(requested, products.stream().map(Product::getId).toList()));
        }

        return products;
    }

    private <T> List<T> pickRandom(List<T> candidates, int size) {
        List<T> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);

        return shuffled.stream().limit(size).toList();
    }

    private Set<UUID> distinct(List<UUID> ids) {
        return ids == null ? Set.of() : new LinkedHashSet<>(ids);
    }

    private Set<UUID> missing(Set<UUID> requested, List<UUID> found) {
        Set<UUID> missing = new LinkedHashSet<>(requested);
        found.forEach(missing::remove);

        return missing;
    }
}
