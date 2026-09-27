package com.ages.pie.infrastructure.storage;

import com.ages.pie.application.config.SupabaseStorageProperties;
import com.ages.pie.application.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Teste de validação da infraestrutura de Object Storage com Supabase real.
 *
 * Pré-requisitos para execução:
 * 1. application-local.properties configurado com credenciais do Supabase
 * 2. Bucket "wardrobe-items" criado no Supabase Dashboard > Storage > New bucket
 * 3. Bucket "wardrobe-items" com visibilidade Public (para validar a URL pública)
 *
 * Para executar: remova o @Disabled e rode com ./mvnw -Dtest=WardrobeStorageValidationTest test
 */
@Disabled("Execução manual: requer application-local.properties e bucket wardrobe-items no Supabase")
class WardrobeStorageValidationTest {

    private ImageStorageService imageStorageService;

    @BeforeEach
    void setUp() throws IOException {
        SupabaseStorageProperties props = new SupabaseStorageProperties();

        Path propsFile = Path.of("src/main/resources/application-local.properties");
        if (Files.exists(propsFile)) {
            Properties p = new Properties();
            try (InputStream is = Files.newInputStream(propsFile)) {
                p.load(is);
            }
            props.setUrl(p.getProperty("supabase.storage.url", ""));
            props.setServiceRoleKey(p.getProperty("supabase.storage.service-role-key", ""));
            props.setBucket(p.getProperty("supabase.storage.bucket", "product-images"));
            props.setWardrobeBucket(p.getProperty("supabase.storage.wardrobe-bucket", "wardrobe-items"));
        }

        assertThat(props.getUrl()).as("supabase.storage.url deve estar configurado").isNotBlank();
        assertThat(props.getServiceRoleKey()).as("supabase.storage.service-role-key deve estar configurado").isNotBlank();

        imageStorageService = new SupabaseImageStorageService(props);
    }

    @Test
    void wardrobeBucket_upload_retornaChaveCorreta() {
        MockMultipartFile file = new MockMultipartFile(
                "test-wardrobe.jpg", "test-wardrobe.jpg", "image/jpeg",
                "fake-image-content".getBytes()
        );
        UUID testId = UUID.randomUUID();

        String key = imageStorageService.uploadForWardrobe(file, testId);

        assertThat(key)
                .startsWith(SupabaseImageStorageService.WARDROBE_FOLDER + "/")
                .contains(testId.toString())
                .endsWith(".jpg");

        // Limpa o arquivo de teste
        assertThatNoException().isThrownBy(() -> imageStorageService.delete(key));
    }

    @Test
    void wardrobeBucket_toPublicUrl_formatoCorreto() {
        MockMultipartFile file = new MockMultipartFile(
                "test-url.png", "test-url.png", "image/png",
                "fake-png".getBytes()
        );
        UUID testId = UUID.randomUUID();

        String key = imageStorageService.uploadForWardrobe(file, testId);
        String url = imageStorageService.toPublicUrl(key);

        assertThat(url)
                .contains("/storage/v1/object/public/")
                .contains("wardrobe-items")
                .endsWith(key);

        imageStorageService.delete(key);
    }

    @Test
    void wardrobeBucket_delete_naoLancaExcecao() {
        MockMultipartFile file = new MockMultipartFile(
                "test-delete.webp", "test-delete.webp", "image/webp",
                "fake-webp".getBytes()
        );
        UUID testId = UUID.randomUUID();

        String key = imageStorageService.uploadForWardrobe(file, testId);

        assertThatNoException().isThrownBy(() -> imageStorageService.delete(key));
    }
}
