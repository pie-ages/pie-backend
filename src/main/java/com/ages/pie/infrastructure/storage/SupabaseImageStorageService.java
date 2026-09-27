package com.ages.pie.infrastructure.storage;

import com.ages.pie.application.config.SupabaseStorageProperties;
import com.ages.pie.application.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    static final String PRODUCTS_FOLDER = "products";
    static final String WARDROBE_FOLDER = "wardrobe";
    static final String LOOKS_FOLDER = "looks";

    static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final RestClient restClient;
    private final SupabaseStorageProperties props;
    private final long maxFileSizeBytes;

    @Autowired
    public SupabaseImageStorageService(SupabaseStorageProperties props) {
        this(props, RestClient.create());
    }

    SupabaseImageStorageService(SupabaseStorageProperties props, RestClient restClient) {
        this.props = props;
        this.restClient = restClient;
        this.maxFileSizeBytes = (long) props.getMaxFileSizeMb() * 1024 * 1024;
    }

    @Override
    public String upload(MultipartFile file, UUID productId) {
        return uploadToPath(file, props.getBucket(), PRODUCTS_FOLDER, productId);
    }

    @Override
    public String uploadForWardrobe(MultipartFile file, UUID wardrobeItemId) {
        return uploadToPath(file, props.getWardrobeBucket(), WARDROBE_FOLDER, wardrobeItemId);
    }

    @Override
    public String uploadForLook(MultipartFile file, UUID lookId) {
        return uploadToPath(file, props.getLookBucket(), LOOKS_FOLDER, lookId);
    }

    @Override
    public void delete(String storageKey) {
        String bucketForKey = getBucketForKey(storageKey);
        String deleteUrl = props.getUrl() + "/storage/v1/object/" + bucketForKey;
        logger.info("Deletando imagem do storage: {}", storageKey);

        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + props.getServiceRoleKey())
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
        return props.getUrl() + "/storage/v1/object/public/" + getBucketForKey(storageKey) + "/" + storageKey;
    }

    String getBucketForKey(String storageKey) {
        if (storageKey.startsWith(WARDROBE_FOLDER + "/")) {
            return props.getWardrobeBucket();
        }
        if (storageKey.startsWith(LOOKS_FOLDER + "/")) {
            return props.getLookBucket();
        }
        return props.getBucket();
    }

    private String uploadToPath(MultipartFile file, String bucket, String folder, UUID entityId) {
        validateFile(file);

        String contentType = file.getContentType();
        String ext = CONTENT_TYPE_TO_EXT.get(contentType);
        String storageKey = folder + "/" + entityId + "/" + UUID.randomUUID() + "." + ext;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            logger.error("Falha ao ler bytes do arquivo para {} em {}", entityId, folder, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao armazenar a imagem. Tente novamente.");
        }

        String uploadUrl = props.getUrl() + "/storage/v1/object/" + bucket + "/" + storageKey;
        logger.info("Fazendo upload de imagem: {}", storageKey);

        try {
            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + props.getServiceRoleKey())
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

    void validateFile(MultipartFile file) {
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
