import api from "../api";

const groupService = {
  getAllGroups: () => api.get("/groups").then((res) => res.data),
  getGroupById: (groupId) =>
    api.get(`groups/${groupId}`).then((res) => res.data),
  createGroup: (group) => api.post(`groups`, group).then((res) => res.data),
};

export default groupService;
