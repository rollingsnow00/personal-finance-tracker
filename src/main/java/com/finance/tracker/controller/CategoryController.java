package com.finance.tracker.controller;

import com.finance.tracker.dto.request.CategoryRequest;
import com.finance.tracker.dto.response.CategoryResponse;
import com.finance.tracker.dto.response.ApiError;
import com.finance.tracker.service.CategoryService;
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
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Income and expense categories owned by the current user")
@SecurityRequirement(name = "Bearer Authentication")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "A valid bearer token is required", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Category name conflicts with an existing category", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }

    @GetMapping
    @Operation(summary = "List categories", description = "Paginated and sorted; deleted categories are hidden")
    public Page<CategoryResponse> list(Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return categoryService.list(auth.getName(), page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category")
    public CategoryResponse get(Authentication auth, @PathVariable Long id) {
        return categoryService.get(auth.getName(), id);
    }

    @PostMapping
    @Operation(summary = "Create a category")
    public ResponseEntity<CategoryResponse> create(Authentication auth, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(auth.getName(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public CategoryResponse update(Authentication auth, @PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a category")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        categoryService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
