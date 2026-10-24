package com.ages.pie.application.controller;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.RefreshRequestDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.service.AuthService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public AuthController(AuthService authService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.authService = authService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.loginCustomer(request);
    }

    @PostMapping("/company/login")
    public LoginResponseDTO loginCompany(@Valid @RequestBody LoginRequestDTO request) {
        return authService.loginCompany(request);
    }

    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt.getTokenValue());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserSummaryDTO me() {
        return authService.findSummary(authenticatedUserProvider.id());
    }
}
