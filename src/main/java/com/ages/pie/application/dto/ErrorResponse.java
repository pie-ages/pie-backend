package com.ages.pie.application.dto;

import java.time.OffsetDateTime;

public record ErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String message,
    String path
) {
    public ErrorResponse(int status, String message, String path) {
        this(OffsetDateTime.now(), status, message, path);
    }
}
