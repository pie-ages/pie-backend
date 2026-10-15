package com.ages.pie.application.service;

import com.ages.pie.application.dto.wardrobe.WardrobeImageAnalysisDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;

@Service
public class ProductImageAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageAnalysisService.class);

    private final WardrobeImageAnalysisService wardrobeImageAnalysisService;

    public ProductImageAnalysisService(WardrobeImageAnalysisService wardrobeImageAnalysisService) {
        this.wardrobeImageAnalysisService = wardrobeImageAnalysisService;
    }

    public Optional<WardrobeImageAnalysisDTO> analyze(MultipartFile file) {
        try {
            WardrobeImageAnalysisDTO result = wardrobeImageAnalysisService.analyze(file);
            return Optional.of(new WardrobeImageAnalysisDTO(
                    normalize(result.category()), normalize(result.style()), normalize(result.color())));
        } catch (ResponseStatusException e) {
            logger.warn("Análise da imagem do produto ignorada: status={}", e.getStatusCode().value());
        } catch (RuntimeException e) {
            logger.warn("Análise da imagem do produto ignorada por erro inesperado", e);
        }
        return Optional.empty();
    }

    private static String normalize(String id) {
        return id == null ? null : id.trim().toLowerCase(Locale.ROOT);
    }
}
