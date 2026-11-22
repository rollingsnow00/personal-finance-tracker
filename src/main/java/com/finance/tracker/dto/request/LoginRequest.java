package com.finance.tracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Schema(description = "Username or email", example = "morris@example.com") String identifier,
        @NotBlank @Schema(example = "FinancePass1!") String password
) { }
