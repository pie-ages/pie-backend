package com.ages.pie.application.exception;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message
) {
}
