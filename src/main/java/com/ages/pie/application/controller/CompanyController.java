package com.ages.pie.application.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.CompanyRequestDTO;
import com.ages.pie.application.dto.CompanyResponseDTO;
import com.ages.pie.application.dto.CompanyUpdateDTO;
import com.ages.pie.application.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponseDTO> criar(@RequestBody CompanyRequestDTO requestDTO) {
        CompanyResponseDTO response = companyService.criar(requestDTO);
        return ResponseEntity.created(URI.create("/api/companies/" + response.id())).body(response);
    }

    @GetMapping
    public List<CompanyResponseDTO> listar() {
        return companyService.listar();
    }

    @GetMapping("/{id}")
    public CompanyResponseDTO buscarPorId(@PathVariable UUID id) {
        return companyService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CompanyResponseDTO atualizar(@PathVariable UUID id, @RequestBody CompanyUpdateDTO updateDTO) {
        return companyService.atualizar(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        companyService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
