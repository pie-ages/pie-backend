package com.ages.pie.application.service;

import com.ages.pie.application.dto.wardrobe.WardrobeImageAnalysisDTO;
import com.ages.pie.infrastructure.ai.BedrockImageAnalysisClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WardrobeImageAnalysisServiceTest {
    private BedrockImageAnalysisClient client;
    private WardrobeImageAnalysisService service;

    @BeforeEach
    void setUp() {
        client = mock(BedrockImageAnalysisClient.class);
        service = new WardrobeImageAnalysisService(client);
    }

    @Test
    void returnsOnlyExistingTaxonomyIds() {
        var expected = new WardrobeImageAnalysisDTO("vestido", "romantico", "verde");
        when(client.analyze(any(), eq("png"))).thenReturn(expected);
        assertThat(service.analyze(photo("image/png", 10))).isEqualTo(expected);
    }

    @Test
    void rejectsEmptyUnsupportedAndOversizedPhotosBeforeInference() {
        assertStatus(photo("image/png", 0), HttpStatus.BAD_REQUEST);
        assertStatus(photo("image/heic", 10), HttpStatus.BAD_REQUEST);
        assertStatus(photo("image/png", 3_932_161), HttpStatus.BAD_REQUEST);
        verifyNoInteractions(client);
    }

    @Test
    void rejectsUnknownOrMissingTaxonomyValues() {
        when(client.analyze(any(), any())).thenReturn(new WardrobeImageAnalysisDTO("vestido", "maxi", "verde"));
        assertStatus(photo("image/png", 10), HttpStatus.BAD_GATEWAY);
        when(client.analyze(any(), any())).thenReturn(new WardrobeImageAnalysisDTO("vestido", "casual", null));
        assertStatus(photo("image/png", 10), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void asksForAnotherPhotoWhenNoGarmentIsVisible() {
        when(client.analyze(any(), any())).thenReturn(new WardrobeImageAnalysisDTO(null, null, null));
        assertStatus(photo("image/png", 10), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private MockMultipartFile photo(String contentType, int size) {
        return new MockMultipartFile("file", "photo.png", contentType, new byte[size]);
    }

    private void assertStatus(MockMultipartFile file, HttpStatus status) {
        assertThatThrownBy(() -> service.analyze(file)).isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(status));
    }
}
