package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.payloads.dtos.ExpenseDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.services.ExpenseService;
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
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

  private final ExpenseService expenseService;

  @PostMapping
  public ResponseEntity<ExpenseDto> createExpense(
      @RequestBody final CreateExpenseRequest createExpenseRequest) {

    final ExpenseDto expenseDto = expenseService.createExpense(createExpenseRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(expenseDto);
  }

  @GetMapping("/groups/{groupId}")
  public ResponseEntity<List<ExpenseDto>> getGroupExpenses(@PathVariable final Long groupId) {

    final List<ExpenseDto> expenseDtos = expenseService.getGroupExpenses(groupId);
    return ResponseEntity.ok(expenseDtos);
  }
}
