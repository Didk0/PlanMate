package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.payloads.dtos.ExpenseDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateExpenseRequest;
import java.util.List;

public interface ExpenseService {

  ExpenseDto createExpense(CreateExpenseRequest createExpenseRequest);

  List<ExpenseDto> getGroupExpenses(Long groupId);
}
