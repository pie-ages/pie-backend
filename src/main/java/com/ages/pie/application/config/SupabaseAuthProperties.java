package com.ages.pie.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "supabase.auth")
public class SupabaseAuthProperties {

    private String url = "";
    private String serviceRoleKey = "";

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getServiceRoleKey() { return serviceRoleKey; }
    public void setServiceRoleKey(String serviceRoleKey) { this.serviceRoleKey = serviceRoleKey; }
}
