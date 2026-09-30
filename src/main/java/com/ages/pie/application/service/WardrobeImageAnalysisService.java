package com.ages.pie.application.service;

import com.ages.pie.application.dto.wardrobe.WardrobeImageAnalysisDTO;
import com.ages.pie.domain.enums.ProductCategory;
import com.ages.pie.domain.enums.ProductColor;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.ai.BedrockImageAnalysisClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
public class WardrobeImageAnalysisService {

    private static final long MAX_IMAGE_BYTES = 3_932_160;
    private static final Map<String, String> FORMATS = Map.of(
            "image/jpeg", "jpeg", "image/png", "png", "image/webp", "webp");
    private final BedrockImageAnalysisClient client;

    public WardrobeImageAnalysisService(BedrockImageAnalysisClient client) {
        this.client = client;
    }

    public WardrobeImageAnalysisDTO analyze(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adicione uma foto da peça.");
        }
        String format = file.getContentType() == null ? null : FORMATS.get(file.getContentType());
        if (format == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não suportado. Envie uma foto JPEG, PNG ou WebP.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A foto deve ter no máximo 3,75 MB para análise. Envie outra foto.");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível ler a foto. Envie outra foto.");
        }
        WardrobeImageAnalysisDTO result = client.analyze(bytes, format);
        if (result == null) {
            throw invalidResponse();
        }
        if (result.category() == null && result.style() == null && result.color() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não identificamos uma peça na imagem. Envie outra foto com a peça visível.");
        }
        if (!ProductCategory.isValid(result.category()) || !ProductStyle.isValid(result.style())
                || !ProductColor.isValid(result.color())) {
            throw invalidResponse();
        }
        return result;
    }

    private ResponseStatusException invalidResponse() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Não foi possível identificar os dados da peça. Envie outra foto ou preencha os campos manualmente.");
    }
}
