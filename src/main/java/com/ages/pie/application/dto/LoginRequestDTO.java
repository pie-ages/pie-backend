package com.ages.pie.application.dto;

public record LoginRequestDTO(
    @NotBlank
    String email,
    @NotBlank
    String password
) {
}
