package com.ages.pie.application.controller;

import com.ages.pie.application.dto.wardrobe.WardrobeResponseDTO;
import com.ages.pie.application.service.WardrobeItemService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/wardrobe")
public class WardrobeController {

    private final WardrobeItemService wardrobeItemService;

    public WardrobeController(WardrobeItemService wardrobeItemService) {
        this.wardrobeItemService = wardrobeItemService;
    }

    @GetMapping
    public ResponseEntity<WardrobeResponseDTO> list(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(wardrobeItemService.listWardrobe(category, pageable));
    }
}