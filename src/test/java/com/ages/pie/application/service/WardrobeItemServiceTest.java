package com.ages.pie.application.service;

import com.ages.pie.application.dto.wardrobe.WardrobeItemRequestDTO;
import com.ages.pie.application.dto.wardrobe.WardrobeItemResponseDTO;
import com.ages.pie.application.dto.wardrobe.WardrobeItemUpdateDTO;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.entity.WardrobeItem;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WardrobeItemRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WardrobeItemServiceTest {

    @Mock
    private WardrobeItemRepository wardrobeItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private UUID userId;
    private User user;
    private WardrobeItemService service;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        setId(user, userId);
        service = new WardrobeItemService(wardrobeItemRepository, userRepository, productRepository,
                imageStorageService, authenticatedUserProvider);
        when(authenticatedUserProvider.id()).thenReturn(userId);
    }

    @Test
    void create_shouldUploadAndPersistOnlyImageReference() {
        MockMultipartFile file = imageFile();
        WardrobeItem item = new WardrobeItem(user, null, "camisa", "azul");
        UUID itemId = UUID.randomUUID();
        setId(item, itemId);
        String storageKey = "wardrobe/" + itemId + "/image.jpg";
        String photoUrl = "https://supabase.test/" + storageKey;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.saveAndFlush(any(WardrobeItem.class))).thenReturn(item);
        when(imageStorageService.uploadForWardrobe(file, itemId)).thenReturn(storageKey);
        when(imageStorageService.toPublicUrl(storageKey)).thenReturn(photoUrl);
        when(wardrobeItemRepository.save(item)).thenReturn(item);

        WardrobeItemResponseDTO result = service.create(
                new WardrobeItemRequestDTO(null, "camisa", "azul"), file);

        assertThat(result.photoUrl()).isEqualTo(photoUrl);
        assertThat(item.getStorageKey()).isEqualTo(storageKey);
        verify(imageStorageService).uploadForWardrobe(file, itemId);
        verify(wardrobeItemRepository).save(item);
    }

    @Test
    void find_shouldNotReturnAnotherUsersItem() {
        UUID itemId = UUID.randomUUID();
        when(wardrobeItemRepository.findByIdAndCustomerId(itemId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(itemId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void update_withNewImage_shouldDeletePreviousObject() {
        UUID itemId = UUID.randomUUID();
        WardrobeItem item = new WardrobeItem(user, null, "camisa", "azul");
        setId(item, itemId);
        item.setImageReference("https://old", "wardrobe/" + itemId + "/old.jpg");
        MockMultipartFile file = imageFile();
        String newStorageKey = "wardrobe/" + itemId + "/new.jpg";

        when(wardrobeItemRepository.findByIdAndCustomerId(itemId, userId)).thenReturn(Optional.of(item));
        when(imageStorageService.uploadForWardrobe(file, itemId)).thenReturn(newStorageKey);
        when(imageStorageService.toPublicUrl(newStorageKey)).thenReturn("https://new");
        when(wardrobeItemRepository.save(item)).thenReturn(item);

        WardrobeItemResponseDTO result = service.update(itemId,
                new WardrobeItemUpdateDTO(null, "calca", "preta"), file);

        assertThat(result.photoUrl()).isEqualTo("https://new");
        verify(imageStorageService).delete("wardrobe/" + itemId + "/old.jpg");
    }

    @Test
    void delete_shouldRemoveObjectAndDatabaseRecord() {
        UUID itemId = UUID.randomUUID();
        WardrobeItem item = new WardrobeItem(user, null, "camisa", "azul");
        setId(item, itemId);
        item.setImageReference("https://image", "wardrobe/" + itemId + "/image.jpg");
        when(wardrobeItemRepository.findByIdAndCustomerId(itemId, userId)).thenReturn(Optional.of(item));

        service.delete(itemId);

        verify(imageStorageService).delete(item.getStorageKey());
        verify(wardrobeItemRepository).delete(item);
    }

    private MockMultipartFile imageFile() {
        return new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    private static void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
