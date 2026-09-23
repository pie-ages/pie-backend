package com.ages.pie.application.controller;

import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.dto.user.UserRequestDTO;
import com.ages.pie.application.dto.user.UserResponseDTO;
import com.ages.pie.application.dto.user.UserUpdateDTO;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PreferenceService preferenceService;
    private final AuthenticatedUserProvider authProvider;

    public UserController(UserService userService,
                          PreferenceService preferenceService,
                          AuthenticatedUserProvider authProvider) {
        this.userService = userService;
        this.preferenceService = preferenceService;
        this.authProvider = authProvider;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/preferences")
    public ResponseEntity<PreferencesResponseDTO> getPreferences() {
        UUID userId = authProvider.id();
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<PreferencesResponseDTO> updatePreferences(
            @Valid @RequestBody PreferencesRequestDTO dto) {
        UUID userId = authProvider.id();
        return ResponseEntity.ok(preferenceService.updateFavoriteColors(userId, dto));
    }
}

