package com.ages.pie.application.controller;

import com.ages.pie.application.dto.user.UserStyleResponseDTO;
import com.ages.pie.application.dto.user.UserStyleUpdateDTO;
import com.ages.pie.application.service.UserStyleService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/style")
public class UserStyleController {

    private final UserStyleService userStyleService;
    private final AuthenticatedUserProvider authenticatedUser;

    public UserStyleController(UserStyleService userStyleService,
            AuthenticatedUserProvider authenticatedUser) {
        this.userStyleService = userStyleService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping
    public ResponseEntity<UserStyleResponseDTO> getMyStyle() {
        return ResponseEntity.ok(userStyleService.getMyStyle(authenticatedUser.id()));
    }

    @PutMapping
    public ResponseEntity<UserStyleResponseDTO> updateMyStyle(
            @Valid @RequestBody UserStyleUpdateDTO dto) {
        return ResponseEntity.ok(
                userStyleService.updateMyStyle(authenticatedUser.id(), dto.styles()));
    }
}
