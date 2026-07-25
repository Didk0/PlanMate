import api from "./client";
import userService from "./userService";

const groupService = {
  getAllGroups: (page = 0, size = 5, search = "") =>
    api
      .get("/groups", { params: { page, size, ...(search ? { search } : {}) } })
      .then((res) => res.data),

  getGroupById: (groupId) => api.get(`groups/${groupId}`).then((res) => res.data),

  createGroup: (group) => api.post(`groups`, group).then((res) => res.data),

  deleteGroup: (groupId) => api.delete(`groups/${groupId}`),

  getGroupDetails: async (groupId) => {
    const [groupData, membersData] = await Promise.all([
      groupService.getGroupById(groupId),
      userService.getGroupMembers(groupId),
    ]);
    return { groupData, membersData };
  },
};

export default groupService;
