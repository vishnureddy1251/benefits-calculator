package com.paylocity.benefits_calculator.controller;

import com.paylocity.benefits_calculator.dto.ApiResponse;
import com.paylocity.benefits_calculator.dto.DependentDto;
import com.paylocity.benefits_calculator.dto.request.CreateDependentModel;
import com.paylocity.benefits_calculator.dto.request.PaginationFilter;
import com.paylocity.benefits_calculator.dto.request.UpdateDependentModel;
import com.paylocity.benefits_calculator.service.DependentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for Dependent operations.
 *
 * Provides endpoints for:
 * - CRUD operations on dependents
 * - Employee-specific queries
 * - Benefit cost calculations
 * - Pagination support
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/dependents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dependent", description = "Dependent management endpoints")
public class DependentController {

    private final DependentService dependentService;

    /**
     * Create a new dependent
     *
     * POST /api/v1/dependents
     */
    @PostMapping
    @Operation(summary = "Create dependent", description = "Create a new dependent for an employee")
    public ResponseEntity<ApiResponse<DependentDto>> createDependent(
            @Valid @RequestBody CreateDependentModel model) {

        log.info("Creating dependent: {} {} for employee {}",
                model.getFirstName(), model.getLastName(), model.getEmployeeId());

        DependentDto dependent = dependentService.createDependent(model);

        ApiResponse<DependentDto> response = ApiResponse.success(
                dependent,
                "Dependent created successfully"
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get dependent by ID
     *
     * GET /api/v1/dependents/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get dependent by ID", description = "Retrieve a single dependent by their ID")
    public ResponseEntity<ApiResponse<DependentDto>> getDependentById(
            @Parameter(description = "Dependent ID") @PathVariable Long id) {

        log.info("Fetching dependent with ID: {}", id);

        DependentDto dependent = dependentService.getDependentById(id);

        ApiResponse<DependentDto> response = ApiResponse.success(dependent);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all dependents
     *
     * GET /api/v1/dependents
     */
    @GetMapping
    @Operation(summary = "Get all dependents", description = "Retrieve all active dependents with optional pagination")
    public ResponseEntity<ApiResponse<?>> getAllDependents(
            @Parameter(description = "Page number (1-based)") @RequestParam(required = false) Integer pageNumber,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer pageSize) {

        if (pageNumber != null && pageSize != null) {
            log.info("Fetching dependents with pagination: page {}, size {}", pageNumber, pageSize);

            PaginationFilter filter = new PaginationFilter(pageNumber, pageSize);
            Page<DependentDto> dependentPage = dependentService.getAllDependents(filter);

            ApiResponse<Page<DependentDto>> response = ApiResponse.success(
                    dependentPage,
                    String.format("Retrieved %d dependents (page %d of %d)",
                            dependentPage.getNumberOfElements(),
                            dependentPage.getNumber() + 1,
                            dependentPage.getTotalPages())
            );

            return ResponseEntity.ok(response);
        } else {
            log.info("Fetching all dependents without pagination");

            List<DependentDto> dependents = dependentService.getAllDependents();

            ApiResponse<List<DependentDto>> response = ApiResponse.success(
                    dependents,
                    String.format("Retrieved %d dependents", dependents.size())
            );

            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get dependents by employee ID
     *
     * GET /api/v1/dependents/employee/{employeeId}
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get dependents by employee", description = "Retrieve all dependents for a specific employee")
    public ResponseEntity<ApiResponse<?>> getDependentsByEmployeeId(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Parameter(description = "Page number (1-based)") @RequestParam(required = false) Integer pageNumber,
            @Parameter(description = "Page size") @RequestParam(required = false) Integer pageSize) {

        if (pageNumber != null && pageSize != null) {
            log.info("Fetching dependents for employee {} with pagination", employeeId);

            PaginationFilter filter = new PaginationFilter(pageNumber, pageSize);
            Page<DependentDto> dependentPage = dependentService.getDependentsByEmployeeId(employeeId, filter);

            ApiResponse<Page<DependentDto>> response = ApiResponse.success(
                    dependentPage,
                    String.format("Employee has %d dependent(s)", dependentPage.getTotalElements())
            );

            return ResponseEntity.ok(response);
        } else {
            log.info("Fetching all dependents for employee {}", employeeId);

            List<DependentDto> dependents = dependentService.getDependentsByEmployeeId(employeeId);

            ApiResponse<List<DependentDto>> response = ApiResponse.success(
                    dependents,
                    String.format("Employee has %d dependent(s)", dependents.size())
            );

            return ResponseEntity.ok(response);
        }
    }

    /**
     * Update dependent
     *
     * PUT /api/v1/dependents/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update dependent", description = "Update an existing dependent")
    public ResponseEntity<ApiResponse<DependentDto>> updateDependent(
            @Parameter(description = "Dependent ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDependentModel model) {

        log.info("Updating dependent with ID: {}", id);

        // Ensure ID in path matches ID in body
        model.setId(id);

        DependentDto dependent = dependentService.updateDependent(model);

        ApiResponse<DependentDto> response = ApiResponse.success(
                dependent,
                "Dependent updated successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Delete dependent
     *
     * DELETE /api/v1/dependents/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete dependent", description = "Soft delete a dependent (sets status to INACTIVE)")
    public ResponseEntity<ApiResponse<Void>> deleteDependent(
            @Parameter(description = "Dependent ID") @PathVariable Long id) {

        log.info("Deleting dependent with ID: {}", id);

        dependentService.deleteDependent(id);

        ApiResponse<Void> response = ApiResponse.success(
                null,
                "Dependent deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate benefit cost for dependent
     *
     * GET /api/v1/dependents/{id}/benefit-cost
     */
    @GetMapping("/{id}/benefit-cost")
    @Operation(summary = "Calculate benefit cost", description = "Calculate monthly benefit cost for a dependent")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateBenefitCost(
            @Parameter(description = "Dependent ID") @PathVariable Long id) {

        log.info("Calculating benefit cost for dependent ID: {}", id);

        BigDecimal cost = dependentService.calculateMonthlyBenefitCost(id);

        ApiResponse<BigDecimal> response = ApiResponse.success(
                cost,
                String.format("Monthly benefit cost: $%s", cost)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get dependent count for employee
     *
     * GET /api/v1/dependents/employee/{employeeId}/count
     */
    @GetMapping("/employee/{employeeId}/count")
    @Operation(summary = "Get dependent count", description = "Get count of dependents for an employee")
    public ResponseEntity<ApiResponse<Long>> getDependentCount(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Fetching dependent count for employee {}", employeeId);

        long count = dependentService.getDependentCount(employeeId);

        ApiResponse<Long> response = ApiResponse.success(
                count,
                String.format("Employee has %d dependent(s)", count)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Check if employee has spouse or partner
     *
     * GET /api/v1/dependents/employee/{employeeId}/has-spouse
     */
    @GetMapping("/employee/{employeeId}/has-spouse")
    @Operation(summary = "Check spouse/partner", description = "Check if employee has a spouse or domestic partner")
    public ResponseEntity<ApiResponse<Boolean>> hasSpouseOrPartner(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {

        log.info("Checking spouse/partner for employee {}", employeeId);

        boolean hasSpouse = dependentService.hasSpouseOrPartner(employeeId);

        ApiResponse<Boolean> response = ApiResponse.success(
                hasSpouse,
                hasSpouse ? "Employee has spouse/partner" : "Employee has no spouse/partner"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get total dependent count
     *
     * GET /api/v1/dependents/count
     */
    @GetMapping("/count")
    @Operation(summary = "Get total dependent count", description = "Get total count of all active dependents")
    public ResponseEntity<ApiResponse<Long>> getTotalDependentCount() {

        log.info("Fetching total dependent count");

        long count = dependentService.getTotalDependentCount();

        ApiResponse<Long> response = ApiResponse.success(
                count,
                String.format("Total active dependents: %d", count)
        );

        return ResponseEntity.ok(response);
    }
}