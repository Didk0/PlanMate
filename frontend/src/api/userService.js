import api from "./client";

const userService = {
  getGroupMembers: (groupId) => api.get(`groups/${groupId}/users`).then((res) => res.data),

  addMemberToGroup: (groupId, user) =>
    api.post(`groups/${groupId}/users`, user).then((res) => res.data),

  removeMemberFromGroup: (groupId, memberId) => api.delete(`groups/${groupId}/users/${memberId}`),

  createUser: (userData) => api.post("users", userData).then((res) => res.data),
};

export default userService;
