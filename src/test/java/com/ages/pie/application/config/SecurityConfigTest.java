package com.ages.pie.application.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.controller.AuthController;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.service.AuthService;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Valida a cadeia de segurança real (SecurityConfig) com tokens assinados
 * por uma chave EC local, simulando o JWKS do Supabase.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class, AuthenticatedUserProvider.class,
    SecurityConfigTest.LocalJwksConfig.class})
@TestPropertySource(properties = "supabase.auth.url=" + SecurityConfigTest.SUPABASE_URL)
class SecurityConfigTest {

    static final String SUPABASE_URL = "https://proj.supabase.co";
    static final String ISSUER = SUPABASE_URL + "/auth/v1";

    static final ECKey SIGNING_KEY = generateKey();
    static final ECKey FOREIGN_KEY = generateKey();

    @TestConfiguration
    static class LocalJwksConfig {
        @Bean
        @Primary
        JwtDecoder localJwtDecoder(SupabaseAuthProperties props) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSource(new ImmutableJWKSet<>(new JWKSet(SIGNING_KEY.toPublicJWK())))
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();
            decoder.setJwtValidator(SecurityConfig.jwtValidator(props.getIssuer(), props.getAudience()));
            return decoder;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CompanyRepository companyRepository;

    private static ECKey generateKey() {
        try {
            return new ECKeyGenerator(Curve.P_256).keyID(UUID.randomUUID().toString()).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JWTClaimsSet.Builder validClaims(UUID subject) {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
            .subject(subject.toString())
            .issuer(ISSUER)
            .audience(List.of("authenticated"))
            .claim("role", "authenticated")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(3600)));
    }

    private static String sign(JWTClaimsSet claims, ECKey key) throws JOSEException {
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(key.getKeyID()).build(), claims);
        jwt.sign(new ECDSASigner(key));
        return jwt.serialize();
    }

    private void expectUnauthorized(String token) throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegidoSemTokenRetorna401ComErrorResponse() throws Exception {
        mockMvc.perform(get("/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Usuário não autenticado"))
            .andExpect(jsonPath("$.path").value("/auth/me"));
    }

    @Test
    void tokenValidoResolveOCustomerPeloSub() throws Exception {
        UUID supabaseUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User("Ana", "ana@email.com", supabaseUserId);
        ReflectionTestUtils.setField(user, "id", userId);
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(user));
        when(authService.findSummary(userId)).thenReturn(new UserSummaryDTO(userId, "Ana", "ana@email.com", null, null, null));

        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + sign(validClaims(supabaseUserId).build(), SIGNING_KEY)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void tokenMalformadoRetorna401() throws Exception {
        expectUnauthorized("nao-e-um-jwt");
    }

    @Test
    void tokenAssinadoPorOutraChaveRetorna401() throws Exception {
        expectUnauthorized(sign(validClaims(UUID.randomUUID()).build(), FOREIGN_KEY));
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        String token = sign(validClaims(UUID.randomUUID()).build(), SIGNING_KEY);
        String[] parts = token.split("\\.");
        String outroPayload = sign(validClaims(UUID.randomUUID()).build(), SIGNING_KEY).split("\\.")[1];
        expectUnauthorized(parts[0] + "." + outroPayload + "." + parts[2]);
    }

    @Test
    void tokenExpiradoRetorna401() throws Exception {
        Instant past = Instant.now().minusSeconds(7200);
        expectUnauthorized(sign(validClaims(UUID.randomUUID())
            .issueTime(Date.from(past))
            .expirationTime(Date.from(past.plusSeconds(3600)))
            .build(), SIGNING_KEY));
    }

    @Test
    void tokenDeOutroIssuerRetorna401() throws Exception {
        expectUnauthorized(sign(validClaims(UUID.randomUUID())
            .issuer("https://outro.supabase.co/auth/v1").build(), SIGNING_KEY));
    }

    @Test
    void tokenComAudienceErradaRetorna401() throws Exception {
        expectUnauthorized(sign(validClaims(UUID.randomUUID())
            .audience(List.of("anon")).build(), SIGNING_KEY));
    }

    @Test
    void loginEPublico() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponseDTO("acc", "ref", 3600, "bearer", null));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@email.com\",\"password\":\"senha\"}"))
            .andExpect(status().isOk());
    }
}
