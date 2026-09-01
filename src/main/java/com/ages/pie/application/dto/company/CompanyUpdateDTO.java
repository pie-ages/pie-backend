package com.ages.pie.application.dto.company;

public record CompanyUpdateDTO(
    String name,
    String socialReason,
    String responsiblePerson,
    String email,
    String website,
    String photoUrl
) {
}
