package com.ages.pie.application.controller;

import java.util.UUID;

import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookPageDTO;
import com.ages.pie.application.dto.look.LookRequestDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.dto.look.LookSuggestionDTO;
import com.ages.pie.application.dto.look.LookUpdateDTO;
import com.ages.pie.application.service.LookService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users/me/looks")
@SecurityRequirement(name = "bearerAuth")
public class LookController {

    private final LookService lookService;
    private final AuthenticatedUserProvider authenticatedUser;

    public LookController(LookService lookService, AuthenticatedUserProvider authenticatedUser) {
        this.lookService = lookService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public ResponseEntity<LookPageDTO> findAll(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(lookService.findAllByUser(authenticatedUser.id(), pageable));
    }

    @PostMapping
    public ResponseEntity<LookResponseDTO> create(@Valid @RequestBody LookRequestDTO dto) {
        LookResponseDTO look = lookService.create(authenticatedUser.id(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(look);
    }

    @GetMapping("/suggestion")
    public ResponseEntity<LookSuggestionDTO> suggestion() {
        return ResponseEntity.ok(lookService.suggestion(authenticatedUser.id()));
    }

    @GetMapping("/{lookId}")
    public ResponseEntity<LookResponseDTO> findById(@PathVariable UUID lookId) {
        return ResponseEntity.ok(lookService.findById(authenticatedUser.id(), lookId));
    }

    @PutMapping("/{lookId}")
    public ResponseEntity<LookResponseDTO> update(@PathVariable UUID lookId,
            @Valid @RequestBody LookUpdateDTO dto) {
        return ResponseEntity.ok(lookService.update(authenticatedUser.id(), lookId, dto));
    }

    @DeleteMapping("/{lookId}")
    public ResponseEntity<Void> delete(@PathVariable UUID lookId) {
        lookService.delete(authenticatedUser.id(), lookId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{lookId}/image", consumes = "multipart/form-data")
    public ResponseEntity<LookResponseDTO> updatePhoto(@PathVariable UUID lookId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(lookService.updatePhoto(authenticatedUser.id(), lookId, file));
    }

    @DeleteMapping("/{lookId}/image")
    public ResponseEntity<Void> removePhoto(@PathVariable UUID lookId) {
        lookService.removePhoto(authenticatedUser.id(), lookId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lookId}/wardrobe-items/{wardrobeItemId}")
    public ResponseEntity<LookItemDTO> addWardrobeItem(@PathVariable UUID lookId,
            @PathVariable UUID wardrobeItemId) {
        LookItemDTO item = lookService.addWardrobeItem(authenticatedUser.id(), lookId, wardrobeItemId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/{lookId}/wardrobe-items/{wardrobeItemId}")
    public ResponseEntity<Void> removeWardrobeItem(@PathVariable UUID lookId,
            @PathVariable UUID wardrobeItemId) {
        lookService.removeWardrobeItem(authenticatedUser.id(), lookId, wardrobeItemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lookId}/products/{productId}")
    public ResponseEntity<LookItemDTO> addProduct(@PathVariable UUID lookId,
            @PathVariable UUID productId) {
        LookItemDTO item = lookService.addProduct(authenticatedUser.id(), lookId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/{lookId}/products/{productId}")
    public ResponseEntity<Void> removeProduct(@PathVariable UUID lookId,
            @PathVariable UUID productId) {
        lookService.removeProduct(authenticatedUser.id(), lookId, productId);
        return ResponseEntity.noContent().build();
    }
}
