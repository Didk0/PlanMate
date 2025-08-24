package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.db.dtos.ExpenseDto;
import io.plan.mate.expense.tracker.backend.payloads.events.ExpenseChangeEnum;
import io.plan.mate.expense.tracker.backend.payloads.events.ExpenseCreatedEvent;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.services.ExpenseService;
import io.plan.mate.expense.tracker.backend.services.publishers.WebSocketEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "expenses", description = "APIs to manage group expenses")
public class ExpenseController {

  private final ExpenseService expenseService;
  private final WebSocketEventPublisher eventPublisher;

  @Operation(
      summary = "Create a new expense",
      description = "Creates a new expense entry for a group",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Expense created successfully",
            content = @Content(schema = @Schema(implementation = ExpenseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid field for expense provided"),
        @ApiResponse(responseCode = "404", description = "User or group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/{groupId}/expenses")
  public ResponseEntity<ExpenseDto> createExpense(
      @PathVariable final Long groupId,
      @Valid @RequestBody final CreateExpenseRequest createExpenseRequest) {

    final ExpenseDto expenseDto = expenseService.createExpense(groupId, createExpenseRequest);
    eventPublisher.publishExpenseCreated(
        new ExpenseCreatedEvent(ExpenseChangeEnum.ADD_EXPENSE, expenseDto));
    return ResponseEntity.status(HttpStatus.CREATED).body(expenseDto);
  }

  @Operation(
      summary = "Get expenses by group ID",
      description = "Retrieves all expenses for a specified group",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of expenses for the group",
            content =
                @Content(schema = @Schema(implementation = ExpenseDto.class, type = "array"))),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{groupId}/expenses")
  public ResponseEntity<List<ExpenseDto>> getGroupExpenses(@PathVariable final Long groupId) {

    final List<ExpenseDto> expenseDtos = expenseService.getGroupExpenses(groupId);
    return ResponseEntity.ok(expenseDtos);
  }
}
