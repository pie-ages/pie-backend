package com.ages.pie.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "supabase.auth")
public class SupabaseAuthProperties {

    private String url = "";
    private String publishableKey = "";
    private String audience = "authenticated";
    private String serviceRoleKey = "";

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url != null ? url.replaceAll("/+$", "") : ""; }

    public String getPublishableKey() { return publishableKey; }
    public void setPublishableKey(String publishableKey) { this.publishableKey = publishableKey; }

    public String getServiceRoleKey() { return serviceRoleKey; }
    public void setServiceRoleKey(String serviceRoleKey) { this.serviceRoleKey = serviceRoleKey; }

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public String getIssuer() { return url + "/auth/v1"; }

    public String getJwksUri() { return getIssuer() + "/.well-known/jwks.json"; }
}
