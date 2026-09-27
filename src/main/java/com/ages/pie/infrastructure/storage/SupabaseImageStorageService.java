package com.ages.pie.infrastructure.storage;

import com.ages.pie.application.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SupabaseImageStorageService implements ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseImageStorageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final RestClient restClient;
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final long maxFileSizeBytes;

    public SupabaseImageStorageService(
            @Value("${supabase.storage.url:}") String supabaseUrl,
            @Value("${supabase.storage.service-role-key:}") String serviceRoleKey,
            @Value("${supabase.storage.bucket:product-images}") String bucket,
            @Value("${pie.storage.max-file-size-mb:5}") int maxFileSizeMb) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.bucket = bucket;
        this.maxFileSizeBytes = (long) maxFileSizeMb * 1024 * 1024;
        this.restClient = RestClient.create();
    }

    @Override
    public String upload(MultipartFile file, UUID productId) {
        validateFile(file);

        String contentType = file.getContentType();
        String ext = CONTENT_TYPE_TO_EXT.get(contentType);
        String storageKey = "products/" + productId + "/" + UUID.randomUUID() + "." + ext;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            logger.error("Falha ao ler bytes do arquivo para produto {}", productId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao armazenar a imagem. Tente novamente.");
        }

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storageKey;
        logger.info("Fazendo upload de imagem: {}", storageKey);

        try {
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            logger.error("Falha no upload para Supabase Storage: {}", storageKey, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao armazenar a imagem. Tente novamente.");
        }

        return storageKey;
    }

    @Override
    public void delete(String storageKey) {
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket;
        logger.info("Deletando imagem do storage: {}", storageKey);

        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", List.of(storageKey)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            logger.warn("Falha ao deletar imagem do storage: {}", storageKey, e);
        }
    }

    @Override
    public String toPublicUrl(String storageKey) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + storageKey;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo não pode estar vazio.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato não suportado. Use JPEG, PNG ou WebP.");
        }

        if (file.getSize() > maxFileSizeBytes) {
            long maxMb = maxFileSizeBytes / (1024 * 1024);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Arquivo excede o tamanho máximo de " + maxMb + " MB.");
        }
    }
}
