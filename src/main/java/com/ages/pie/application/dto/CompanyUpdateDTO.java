package com.ages.pie.application.dto;

public record CompanyUpdateDTO(
    String name,
    String socialReason,
    String responsiblePerson,
    String email,
    String website,
    String photoUrl
) {
}
