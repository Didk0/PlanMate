import api from "../api";

const expenseService = {
  getGroupExpenses: (groupId) =>
    api.get(`expenses/groups/${groupId}`).then((res) => res.data),
};

export default expenseService;
