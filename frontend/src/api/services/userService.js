import api from "../api";

const userService = {
  getGroupMembers: (groupId) =>
    api.get(`groups/${groupId}/users`).then((res) => res.data),
  addMemberToGroup: (groupId, user) =>
    api.post(`groups/${groupId}/users`, user).then((res) => res.data),
  removeMemberFromGroup: (groupId, userId) => api.delete(`groups/${groupId}/users/${userId}`),
};

export default userService;
