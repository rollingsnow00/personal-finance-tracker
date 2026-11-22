package com.finance.tracker.controller;

import com.finance.tracker.dto.request.TransactionRequest;
import com.finance.tracker.dto.request.TransactionSearchRequest;
import com.finance.tracker.dto.response.TransactionResponse;
import com.finance.tracker.dto.response.ApiError;
import com.finance.tracker.service.TransactionService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Income and expense CRUD with paging, sorting, and filtering")
@SecurityRequirement(name = "Bearer Authentication")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid filters or transaction rule", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "A valid bearer token is required", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Transaction or category not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Data conflict", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) { this.transactionService = transactionService; }

    @GetMapping
    @Operation(summary = "Search and list transactions", description = "Filters are optional; results are paginated and sorted")
    public Page<TransactionResponse> search(Authentication auth, @Valid @ModelAttribute TransactionSearchRequest filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return transactionService.search(auth.getName(), filters, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction")
    public TransactionResponse get(Authentication auth, @PathVariable Long id) {
        return transactionService.get(auth.getName(), id);
    }

    @PostMapping
    @Operation(summary = "Create an income or expense")
    public ResponseEntity<TransactionResponse> create(Authentication auth,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(auth.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public TransactionResponse update(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a transaction")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        transactionService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
