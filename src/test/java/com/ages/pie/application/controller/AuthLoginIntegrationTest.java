package com.ages.pie.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.security.JwtTokenProvider;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void seedUsuario() {
        userRepository.save(new User(
            "Usuário Teste", "login-" + UUID.randomUUID() + "@teste.com", "hash(x)"));
    }

    @Test
    void loginRetornaTokenVerificavelParaUmUsuarioReal() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"qualquer@teste.com\",\"password\":\"qualquer\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.id").isNotEmpty())
            .andExpect(jsonPath("$.user.email").isNotEmpty())
            .andReturn();

        String json = result.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");
        String userId = JsonPath.read(json, "$.user.id");

        assertThat(jwtTokenProvider.validarToken(token)).isTrue();
        assertThat(jwtTokenProvider.extrairUserId(token)).hasToString(userId);
        assertThat(userRepository.findById(UUID.fromString(userId))).isPresent();
    }

    @Test
    void loginNaoExigeCredenciais() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
