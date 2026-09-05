package com.ages.pie.application.dto.company;

public record CompanyRequestDTO(
    String name,
    String cnpj,
    String socialReason,
    String responsiblePerson,
    String email,
    String password,
    String website,
    String photoUrl
) {
}
