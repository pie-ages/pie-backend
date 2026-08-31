package com.ages.pie.application.controller;

import com.ages.pie.application.dto.ProductRequestDTO;
import com.ages.pie.application.dto.ProductResponseDTO;
import com.ages.pie.application.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> criar(@Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductResponseDTO response = productService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listarTodos() {
        return ResponseEntity.ok(productService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> atualizar(@PathVariable UUID id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.ok(productService.atualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        productService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
