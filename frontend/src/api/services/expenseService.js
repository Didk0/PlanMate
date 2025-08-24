import api from "../api";

const expenseService = {
  getGroupExpenses: (groupId) =>
    api.get(`groups/${groupId}/expenses`).then((res) => res.data),
  calculateSettlements: (groupId) =>
    api.get(`/groups/${groupId}/settlements/calculate`).then((res) => res.data),
  createExpense: (groupId, expense) =>
    api.post(`/groups/${groupId}/expenses`, expense).then((res) => res.data),
};

export default expenseService;
