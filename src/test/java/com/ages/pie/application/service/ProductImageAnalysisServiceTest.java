package com.ages.pie.application.service;

import com.ages.pie.application.dto.wardrobe.WardrobeImageAnalysisDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductImageAnalysisServiceTest {

    private final WardrobeImageAnalysisService wardrobeImageAnalysisService = mock(WardrobeImageAnalysisService.class);
    private final ProductImageAnalysisService service = new ProductImageAnalysisService(wardrobeImageAnalysisService);
    private final MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[]{1});

    @Test
    void analyze_shouldReturnResult() {
        WardrobeImageAnalysisDTO analysis = new WardrobeImageAnalysisDTO("vestido", "romantico", "verde");
        when(wardrobeImageAnalysisService.analyze(file)).thenReturn(analysis);

        assertThat(service.analyze(file)).contains(analysis);
    }

    @Test
    void analyze_shouldNormalizeIdsToLowercase() {
        when(wardrobeImageAnalysisService.analyze(file))
                .thenReturn(new WardrobeImageAnalysisDTO("Vestido", "ROMANTICO", "Off-White"));

        assertThat(service.analyze(file))
                .contains(new WardrobeImageAnalysisDTO("vestido", "romantico", "off-white"));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 422, 502, 503})
    void analyze_whenAnalysisFails_shouldReturnEmpty(int status) {
        when(wardrobeImageAnalysisService.analyze(file))
                .thenThrow(new ResponseStatusException(HttpStatus.valueOf(status)));

        assertThat(service.analyze(file)).isEmpty();
    }

    @Test
    void analyze_whenUnexpectedError_shouldReturnEmpty() {
        when(wardrobeImageAnalysisService.analyze(file)).thenThrow(new IllegalStateException("boom"));

        assertThat(service.analyze(file)).isEmpty();
    }
}
