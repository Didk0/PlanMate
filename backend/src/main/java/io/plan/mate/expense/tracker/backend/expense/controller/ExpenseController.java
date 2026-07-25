package io.plan.mate.expense.tracker.backend.expense.controller;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.dto.ApiError;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.UpdateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "expenses", description = "APIs to manage group expenses")
public class ExpenseController {

  private final ExpenseService expenseService;

  @Operation(
      summary = "Create a new expense",
      description = "Creates a new expense entry for a group",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Expense created successfully",
            content = @Content(schema = @Schema(implementation = ExpenseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid field for expense provided", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not a member of the group", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User or group not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isMember(#groupId)")
  @PostMapping("/{groupId}/expenses")
  public ResponseEntity<ExpenseDto> createExpense(
      @PathVariable final Long groupId,
      @Valid @RequestBody final CreateExpenseRequest createExpenseRequest) {

    final ExpenseDto expenseDto = expenseService.createExpense(groupId, createExpenseRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(expenseDto);
  }

  @Operation(
      summary = "Get expenses by group ID",
      description = "Retrieves a page of expenses for a specified group, newest first",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Page of expenses for the group",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not a member of the group", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isMember(#groupId)")
  @GetMapping("/{groupId}/expenses")
  public ResponseEntity<PagedResponse<ExpenseDto>> getGroupExpenses(
      @PathVariable final Long groupId,
      @RequestParam(required = false) final String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          final Pageable pageable) {

    return ResponseEntity.ok(expenseService.getGroupExpenses(groupId, search, pageable));
  }

  @Operation(
      summary = "Update an existing expense",
      description = "Updates the description, amount, payer and participants of an expense",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Expense updated successfully",
            content = @Content(schema = @Schema(implementation = ExpenseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid field for expense provided", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not a member of the group", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User, group or expense not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isMember(#groupId)")
  @PutMapping("/{groupId}/expenses/{expenseId}")
  public ResponseEntity<ExpenseDto> updateExpense(
      @PathVariable final Long groupId,
      @PathVariable final Long expenseId,
      @Valid @RequestBody final UpdateExpenseRequest updateExpenseRequest) {

    final ExpenseDto expenseDto =
        expenseService.updateExpense(groupId, expenseId, updateExpenseRequest);

    return ResponseEntity.ok(expenseDto);
  }

  @Operation(
      summary = "Delete an expense",
      description = "Deletes an expense from a group",
      responses = {
        @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not a member of the group", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Group or expense not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isMember(#groupId)")
  @DeleteMapping("/{groupId}/expenses/{expenseId}")
  public ResponseEntity<Void> deleteExpense(
      @PathVariable final Long groupId, @PathVariable final Long expenseId) {

    expenseService.deleteExpense(groupId, expenseId);

    return ResponseEntity.noContent().build();
  }
}
