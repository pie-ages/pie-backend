package com.ages.pie.application.service;

import com.ages.pie.application.dto.wardrobe.WardrobeItemRequestDTO;
import com.ages.pie.application.dto.wardrobe.WardrobeItemResponseDTO;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.WardrobeItem;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WardrobeItemRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class WardrobeItemService {

    private final WardrobeItemRepository wardrobeItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public WardrobeItemService(WardrobeItemRepository wardrobeItemRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository,
                               ImageStorageService imageStorageService,
                               AuthenticatedUserProvider authenticatedUserProvider) {
        this.wardrobeItemRepository = wardrobeItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.imageStorageService = imageStorageService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional
    public WardrobeItemResponseDTO create(WardrobeItemRequestDTO request, MultipartFile file) {
        User customer = currentUser();
        Product product = findProduct(request.productId());
        WardrobeItem item = wardrobeItemRepository.saveAndFlush(
                new WardrobeItem(customer, product, request.category(), request.color()));

        String storageKey = imageStorageService.uploadForWardrobe(file, item.getId());
        try {
            item.setImageReference(imageStorageService.toPublicUrl(storageKey), storageKey);
            return toResponse(wardrobeItemRepository.save(item));
        } catch (RuntimeException exception) {
            imageStorageService.delete(storageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<WardrobeItemResponseDTO> list() {
        UUID customerId = authenticatedUserProvider.id();
        return wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WardrobeItemResponseDTO find(UUID itemId) {
        return toResponse(findOwnedItem(itemId));
    }

    @Transactional
    public WardrobeItemResponseDTO update(UUID itemId, WardrobeItemRequestDTO request, MultipartFile file) {
        WardrobeItem item = findOwnedItem(itemId);
        Product product = findProduct(request.productId());
        String previousStorageKey = item.getStorageKey();

        item.update(product, request.category(), request.color());
        String newStorageKey = null;
        if (file != null && !file.isEmpty()) {
            try {
                newStorageKey = imageStorageService.uploadForWardrobe(file, item.getId());
                item.setImageReference(imageStorageService.toPublicUrl(newStorageKey), newStorageKey);
                WardrobeItemResponseDTO response = toResponse(wardrobeItemRepository.save(item));
                if (previousStorageKey != null) {
                    imageStorageService.delete(previousStorageKey);
                }
                return response;
            } catch (RuntimeException exception) {
                if (newStorageKey != null) {
                    imageStorageService.delete(newStorageKey);
                }
                throw exception;
            }
        }

        return toResponse(wardrobeItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID itemId) {
        WardrobeItem item = findOwnedItem(itemId);
        if (item.getStorageKey() != null) {
            imageStorageService.delete(item.getStorageKey());
        }
        wardrobeItemRepository.delete(item);
    }

    private WardrobeItem findOwnedItem(UUID itemId) {
        return wardrobeItemRepository.findByIdAndCustomerId(itemId, authenticatedUserProvider.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Peça não encontrada."));
    }

    private User currentUser() {
        UUID customerId = authenticatedUserProvider.id();
        return userRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."));
    }

    private Product findProduct(UUID productId) {
        if (productId == null) {
            return null;
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Produto não encontrado."));
    }

    private WardrobeItemResponseDTO toResponse(WardrobeItem item) {
        return new WardrobeItemResponseDTO(
                item.getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getCategory(),
                item.getColor(),
                item.getPhotoUrl());
    }
}