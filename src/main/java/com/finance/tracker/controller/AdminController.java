package com.finance.tracker.controller;

import com.finance.tracker.dto.response.BudgetResponse;
import com.finance.tracker.dto.response.CategoryResponse;
import com.finance.tracker.dto.response.TransactionResponse;
import com.finance.tracker.dto.response.ApiError;
import com.finance.tracker.service.BudgetService;
import com.finance.tracker.service.CategoryService;
import com.finance.tracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "ADMIN-only restore operations for soft-deleted records")
@SecurityRequirement(name = "Bearer Authentication")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "A valid bearer token is required", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Deleted resource not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Resource cannot be restored because of a conflict", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class AdminController {
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    public AdminController(CategoryService categoryService, TransactionService transactionService,
                           BudgetService budgetService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.budgetService = budgetService;
    }

    @PostMapping("/categories/{id}/restore")
    @Operation(summary = "Restore a deleted category; ADMIN role required")
    public CategoryResponse restoreCategory(@PathVariable Long id) { return categoryService.restore(id); }

    @PostMapping("/transactions/{id}/restore")
    @Operation(summary = "Restore a deleted transaction; ADMIN role required")
    public TransactionResponse restoreTransaction(@PathVariable Long id) { return transactionService.restore(id); }

    @PostMapping("/budgets/{id}/restore")
    @Operation(summary = "Restore a deleted budget; ADMIN role required")
    public BudgetResponse restoreBudget(@PathVariable Long id) { return budgetService.restore(id); }
}
