package com.ages.pie.application.controller;

import com.ages.pie.application.dto.company.AdminRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.service.CompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final CompanyService companyService;

    public AdminController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponseDTO> create(@Valid @RequestBody AdminRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.createAdmin(dto));
    }
}
