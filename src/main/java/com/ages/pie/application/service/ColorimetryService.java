package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.ColorimetryAnalysisDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.domain.service.SeasonCatalog;
import com.ages.pie.domain.service.SeasonCatalog.Season;
import com.ages.pie.infrastructure.ai.BedrockImageAnalysisClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Classifies the user's seasonal coloring from a face photo. The photo is only held in memory for the
 * request and is never stored; only the resulting season id is persisted.
 */
@Service
public class ColorimetryService {

    // Skin undertone survives downscaling; at 512px the image costs a fraction of the tokens of a full photo.
    static final int MAX_IMAGE_SIDE = 512;
    // Phone cameras top out around 48 MP; anything larger is not a selfie and only costs decode time.
    static final long MAX_SOURCE_PIXELS = 50_000_000L;
    // Some Android pickers report JPEGs with the non-standard "image/jpg" type.
    private static final Set<String> FORMATS = Set.of("image/jpeg", "image/jpg", "image/png");

    private final BedrockImageAnalysisClient client;
    private final SeasonCatalog seasonCatalog;
    private final PreferenceService preferenceService;
    private final ColorimetryRateLimiter rateLimiter;

    public ColorimetryService(BedrockImageAnalysisClient client, SeasonCatalog seasonCatalog,
                              PreferenceService preferenceService, ColorimetryRateLimiter rateLimiter) {
        this.client = client;
        this.seasonCatalog = seasonCatalog;
        this.preferenceService = preferenceService;
        this.rateLimiter = rateLimiter;
    }

    public PreferencesResponseDTO analyze(UUID userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tire uma foto do seu rosto para a colorimetria.");
        }
        if (file.getContentType() == null || !FORMATS.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não suportado: " + file.getContentType() + ". Envie uma foto JPEG ou PNG.");
        }
        byte[] image = downscale(file);
        rateLimiter.acquire(userId);
        ColorimetryAnalysisDTO result = client.analyzeColorimetry(image, "jpeg");
        if (result == null) {
            throw invalidResponse();
        }
        if (Boolean.FALSE.equals(result.face())
                || (result.temperature() == null && result.depth() == null && result.chroma() == null)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não identificamos um rosto na foto. Tire outra foto de frente, com o rosto visível.");
        }
        Season season = seasonCatalog.findByAxes(
                normalize(result.temperature()), normalize(result.depth()), normalize(result.chroma()));
        if (season == null) {
            throw invalidResponse();
        }
        return preferenceService.saveSeason(userId, season);
    }

    // The model may vary casing or pad values even at temperature 0; the catalog keys are lowercase.
    private static String normalize(String axis) {
        return axis == null ? null : axis.trim().toLowerCase(Locale.ROOT);
    }

    private byte[] downscale(MultipartFile file) {
        BufferedImage source;
        try (ImageInputStream in = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = in == null ? null : ImageIO.getImageReaders(in);
            if (readers == null || !readers.hasNext()) {
                throw unreadablePhoto();
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if ((long) width * height > MAX_SOURCE_PIXELS) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A foto tem " + width + "x" + height
                            + " pixels; o máximo é " + MAX_SOURCE_PIXELS / 1_000_000 + " megapixels. Tire outra foto.");
                }
                // Subsampling decodes straight to a small raster, so the full-size image is never allocated.
                int step = Math.max(1, Math.max(width, height) / MAX_IMAGE_SIDE);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(step, step, 0, 0);
                source = reader.read(0, param);
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof ResponseStatusException status) {
                throw status;
            }
            throw unreadablePhoto();
        }
        double scale = Math.min(1.0, (double) MAX_IMAGE_SIDE / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        // JPEG has no alpha channel, so PNGs are flattened onto an RGB canvas.
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(target, "jpeg", out);
        } catch (IOException e) {
            throw unreadablePhoto();
        }
        return out.toByteArray();
    }

    private ResponseStatusException unreadablePhoto() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler a foto. Tire outra foto.");
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Não foi possível identificar sua colorimetria. Tire outra foto em um local bem iluminado.");
    }
}
