package com.ages.pie.application.service;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookPageDTO;
import com.ages.pie.application.dto.look.LookRequestDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookSuggestionDTO;
import com.ages.pie.application.dto.look.LookUpdateDTO;
import com.ages.pie.application.exception.BusinessException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.LookMapper;
import com.ages.pie.domain.entity.Company;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LookServiceTest {

    @Mock
    private LookRepository lookRepository;

    @Mock
    private LookWardrobeItemRepository lookWardrobeItemRepository;

    @Mock
    private LookProductRepository lookProductRepository;

    @Mock
    private WardrobeItemRepository wardrobeItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private LookMapper lookMapper;

    @InjectMocks
    private LookService lookService;

    private UUID userId;
    private UUID otherUserId;
    private UUID lookId;
    private UUID wardrobeItemId;
    private UUID otherWardrobeItemId;
    private UUID productId;
    private User user;
    private Look look;
    private WardrobeItem wardrobeItem;
    private Product product;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        lookId = UUID.randomUUID();
        wardrobeItemId = UUID.randomUUID();
        otherWardrobeItemId = UUID.randomUUID();
        productId = UUID.randomUUID();

        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        ReflectionTestUtils.setField(user, "id", userId);

        Company company = new Company(UUID.randomUUID(), "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", "hash(senha123)", null, null);
        product = new Product(company, "Blazer Social Feminino");
        ReflectionTestUtils.setField(product, "id", productId);

        wardrobeItem = new WardrobeItem(user, null, "Camisetas", "Branco");
        ReflectionTestUtils.setField(wardrobeItem, "id", wardrobeItemId);

        look = new Look(user, "Look de trabalho", "Formal", "Trabalho");
        ReflectionTestUtils.setField(look, "id", lookId);
    }

    private LookItemDTO itemDTO() {
        return new LookItemDTO(wardrobeItemId, null, "Camiseta", "Camisetas", "Branco", null);
    }

    private LookResponseDTO responseDTO() {
        return new LookResponseDTO(lookId, "Look de trabalho", "Formal", "Trabalho", null, false,
                List.of(), null, null);
    }

    @Test
    void create_shouldPersistLookWithItems_whenItemsBelongToUser() {
        LookResponseDTO dto = responseDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.findByIdInAndCustomerId(anyCollection(), eq(userId)))
                .thenReturn(List.of(wardrobeItem));
        when(productRepository.findAllById(anySet())).thenReturn(List.of(product));
        when(lookRepository.save(any(Look.class))).thenReturn(look);
        when(lookMapper.toItemDTO(wardrobeItem)).thenReturn(itemDTO());
        when(lookMapper.toItemDTO(product)).thenReturn(itemDTO());
        when(lookMapper.toResponseDTO(eq(look), anyList())).thenReturn(dto);

        LookResponseDTO result = lookService.create(userId, new LookRequestDTO(
                "Look de trabalho", "Formal", "Trabalho", List.of(wardrobeItemId), List.of(productId)));

        assertThat(result).isEqualTo(dto);
        verify(lookWardrobeItemRepository).saveAll(anyList());
        verify(lookProductRepository).saveAll(anyList());
    }

    @Test
    void create_shouldPersistLookWithoutItems_whenListsAreNull() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lookRepository.save(any(Look.class))).thenReturn(look);
        when(lookMapper.toResponseDTO(eq(look), anyList())).thenReturn(responseDTO());

        lookService.create(userId, new LookRequestDTO("Look de trabalho", null, null, null, null));

        verifyNoInteractions(wardrobeItemRepository);
        verify(productRepository, never()).findAllById(anySet());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.create(userId, new LookRequestDTO(
                "Look de trabalho", null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
        verify(lookRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenWardrobeItemBelongsToAnotherUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.findByIdInAndCustomerId(anyCollection(), eq(userId)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> lookService.create(userId, new LookRequestDTO(
                "Look de trabalho", null, null, List.of(otherWardrobeItemId), null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(otherWardrobeItemId.toString());
        verify(lookRepository, never()).save(any());
        verify(lookWardrobeItemRepository, never()).saveAll(anyList());
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(anySet())).thenReturn(List.of());

        assertThatThrownBy(() -> lookService.create(userId, new LookRequestDTO(
                "Look de trabalho", null, null, null, List.of(productId))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
        verify(lookRepository, never()).save(any());
    }

    @Test
    void create_shouldResolveEachItemOnce_whenRequestRepeatsTheSameId() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(wardrobeItemRepository.findByIdInAndCustomerId(anyCollection(), eq(userId)))
                .thenReturn(List.of(wardrobeItem));
        when(lookRepository.save(any(Look.class))).thenReturn(look);
        when(lookMapper.toItemDTO(wardrobeItem)).thenReturn(itemDTO());
        when(lookMapper.toResponseDTO(eq(look), anyList())).thenReturn(responseDTO());

        lookService.create(userId, new LookRequestDTO("Look de trabalho", null, null,
                List.of(wardrobeItemId, wardrobeItemId), null));

        ArgumentCaptor<List<LookWardrobeItem>> captor = ArgumentCaptor.captor();
        verify(lookWardrobeItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void findAllByUser_shouldReturnOnlyLooksOfTheUser() {
        LookResponseDTO dto = responseDTO();
        when(lookRepository.findByCustomerIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(look));
        when(lookMapper.toResponseDTO(look)).thenReturn(dto);

        assertThat(lookService.findAllByUser(userId)).containsExactly(dto);
    }

    @Test
    void findAllByUser_shouldReturnPageWithLookItemsAndNextFlag() {
        LookResponseDTO firstDto = responseDTO();
        PageRequest pageable = PageRequest.of(0, 1);
        when(lookRepository.findByCustomerId(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(look), pageable, 2));
        when(lookMapper.toResponseDTO(look)).thenReturn(firstDto);

        LookPageDTO result = lookService.findAllByUser(userId, pageable);

        assertThat(result.items()).containsExactly(firstDto);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.hasNext()).isTrue();
        verify(lookRepository).findByCustomerId(userId, pageable);
    }

    @Test
    void findById_shouldReturnLook_whenLookBelongsToUser() {
        LookResponseDTO dto = responseDTO();
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(lookMapper.toResponseDTO(look)).thenReturn(dto);

        assertThat(lookService.findById(userId, lookId)).isEqualTo(dto);
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.findById(otherUserId, lookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(lookId.toString());
    }

    @Test
    void update_shouldApplyNewValues_whenLookBelongsToUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(lookRepository.save(look)).thenReturn(look);
        when(lookMapper.toResponseDTO(look)).thenReturn(responseDTO());

        lookService.update(userId, lookId, new LookUpdateDTO("Novo título", "Nova descrição", "Festa"));

        assertThat(look.getTitle()).isEqualTo("Novo título");
        assertThat(look.getDescription()).isEqualTo("Nova descrição");
        assertThat(look.getOccasion()).isEqualTo("Festa");
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.update(otherUserId, lookId,
                new LookUpdateDTO("Invadido", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(lookRepository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveLinksAndLook_whenLookBelongsToUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));

        lookService.delete(userId, lookId);

        verify(lookWardrobeItemRepository).deleteByLookId(lookId);
        verify(lookProductRepository).deleteByLookId(lookId);
        verify(lookRepository).delete(look);
    }

    @Test
    void delete_shouldRemoveImageFromStorage_whenLookHasPhoto() {
        look.updatePhoto("https://storage/looks/a.jpg", "looks/a.jpg");
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));

        lookService.delete(userId, lookId);

        verify(imageStorageService).delete("looks/a.jpg");
    }

    @Test
    void updatePhoto_shouldStoreUrlAndKey_whenFileIsValid() {
        MockMultipartFile file = new MockMultipartFile("file", "look.jpg", "image/jpeg", new byte[] { 1 });
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(imageStorageService.uploadForLook(file, lookId)).thenReturn("looks/" + lookId + "/a.jpg");
        when(imageStorageService.toPublicUrl("looks/" + lookId + "/a.jpg"))
                .thenReturn("https://storage/looks/" + lookId + "/a.jpg");
        when(lookMapper.toResponseDTO(look)).thenReturn(responseDTO());

        lookService.updatePhoto(userId, lookId, file);

        assertThat(look.getPhotoStorageKey()).isEqualTo("looks/" + lookId + "/a.jpg");
        assertThat(look.getPhotoUrl()).isEqualTo("https://storage/looks/" + lookId + "/a.jpg");
        verify(lookRepository).save(look);
    }

    @Test
    void updatePhoto_shouldDeletePreviousImage_whenLookAlreadyHadPhoto() {
        MockMultipartFile file = new MockMultipartFile("file", "look.jpg", "image/jpeg", new byte[] { 1 });
        look.updatePhoto("https://storage/looks/antiga.jpg", "looks/antiga.jpg");
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(imageStorageService.uploadForLook(file, lookId)).thenReturn("looks/nova.jpg");
        when(imageStorageService.toPublicUrl("looks/nova.jpg")).thenReturn("https://storage/looks/nova.jpg");
        when(lookMapper.toResponseDTO(look)).thenReturn(responseDTO());

        lookService.updatePhoto(userId, lookId, file);

        verify(imageStorageService).delete("looks/antiga.jpg");
        assertThat(look.getPhotoStorageKey()).isEqualTo("looks/nova.jpg");
    }

    @Test
    void updatePhoto_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        MockMultipartFile file = new MockMultipartFile("file", "look.jpg", "image/jpeg", new byte[] { 1 });
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.updatePhoto(otherUserId, lookId, file))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(imageStorageService);
    }

    @Test
    void removePhoto_shouldClearFieldsAndDeleteFromStorage_whenLookHasPhoto() {
        look.updatePhoto("https://storage/looks/a.jpg", "looks/a.jpg");
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));

        lookService.removePhoto(userId, lookId);

        assertThat(look.getPhotoUrl()).isNull();
        assertThat(look.getPhotoStorageKey()).isNull();
        verify(imageStorageService).delete("looks/a.jpg");
        verify(lookRepository).save(look);
    }

    @Test
    void removePhoto_shouldThrowResourceNotFoundException_whenLookHasNoPhoto() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));

        assertThatThrownBy(() -> lookService.removePhoto(userId, lookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(lookId.toString());
        verifyNoInteractions(imageStorageService);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.delete(otherUserId, lookId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(lookWardrobeItemRepository, never()).deleteByLookId(any());
        verify(lookRepository, never()).delete(any());
    }

    @Test
    void addWardrobeItem_shouldSaveLink_whenItemBelongsToUser() {
        LookItemDTO dto = itemDTO();
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(wardrobeItemRepository.findByIdAndCustomerId(wardrobeItemId, userId))
                .thenReturn(Optional.of(wardrobeItem));
        when(lookWardrobeItemRepository.existsByLookIdAndWardrobeItemId(lookId, wardrobeItemId))
                .thenReturn(false);
        when(lookMapper.toItemDTO(wardrobeItem)).thenReturn(dto);

        assertThat(lookService.addWardrobeItem(userId, lookId, wardrobeItemId)).isEqualTo(dto);
        verify(lookWardrobeItemRepository).save(any(LookWardrobeItem.class));
    }

    @Test
    void addWardrobeItem_shouldThrowResourceNotFoundException_whenItemBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(wardrobeItemRepository.findByIdAndCustomerId(otherWardrobeItemId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.addWardrobeItem(userId, lookId, otherWardrobeItemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(otherWardrobeItemId.toString());
        verify(lookWardrobeItemRepository, never()).save(any());
    }

    @Test
    void addWardrobeItem_shouldThrowBusinessException_whenItemAlreadyInLook() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(wardrobeItemRepository.findByIdAndCustomerId(wardrobeItemId, userId))
                .thenReturn(Optional.of(wardrobeItem));
        when(lookWardrobeItemRepository.existsByLookIdAndWardrobeItemId(lookId, wardrobeItemId))
                .thenReturn(true);

        assertThatThrownBy(() -> lookService.addWardrobeItem(userId, lookId, wardrobeItemId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(wardrobeItemId.toString());
        verify(lookWardrobeItemRepository, never()).save(any());
    }

    @Test
    void addProduct_shouldSaveLink_whenProductExists() {
        LookItemDTO dto = itemDTO();
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(lookProductRepository.existsByLookIdAndProductId(lookId, productId)).thenReturn(false);
        when(lookMapper.toItemDTO(product)).thenReturn(dto);

        assertThat(lookService.addProduct(userId, lookId, productId)).isEqualTo(dto);
        verify(lookProductRepository).save(any(LookProduct.class));
    }

    @Test
    void addProduct_shouldThrowBusinessException_whenProductAlreadyInLook() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(lookProductRepository.existsByLookIdAndProductId(lookId, productId)).thenReturn(true);

        assertThatThrownBy(() -> lookService.addProduct(userId, lookId, productId))
                .isInstanceOf(BusinessException.class);
        verify(lookProductRepository, never()).save(any());
    }

    @Test
    void removeWardrobeItem_shouldDeleteLink_whenItemIsInLook() {
        LookWardrobeItem link = new LookWardrobeItem(look, wardrobeItem);
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(lookWardrobeItemRepository.findByLookIdAndWardrobeItemId(lookId, wardrobeItemId))
                .thenReturn(Optional.of(link));

        lookService.removeWardrobeItem(userId, lookId, wardrobeItemId);

        verify(lookWardrobeItemRepository).delete(link);
    }

    @Test
    void removeWardrobeItem_shouldThrowResourceNotFoundException_whenItemIsNotInLook() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(lookWardrobeItemRepository.findByLookIdAndWardrobeItemId(lookId, wardrobeItemId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.removeWardrobeItem(userId, lookId, wardrobeItemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(wardrobeItemId.toString());
    }

    @Test
    void removeProduct_shouldDeleteLink_whenProductIsInLook() {
        LookProduct link = new LookProduct(look, product);
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(lookProductRepository.findByLookIdAndProductId(lookId, productId))
                .thenReturn(Optional.of(link));

        lookService.removeProduct(userId, lookId, productId);

        verify(lookProductRepository).delete(link);
    }

    @Test
    void removeProduct_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.removeProduct(otherUserId, lookId, productId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(lookProductRepository, never()).delete(any());
    }

    @Test
    void suggestion_shouldMixWardrobeItemsAndProducts_whenBothAreAvailable() {
        LookSuggestionDTO dto = new LookSuggestionDTO(List.of());
        when(wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wardrobeItem, wardrobeItem, wardrobeItem));
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED))
                .thenReturn(List.of(product, product, product));
        when(lookMapper.toSuggestionDTO(anyList(), anyList())).thenReturn(dto);

        assertThat(lookService.suggestion(userId)).isEqualTo(dto);

        ArgumentCaptor<List<WardrobeItem>> wardrobeCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<Product>> productCaptor = ArgumentCaptor.captor();
        verify(lookMapper).toSuggestionDTO(wardrobeCaptor.capture(), productCaptor.capture());
        assertThat(wardrobeCaptor.getValue()).hasSize(2);
        assertThat(productCaptor.getValue()).hasSize(2);
    }

    @Test
    void suggestion_shouldCompleteWithProducts_whenWardrobeIsEmpty() {
        when(wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED))
                .thenReturn(List.of(product, product, product, product, product));
        when(lookMapper.toSuggestionDTO(anyList(), anyList())).thenReturn(new LookSuggestionDTO(List.of()));

        lookService.suggestion(userId);

        ArgumentCaptor<List<Product>> productCaptor = ArgumentCaptor.captor();
        verify(lookMapper).toSuggestionDTO(anyList(), productCaptor.capture());
        assertThat(productCaptor.getValue()).hasSize(4);
    }

    @Test
    void suggestion_shouldNotPersistAnything() {
        when(wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(wardrobeItem));
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED))
                .thenReturn(List.of(product));
        when(lookMapper.toSuggestionDTO(anyList(), anyList())).thenReturn(new LookSuggestionDTO(List.of()));

        lookService.suggestion(userId);

        verify(lookRepository, never()).save(any());
        verify(lookWardrobeItemRepository, never()).save(any());
        verify(lookWardrobeItemRepository, never()).saveAll(anyList());
        verify(lookProductRepository, never()).save(any());
        verify(lookProductRepository, never()).saveAll(anyList());
    }

    @Test
    void create_shouldPersistLook_whenItemListsAreEmpty() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(lookRepository.save(any(Look.class))).thenReturn(look);
        when(lookMapper.toResponseDTO(eq(look), anyList())).thenReturn(responseDTO());

        lookService.create(userId, new LookRequestDTO("Sem pecas", null, null, List.of(), List.of()));

        ArgumentCaptor<List<LookWardrobeItem>> wardrobeCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<LookProduct>> productCaptor = ArgumentCaptor.captor();
        verify(lookWardrobeItemRepository).saveAll(wardrobeCaptor.capture());
        verify(lookProductRepository).saveAll(productCaptor.capture());
        assertThat(wardrobeCaptor.getValue()).isEmpty();
        assertThat(productCaptor.getValue()).isEmpty();
        verifyNoInteractions(wardrobeItemRepository);
    }

    @Test
    void create_shouldReportOnlyTheMissingProduct_whenOneOfSeveralIsMissing() {
        UUID missingProductId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(anySet())).thenReturn(List.of(product));

        assertThatThrownBy(() -> lookService.create(userId, new LookRequestDTO(
                "Look", null, null, null, List.of(productId, missingProductId))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingProductId.toString())
                .hasMessageNotContaining(productId.toString());
    }

    @Test
    void addProduct_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.addProduct(userId, lookId, productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
        verify(lookProductRepository, never()).save(any());
    }

    @Test
    void addProduct_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.addProduct(otherUserId, lookId, productId))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void removeWardrobeItem_shouldThrowResourceNotFoundException_whenLookBelongsToAnotherUser() {
        when(lookRepository.findByIdAndCustomerId(lookId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.removeWardrobeItem(otherUserId, lookId, wardrobeItemId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(lookWardrobeItemRepository, never()).delete(any());
    }

    @Test
    void removeProduct_shouldThrowResourceNotFoundException_whenProductIsNotInLook() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));
        when(lookProductRepository.findByLookIdAndProductId(lookId, productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lookService.removeProduct(userId, lookId, productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
    }

    @Test
    void update_shouldThrowIllegalArgumentException_whenTitleIsBlank() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));

        assertThatThrownBy(() -> lookService.update(userId, lookId, new LookUpdateDTO(" ", null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(lookRepository, never()).save(any());
    }

    @Test
    void findAllByUser_shouldReturnEmptyList_whenUserHasNoLooks() {
        when(lookRepository.findByCustomerIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        assertThat(lookService.findAllByUser(userId)).isEmpty();
        verifyNoInteractions(lookMapper);
    }

    @Test
    void findAllByUser_shouldReturnEmptyPage_whenUserHasNoLooks() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(lookRepository.findByCustomerId(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        LookPageDTO result = lookService.findAllByUser(userId, pageable);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        assertThat(result.hasNext()).isFalse();
        verifyNoInteractions(lookMapper);
    }

    @Test
    void delete_shouldNotCallStorage_whenLookHasNoPhoto() {
        when(lookRepository.findByIdAndCustomerId(lookId, userId)).thenReturn(Optional.of(look));

        lookService.delete(userId, lookId);

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void suggestion_shouldReturnOnlyWardrobeItems_whenCatalogIsEmpty() {
        when(wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(wardrobeItem, wardrobeItem, wardrobeItem));
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of());
        when(lookMapper.toSuggestionDTO(anyList(), anyList())).thenReturn(new LookSuggestionDTO(List.of()));

        lookService.suggestion(userId);

        ArgumentCaptor<List<WardrobeItem>> wardrobeCaptor = ArgumentCaptor.captor();
        verify(lookMapper).toSuggestionDTO(wardrobeCaptor.capture(), anyList());
        assertThat(wardrobeCaptor.getValue()).hasSize(2);
    }

    @Test
    void suggestion_shouldReturnEmptyComposition_whenNothingIsAvailable() {
        LookSuggestionDTO empty = new LookSuggestionDTO(List.of());
        when(wardrobeItemRepository.findAllByCustomerIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of());
        when(lookMapper.toSuggestionDTO(List.of(), List.of())).thenReturn(empty);

        assertThat(lookService.suggestion(userId)).isEqualTo(empty);
    }
}
