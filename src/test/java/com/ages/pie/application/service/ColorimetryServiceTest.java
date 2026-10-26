package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.ColorimetryAnalysisDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.domain.service.SeasonCatalog;
import com.ages.pie.infrastructure.ai.BedrockImageAnalysisClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ColorimetryServiceTest {
    private final SeasonCatalog catalog = new SeasonCatalog();
    private final UUID userId = UUID.randomUUID();
    private BedrockImageAnalysisClient client;
    private PreferenceService preferenceService;
    private ColorimetryRateLimiter rateLimiter;
    private ColorimetryService service;

    @BeforeEach
    void setUp() {
        client = mock(BedrockImageAnalysisClient.class);
        preferenceService = mock(PreferenceService.class);
        rateLimiter = new ColorimetryRateLimiter();
        service = new ColorimetryService(client, catalog, preferenceService, rateLimiter);
    }

    @Test
    void savesTheSeasonMatchingTheClassifiedAxes() throws IOException {
        var expected = new PreferencesResponseDTO(List.of("#FFB58C"), List.of("#00C4A1"), List.of(), null);
        when(client.analyzeColorimetry(any(), eq("jpeg"))).thenReturn(new ColorimetryAnalysisDTO(true, "warm", "medium", "soft"));
        when(preferenceService.saveSeason(userId, catalog.findById("soft_autumn").orElseThrow())).thenReturn(expected);

        assertThat(service.analyze(userId, photo("image/png", 40, 30))).isEqualTo(expected);
    }

    @Test
    void normalizesAxesCasingAndWhitespaceFromTheModel() throws IOException {
        var expected = new PreferencesResponseDTO(List.of("#FFB58C"), List.of("#00C4A1"), List.of(), null);
        when(client.analyzeColorimetry(any(), eq("jpeg"))).thenReturn(new ColorimetryAnalysisDTO(true, "Warm", " MEDIUM ", "Soft"));
        when(preferenceService.saveSeason(userId, catalog.findById("soft_autumn").orElseThrow())).thenReturn(expected);

        assertThat(service.analyze(userId, photo("image/png", 40, 30))).isEqualTo(expected);
    }

    @Test
    void acceptsTheNonStandardJpgContentType() throws IOException {
        when(client.analyzeColorimetry(any(), eq("jpeg"))).thenReturn(new ColorimetryAnalysisDTO(true, "cool", "light", "soft"));

        service.analyze(userId, photo("image/jpg", 40, 30));

        verify(preferenceService).saveSeason(userId, catalog.findByAxes("cool", "light", "soft"));
    }

    @Test
    void stopsCallingTheModelOnceTheHourlyLimitIsReached() throws IOException {
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(true, "cool", "deep", "bright"));
        for (int i = 0; i < ColorimetryRateLimiter.MAX_ANALYSES; i++) {
            service.analyze(userId, photo("image/png", 10, 10));
        }

        assertStatus(photo("image/png", 10, 10), HttpStatus.TOO_MANY_REQUESTS);
        verify(client, times(ColorimetryRateLimiter.MAX_ANALYSES)).analyzeColorimetry(any(), any());
    }

    @Test
    void rejectedUploadsDoNotConsumeTheHourlyLimit() throws IOException {
        for (int i = 0; i < ColorimetryRateLimiter.MAX_ANALYSES; i++) {
            assertStatus(new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[10]), HttpStatus.BAD_REQUEST);
        }
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(true, "cool", "deep", "bright"));

        service.analyze(userId, photo("image/png", 10, 10));

        verify(client).analyzeColorimetry(any(), any());
    }

    @Test
    void downscalesLargePhotosBeforeInference() throws IOException {
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(true, "cool", "deep", "bright"));

        service.analyze(userId, photo("image/jpeg", 2000, 1500));

        ArgumentCaptor<byte[]> sent = ArgumentCaptor.forClass(byte[].class);
        verify(client).analyzeColorimetry(sent.capture(), eq("jpeg"));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(sent.getValue()));
        assertThat(image.getWidth()).isEqualTo(ColorimetryService.MAX_IMAGE_SIDE);
        assertThat(image.getHeight()).isEqualTo(384);
    }

    @Test
    void rejectsPhotosDeclaringMorePixelsThanTheCapWithoutDecodingThem() throws IOException {
        byte[] png = photo("image/png", 1, 1).getBytes();
        // Rewrite the IHDR size to 10000x10000 (100 MP); the pixel data stays a single pixel.
        ByteBuffer.wrap(png, 16, 8).putInt(10_000).putInt(10_000);
        CRC32 crc = new CRC32();
        crc.update(png, 12, 17);
        ByteBuffer.wrap(png, 29, 4).putInt((int) crc.getValue());

        assertThatThrownBy(() -> service.analyze(userId, new MockMultipartFile("file", "bomb.png", "image/png", png)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("10000x10000")
                .hasMessageContaining("50 megapixels");
        verifyNoInteractions(client);
    }

    @Test
    void rejectsEmptyUnsupportedAndUnreadablePhotosBeforeInference() {
        assertStatus(new MockMultipartFile("file", "a.png", "image/png", new byte[0]), HttpStatus.BAD_REQUEST);
        assertStatus(new MockMultipartFile("file", "a.webp", "image/webp", new byte[10]), HttpStatus.BAD_REQUEST);
        assertStatus(new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[10]), HttpStatus.BAD_REQUEST);
        verifyNoInteractions(client, preferenceService);
    }

    @Test
    void asksForAnotherPhotoWhenNoFaceIsVisible() throws IOException {
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(false, null, null, null));
        assertStatus(photo("image/png", 10, 10), HttpStatus.UNPROCESSABLE_ENTITY);
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(false, "warm", "deep", "soft"));
        assertStatus(photo("image/png", 10, 10), HttpStatus.UNPROCESSABLE_ENTITY);
        verifyNoInteractions(preferenceService);
    }

    @Test
    void rejectsUnknownOrPartialAxes() throws IOException {
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(true, "neutral", "medium", "soft"));
        assertStatus(photo("image/png", 10, 10), HttpStatus.BAD_GATEWAY);
        when(client.analyzeColorimetry(any(), any())).thenReturn(new ColorimetryAnalysisDTO(true, "warm", null, "soft"));
        assertStatus(photo("image/png", 10, 10), HttpStatus.BAD_GATEWAY);
        verifyNoInteractions(preferenceService);
    }

    private MockMultipartFile photo(String contentType, int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB),
                contentType.equals("image/png") ? "png" : "jpeg", out);
        return new MockMultipartFile("file", "face", contentType, out.toByteArray());
    }

    private void assertStatus(MockMultipartFile file, HttpStatus status) {
        assertThatThrownBy(() -> service.analyze(userId, file)).isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(status));
    }
}
