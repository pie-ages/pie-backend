package com.ages.pie.application.controller;

import com.ages.pie.application.dto.wardrobe.WardrobeItemRequestDTO;
import com.ages.pie.application.dto.wardrobe.WardrobeItemResponseDTO;
import com.ages.pie.application.dto.wardrobe.WardrobeItemUpdateDTO;
import com.ages.pie.application.service.WardrobeItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/me/wardrobe/items")
public class WardrobeItemController {

    private final WardrobeItemService wardrobeItemService;

    public WardrobeItemController(WardrobeItemService wardrobeItemService) {
        this.wardrobeItemService = wardrobeItemService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WardrobeItemResponseDTO> create(
            @Valid @RequestPart("item") WardrobeItemRequestDTO request,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wardrobeItemService.create(request, file));
    }

    @GetMapping
    public ResponseEntity<List<WardrobeItemResponseDTO>> list() {
        return ResponseEntity.ok(wardrobeItemService.list());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<WardrobeItemResponseDTO> find(@PathVariable UUID itemId) {
        return ResponseEntity.ok(wardrobeItemService.find(itemId));
    }

    @PatchMapping(value = "/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WardrobeItemResponseDTO> update(
            @PathVariable UUID itemId,
            @Valid @RequestPart("item") WardrobeItemUpdateDTO request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(wardrobeItemService.update(itemId, request, file));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable UUID itemId) {
        wardrobeItemService.delete(itemId);
        return ResponseEntity.noContent().build();
    }
}