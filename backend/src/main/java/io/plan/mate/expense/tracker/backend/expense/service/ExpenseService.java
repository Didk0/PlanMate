package io.plan.mate.expense.tracker.backend.expense.service;

import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import java.util.List;

public interface ExpenseService {

  ExpenseDto createExpense(Long groupId, CreateExpenseRequest createExpenseRequest);

  List<ExpenseDto> getGroupExpenses(Long groupId);
}
