package com.finance.tracker.controller;

import com.finance.tracker.dto.response.MonthlyReportResponse;
import com.finance.tracker.dto.response.ApiError;
import com.finance.tracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Monthly totals and expense totals by category")
@SecurityRequirement(name = "Bearer Authentication")
@Validated
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid month or currency", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "A valid bearer token is required", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) { this.reportService = reportService; }

    @GetMapping("/monthly")
    @Operation(summary = "Get a monthly income, expense, balance, and budget report")
    public MonthlyReportResponse monthly(Authentication auth,
            @RequestParam(required = false) YearMonth month,
            @RequestParam(defaultValue = "PLN") @Pattern(regexp = "^[A-Z]{3}$") String currency) {
        return reportService.monthly(auth.getName(), month == null ? YearMonth.now() : month, currency);
    }
}
