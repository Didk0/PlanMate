import api from "./client";
import expenseService from "./expenseService";
import userService from "./userService";

const groupService = {
  getAllGroups: () => api.get("/groups").then((res) => res.data),

  getGroupById: (groupId) => api.get(`groups/${groupId}`).then((res) => res.data),

  createGroup: (group) => api.post(`groups`, group).then((res) => res.data),

  getGroupDetails: async (groupId) => {
    const [groupData, membersData, expensesData] = await Promise.all([
      groupService.getGroupById(groupId),
      userService.getGroupMembers(groupId),
      expenseService.getGroupExpenses(groupId),
    ]);
    return { groupData, membersData, expensesData };
  },
};

export default groupService;
