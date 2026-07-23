import api from "./client";

const userService = {
  getGroupMembers: (groupId) => api.get(`groups/${groupId}/users`).then((res) => res.data),

  addMemberToGroup: (groupId, user) =>
    api.post(`groups/${groupId}/users`, user).then((res) => res.data),

  removeMemberFromGroup: (groupId, memberId) => api.delete(`groups/${groupId}/users/${memberId}`),

  createUser: () => api.post("users").then((res) => res.data),

  getAllUsers: () => api.get("users").then((res) => res.data),

  deleteUser: (userId) => api.delete(`users/${userId}`),
};

export default userService;
