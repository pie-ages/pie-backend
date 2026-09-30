package com.ages.pie.infrastructure.storage;

import com.ages.pie.application.config.SupabaseStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class StorageBucketInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StorageBucketInitializer.class);

    private final SupabaseStorageProperties props;
    private final RestClient restClient;

    @Autowired
    public StorageBucketInitializer(SupabaseStorageProperties props) {
        this(props, RestClient.create());
    }

    StorageBucketInitializer(SupabaseStorageProperties props, RestClient restClient) {
        this.props = props;
        this.restClient = restClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (props.getUrl().isBlank() || props.getServiceRoleKey().isBlank()) {
            logger.warn("Supabase Storage não configurado — buckets não serão criados.");
            return;
        }

        List<BucketSpec> buckets = List.of(
                new BucketSpec(props.getBucket(), true),
                new BucketSpec(props.getWardrobeBucket(), true),
                new BucketSpec(props.getLookBucket(), true)
        );

        for (BucketSpec bucket : buckets) {
            ensureBucketExists(bucket);
        }
    }

    private void ensureBucketExists(BucketSpec bucket) {
        String url = props.getUrl() + "/storage/v1/bucket";
        try {
            restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + props.getServiceRoleKey())
                    .header("apikey", props.getServiceRoleKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("id", bucket.id(), "name", bucket.id(), "public", bucket.isPublic()))
                    .retrieve()
                    .toBodilessEntity();
            logger.info("Bucket '{}' criado no Supabase Storage.", bucket.id());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                logger.debug("Bucket '{}' já existe.", bucket.id());
            } else {
                logger.warn("Falha ao criar bucket '{}': {} {}", bucket.id(), e.getStatusCode(), e.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Falha ao criar bucket '{}': {}", bucket.id(), e.getMessage());
        }
    }

    private record BucketSpec(String id, boolean isPublic) {}
}
