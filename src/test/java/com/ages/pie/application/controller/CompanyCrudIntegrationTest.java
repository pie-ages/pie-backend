package com.ages.pie.application.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
class CompanyCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final String cnpj = Long.toString(Math.floorMod(System.nanoTime(), 100_000_000_000_000L));
    private final String email = "empresa-" + UUID.randomUUID() + "@teste.com";

    private String payload() {
        return """
            {"name":"Loja X","cnpj":"%s","socialReason":"Loja X LTDA",
             "responsiblePerson":"Maria","email":"%s","password":"senha123",
             "website":"https://lojax.com"}
            """.formatted(cnpj, email);
    }

    @Test
    void criaEConsultaEmpresa() throws Exception {
        String id = idDe(mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON).content(payload()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.active").value(true))
            .andReturn());

        mockMvc.perform(get("/api/companies/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Loja X"))
            .andExpect(jsonPath("$.cnpj").value(cnpj));
    }

    @Test
    void rejeitaCnpjDuplicado() throws Exception {
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON).content(payload()))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON).content(payload()))
            .andExpect(status().isConflict());
    }

    @Test
    void empresaDesativadaSaiDaListagemMasContinuaConsultavel() throws Exception {
        String id = idDe(mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON).content(payload()))
            .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(delete("/api/companies/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/companies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).isEmpty());

        mockMvc.perform(get("/api/companies/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void atualizaEmpresa() throws Exception {
        String novoEmail = "renomeada-" + UUID.randomUUID() + "@teste.com";
        String id = idDe(mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON).content(payload()))
            .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(put("/api/companies/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Loja Renomeada","socialReason":"Loja X LTDA",
                     "responsiblePerson":"Joao","email":"%s"}
                    """.formatted(novoEmail)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Loja Renomeada"));

        mockMvc.perform(get("/api/companies/{id}", id))
            .andExpect(jsonPath("$.email").value(novoEmail))
            .andExpect(jsonPath("$.responsiblePerson").value("Joao"));
    }

    private String idDe(MvcResult result) {
        String location = result.getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
