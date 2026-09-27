package com.ages.pie.infrastructure.storage;

import com.ages.pie.application.config.SupabaseStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SupabaseImageStorageServiceTest {

    private SupabaseStorageProperties props;
    private SupabaseImageStorageService service;

    @BeforeEach
    void setUp() {
        props = new SupabaseStorageProperties();
        props.setUrl("https://test.supabase.co");
        props.setServiceRoleKey("test-key");
        props.setBucket("product-images");
        props.setWardrobeBucket("wardrobe-items");
        props.setMaxFileSizeMb(5);
        service = new SupabaseImageStorageService(props, mock(RestClient.class));
    }

    // --- validateFile ---

    @Test
    void validateFile_empty_shouldThrow400() {
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> service.validateFile(empty))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateFile_nullContentType_shouldThrow400() {
        MockMultipartFile file = new MockMultipartFile("file", "img.bin", null, new byte[100]);

        assertThatThrownBy(() -> service.validateFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateFile_invalidContentType_shouldThrow400() {
        MockMultipartFile pdf = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[100]);

        assertThatThrownBy(() -> service.validateFile(pdf))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateFile_tooLarge_shouldThrow400() {
        byte[] bigFile = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", bigFile);

        assertThatThrownBy(() -> service.validateFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateFile_validTypes_shouldNotThrow() {
        for (String type : SupabaseImageStorageService.ALLOWED_CONTENT_TYPES) {
            MockMultipartFile file = new MockMultipartFile("file", "img", type, new byte[100]);
            service.validateFile(file);
        }
    }

    // --- getBucketForKey ---

    @Test
    void getBucketForKey_productPrefix_returnsProductBucket() {
        String key = SupabaseImageStorageService.PRODUCTS_FOLDER + "/" + UUID.randomUUID() + "/img.jpg";
        assertThat(service.getBucketForKey(key)).isEqualTo("product-images");
    }

    @Test
    void getBucketForKey_wardrobePrefix_returnsWardrobeBucket() {
        String key = SupabaseImageStorageService.WARDROBE_FOLDER + "/" + UUID.randomUUID() + "/img.jpg";
        assertThat(service.getBucketForKey(key)).isEqualTo("wardrobe-items");
    }

    // --- toPublicUrl ---

    @Test
    void toPublicUrl_productKey_usesProductBucket() {
        String key = SupabaseImageStorageService.PRODUCTS_FOLDER + "/" + UUID.randomUUID() + "/img.jpg";
        String url = service.toPublicUrl(key);

        assertThat(url).startsWith("https://test.supabase.co/storage/v1/object/public/product-images/");
        assertThat(url).endsWith(key);
    }

    @Test
    void toPublicUrl_wardrobeKey_usesWardrobeBucket() {
        String key = SupabaseImageStorageService.WARDROBE_FOLDER + "/" + UUID.randomUUID() + "/img.png";
        String url = service.toPublicUrl(key);

        assertThat(url).startsWith("https://test.supabase.co/storage/v1/object/public/wardrobe-items/");
        assertThat(url).endsWith(key);
    }
}
