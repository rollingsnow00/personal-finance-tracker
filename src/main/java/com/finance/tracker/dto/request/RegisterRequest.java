package com.finance.tracker.dto.request;

import com.finance.tracker.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 40)
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username may contain letters, numbers, dots, underscores, and hyphens")
        @Schema(example = "morris") String username,
        @NotBlank @Email @Size(max = 254) @Schema(example = "morris@example.com") String email,
        @NotBlank @StrongPassword @Schema(example = "FinancePass1!") String password
) { }
