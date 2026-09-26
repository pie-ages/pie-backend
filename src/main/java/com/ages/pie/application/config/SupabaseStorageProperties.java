package com.ages.pie.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "supabase.storage")
public class SupabaseStorageProperties {

    private String url = "";
    private String serviceRoleKey = "";
    private String bucket = "product-images";
    private String wardrobeBucket = "wardrobe-items";
    private int maxFileSizeMb = 5;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getServiceRoleKey() { return serviceRoleKey; }
    public void setServiceRoleKey(String serviceRoleKey) { this.serviceRoleKey = serviceRoleKey; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getWardrobeBucket() { return wardrobeBucket; }
    public void setWardrobeBucket(String wardrobeBucket) { this.wardrobeBucket = wardrobeBucket; }

    public int getMaxFileSizeMb() { return maxFileSizeMb; }
    public void setMaxFileSizeMb(int maxFileSizeMb) { this.maxFileSizeMb = maxFileSizeMb; }
}
