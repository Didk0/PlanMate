package io.plan.mate.expense.tracker.backend.expense.service;

import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.UpdateExpenseRequest;
import java.util.List;

public interface ExpenseService {

  ExpenseDto createExpense(Long groupId, CreateExpenseRequest createExpenseRequest);

  ExpenseDto updateExpense(
      Long groupId, Long expenseId, UpdateExpenseRequest updateExpenseRequest);

  void deleteExpense(Long groupId, Long expenseId);

  List<ExpenseDto> getGroupExpenses(Long groupId);
}
