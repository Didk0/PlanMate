import api from "./client";

const expenseService = {
  getGroupExpenses: (groupId, page = 0, size = 10) =>
    api.get(`groups/${groupId}/expenses`, { params: { page, size } }).then((res) => res.data),
  calculateSettlements: (groupId) =>
    api.get(`/groups/${groupId}/settlements/calculate`).then((res) => res.data),
  createExpense: (groupId, expense) =>
    api.post(`/groups/${groupId}/expenses`, expense).then((res) => res.data),
  updateExpense: (groupId, expenseId, expense) =>
    api.put(`/groups/${groupId}/expenses/${expenseId}`, expense).then((res) => res.data),
  deleteExpense: (groupId, expenseId) =>
    api.delete(`/groups/${groupId}/expenses/${expenseId}`).then((res) => res.data),
};

export default expenseService;
