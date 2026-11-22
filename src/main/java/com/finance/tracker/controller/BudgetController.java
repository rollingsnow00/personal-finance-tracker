package com.finance.tracker.controller;

import com.finance.tracker.dto.request.BudgetRequest;
import com.finance.tracker.dto.response.BudgetResponse;
import com.finance.tracker.dto.response.ApiError;
import com.finance.tracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Monthly spending limits by expense category")
@SecurityRequirement(name = "Bearer Authentication")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid budget request", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "A valid bearer token is required", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Budget or expense category not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "A budget already exists for the category and month", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) { this.budgetService = budgetService; }

    @GetMapping
    @Operation(summary = "List budgets", description = "Paginated and sorted; deleted budgets are hidden")
    public Page<BudgetResponse> list(Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "monthStart") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return budgetService.list(auth.getName(), page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a budget")
    public BudgetResponse get(Authentication auth, @PathVariable Long id) {
        return budgetService.get(auth.getName(), id);
    }

    @PostMapping
    @Operation(summary = "Create a monthly category budget")
    public ResponseEntity<BudgetResponse> create(Authentication auth, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(auth.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public BudgetResponse update(Authentication auth, @PathVariable Long id,
                                 @Valid @RequestBody BudgetRequest request) {
        return budgetService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a budget")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        budgetService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
