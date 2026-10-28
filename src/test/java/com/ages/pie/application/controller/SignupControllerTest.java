package com.ages.pie.application.controller;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.dto.user.UserResponseDTO;
import com.ages.pie.application.service.ColorimetryService;
import com.ages.pie.application.service.CompanyService;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.StyleIdentificationService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, CompanyController.class})
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
@WithAnonymousUser
class SignupControllerTest {

    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private UserService userService;
    @MockitoBean private CompanyService companyService;
    @MockitoBean private PreferenceService preferenceService;
    @MockitoBean private ColorimetryService colorimetryService;
    @MockitoBean private StyleIdentificationService styleIdentificationService;
    @MockitoBean private AuthenticatedUserProvider authProvider;

    @Autowired private MockMvc mockMvc;

    @Test
    void cadastroDeClienteNaoExigeToken() throws Exception {
        when(userService.create(any())).thenReturn(
                new UserResponseDTO(UUID.randomUUID(), "Ana", "ana@email.com", null, null, null));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@email.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastroDeEmpresaNaoExigeToken() throws Exception {
        when(companyService.create(any())).thenReturn(
                new CompanyResponseDTO(UUID.randomUUID(), "Loja", "12345678000199", "Loja LTDA",
                        "Ana", "loja@email.com", null, true, null, null, null, null, null));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Loja\",\"cnpj\":\"12345678000199\",\"email\":\"loja@email.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isCreated());
    }
}
