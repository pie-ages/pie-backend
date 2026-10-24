package com.ages.pie.application.controller;

import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.company.CompanyRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.dto.company.CompanyUpdateDTO;
import com.ages.pie.application.dto.company.UpdateStatusDTO;
import com.ages.pie.application.service.CompanyService;
import com.ages.pie.domain.enums.CompanyStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponseDTO> create(@Valid @RequestBody CompanyRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponseDTO>> findAll(
            @RequestParam(required = false) CompanyStatus status) {
        if (status != null) {
            return ResponseEntity.ok(companyService.findAllByStatus(status));
        }
        return ResponseEntity.ok(companyService.findAllAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody CompanyUpdateDTO dto) {
        return ResponseEntity.ok(companyService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CompanyResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusDTO dto) {
        return ResponseEntity.ok(companyService.updateStatus(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
