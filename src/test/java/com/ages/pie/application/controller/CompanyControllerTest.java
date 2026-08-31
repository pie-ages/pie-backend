package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.CompanyResponseDTO;
import com.ages.pie.application.exception.DuplicateResourceException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    private CompanyResponseDTO response(UUID id) {
        return new CompanyResponseDTO(id, "Loja X", "12345678000199", "Loja X LTDA",
            "Maria", "contato@lojax.com", "https://lojax.com", true, null, null, null);
    }

    @Test
    void criaEmpresaRetorna201ComLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyService.criar(any())).thenReturn(response(id));

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Loja X","cnpj":"12345678000199","socialReason":"Loja X LTDA",
                     "responsiblePerson":"Maria","email":"contato@lojax.com","password":"senha123"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/companies/" + id))
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Loja X"));
    }

    @Test
    void criaEmpresaComCnpjDuplicadoRetorna409() throws Exception {
        when(companyService.criar(any()))
            .thenThrow(new DuplicateResourceException("cnpj duplicado"));

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Loja X\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void criaEmpresaInvalidaRetorna400() throws Exception {
        when(companyService.criar(any()))
            .thenThrow(new IllegalArgumentException("nome obrigatorio"));

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void listaEmpresasRetorna200() throws Exception {
        when(companyService.listar()).thenReturn(List.of(response(UUID.randomUUID())));

        mockMvc.perform(get("/api/companies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Loja X"));
    }

    @Test
    void buscaPorIdRetorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyService.buscarPorId(id)).thenReturn(response(id));

        mockMvc.perform(get("/api/companies/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void buscaPorIdInexistenteRetorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyService.buscarPorId(id))
            .thenThrow(new ResourceNotFoundException("nao encontrada"));

        mockMvc.perform(get("/api/companies/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void atualizaEmpresaRetorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyService.atualizar(eq(id), any())).thenReturn(response(id));

        mockMvc.perform(put("/api/companies/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Loja X","socialReason":"Loja X LTDA",
                     "responsiblePerson":"Maria","email":"contato@lojax.com"}
                    """))
            .andExpect(status().isOk());
    }

    @Test
    void atualizaEmpresaInexistenteRetorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyService.atualizar(eq(id), any()))
            .thenThrow(new ResourceNotFoundException("nao encontrada"));

        mockMvc.perform(put("/api/companies/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Loja X\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void desativaEmpresaRetorna204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/companies/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    void desativaEmpresaInexistenteRetorna404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("nao encontrada"))
            .when(companyService).desativar(id);

        mockMvc.perform(delete("/api/companies/{id}", id))
            .andExpect(status().isNotFound());
    }
}
