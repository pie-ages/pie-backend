package com.ages.pie.application.dto;

import java.util.UUID;
import com.ages.pie.domain.enums.AccountSex;

public record UserSummaryDTO(
    UUID id,
    String name,
    String email,
    String photoUrl,
    String role,
    String status,
    AccountSex sex
) {
}