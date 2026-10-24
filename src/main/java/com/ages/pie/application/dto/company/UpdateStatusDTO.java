package com.ages.pie.application.dto.company;

import com.ages.pie.domain.enums.CompanyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDTO(@NotNull CompanyStatus status) {
}
