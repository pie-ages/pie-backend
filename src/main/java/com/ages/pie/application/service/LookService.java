package com.ages.pie.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookItemRefDTO;
import com.ages.pie.application.dto.look.LookPageDTO;
import com.ages.pie.application.dto.look.LookRequestDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookSuggestionDTO;
import com.ages.pie.application.dto.look.LookUpdateDTO;
import com.ages.pie.application.exception.BusinessException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.LookMapper;
import com.ages.pie.domain.entity.Look;
import com.ages.pie.domain.entity.LookProduct;
import com.ages.pie.domain.entity.LookWardrobeItem;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.WardrobeItem;
import com.ages.pie.domain.enums.ProductStatus;
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

    private static final int SUGGESTION_SIZE = 4;

    private final LookRepository lookRepository;
    private final LookWardrobeItemRepository lookWardrobeItemRepository;
    private final LookProductRepository lookProductRepository;
    private final WardrobeItemRepository wardrobeItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final LookMapper lookMapper;

    public LookService(LookRepository lookRepository,
            LookWardrobeItemRepository lookWardrobeItemRepository,
            LookProductRepository lookProductRepository,
            WardrobeItemRepository wardrobeItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService,
            LookMapper lookMapper) {
        this.lookRepository = lookRepository;
        this.lookWardrobeItemRepository = lookWardrobeItemRepository;
        this.lookProductRepository = lookProductRepository;
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.lookMapper = lookMapper;
    }

    @Transactional
    public LookResponseDTO create(UUID userId, LookRequestDTO dto) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        List<LookItemRefDTO> refs = orderedRefs(dto);
        Map<UUID, WardrobeItem> wardrobeItems = indexBy(
                resolveWardrobeItems(userId, idsOf(refs, LookItemRefDTO::wardrobeItemId)),
                WardrobeItem::getId);
        Map<UUID, Product> products = indexBy(
                resolveProducts(idsOf(refs, LookItemRefDTO::productId)),
                Product::getId);

        Look look = lookRepository.save(
                new Look(customer, dto.title(), dto.description(), dto.occasion()));

        List<LookWardrobeItem> wardrobeLinks = new ArrayList<>();
        List<LookProduct> productLinks = new ArrayList<>();
        List<LookItemDTO> items = new ArrayList<>();
        for (int position = 0; position < refs.size(); position++) {
            LookItemRefDTO ref = refs.get(position);
            if (ref.wardrobeItemId() != null) {
                WardrobeItem wardrobeItem = wardrobeItems.get(ref.wardrobeItemId());
                wardrobeLinks.add(new LookWardrobeItem(look, wardrobeItem, position));
                items.add(lookMapper.toItemDTO(wardrobeItem));
            } else {
                Product product = products.get(ref.productId());
                productLinks.add(new LookProduct(look, product, position));
                items.add(lookMapper.toItemDTO(product));
            }
        }

        lookWardrobeItemRepository.saveAll(wardrobeLinks);
        lookProductRepository.saveAll(productLinks);

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

        lookWardrobeItemRepository.save(new LookWardrobeItem(look, wardrobeItem, nextPosition(lookId)));
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

        lookProductRepository.save(new LookProduct(look, product, nextPosition(lookId)));
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
        List<WardrobeItem> wardrobeItems = pickRandom(
                wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId), SUGGESTION_SIZE / 2);
        List<Product> products = pickRandom(
                productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED),
                SUGGESTION_SIZE - wardrobeItems.size());

        return lookMapper.toSuggestionDTO(wardrobeItems, products);
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

    /** Próxima posição livre no look, considerando peças do guarda-roupa e produtos. */
    private int nextPosition(UUID lookId) {
        return Math.max(
                lookWardrobeItemRepository.findMaxPositionByLookId(lookId),
                lookProductRepository.findMaxPositionByLookId(lookId)) + 1;
    }

    /**
     * Sequência final das peças. {@code items} (ordem do usuário) tem prioridade; no formato legado
     * (listas separadas) a ordem é: peças do guarda-roupa e depois produtos. Duplicatas mantêm a 1ª ocorrência.
     */
    private List<LookItemRefDTO> orderedRefs(LookRequestDTO dto) {
        Set<LookItemRefDTO> refs = new LinkedHashSet<>();

        if (dto.items() != null && !dto.items().isEmpty()) {
            for (LookItemRefDTO ref : dto.items()) {
                if ((ref.wardrobeItemId() == null) == (ref.productId() == null)) {
                    throw new BusinessException(
                            "Cada peça deve informar exatamente um de wardrobeItemId ou productId");
                }
                refs.add(ref);
            }
        } else {
            distinct(dto.wardrobeItemIds()).forEach(id -> refs.add(new LookItemRefDTO(id, null)));
            distinct(dto.productIds()).forEach(id -> refs.add(new LookItemRefDTO(null, id)));
        }

        return List.copyOf(refs);
    }

    private List<UUID> idsOf(List<LookItemRefDTO> refs, Function<LookItemRefDTO, UUID> extractor) {
        return refs.stream().map(extractor).filter(Objects::nonNull).toList();
    }

    private <T> Map<UUID, T> indexBy(List<T> entities, Function<T, UUID> idOf) {
        Map<UUID, T> index = new HashMap<>();
        entities.forEach(entity -> index.put(idOf.apply(entity), entity));
        return index;
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
