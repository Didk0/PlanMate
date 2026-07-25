package io.plan.mate.expense.tracker.backend.expense.service;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.UpdateExpenseRequest;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

  ExpenseDto createExpense(Long groupId, CreateExpenseRequest createExpenseRequest);

  ExpenseDto updateExpense(
      Long groupId, Long expenseId, UpdateExpenseRequest updateExpenseRequest);

  void deleteExpense(Long groupId, Long expenseId);

  PagedResponse<ExpenseDto> getGroupExpenses(Long groupId, String search, Pageable pageable);
}
