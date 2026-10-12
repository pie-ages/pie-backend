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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static final Set<String> FORMATS = Set.of("image/jpeg", "image/png");

    private final BedrockImageAnalysisClient client;
    private final SeasonCatalog seasonCatalog;
    private final PreferenceService preferenceService;

    public ColorimetryService(BedrockImageAnalysisClient client, SeasonCatalog seasonCatalog,
                              PreferenceService preferenceService) {
        this.client = client;
        this.seasonCatalog = seasonCatalog;
        this.preferenceService = preferenceService;
    }

    public PreferencesResponseDTO analyze(UUID userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tire uma foto do seu rosto para a colorimetria.");
        }
        if (file.getContentType() == null || !FORMATS.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não suportado: " + file.getContentType() + ". Envie uma foto JPEG ou PNG.");
        }
        ColorimetryAnalysisDTO result = client.analyzeColorimetry(downscale(file), "jpeg");
        if (result == null) {
            throw invalidResponse();
        }
        if (Boolean.FALSE.equals(result.face())
                || (result.temperature() == null && result.depth() == null && result.chroma() == null)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não identificamos um rosto na foto. Tire outra foto de frente, com o rosto visível.");
        }
        Season season = seasonCatalog.findByAxes(result.temperature(), result.depth(), result.chroma());
        if (season == null) {
            throw invalidResponse();
        }
        return preferenceService.saveSeason(userId, season);
    }

    private byte[] downscale(MultipartFile file) {
        BufferedImage source;
        try (InputStream in = file.getInputStream()) {
            source = ImageIO.read(in);
        } catch (IOException e) {
            source = null;
        }
        if (source == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível ler a foto. Tire outra foto.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler a foto. Tire outra foto.");
        }
        return out.toByteArray();
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Não foi possível identificar sua colorimetria. Tire outra foto em um local bem iluminado.");
    }
}
