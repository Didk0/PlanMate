import api from "../api";

const userService = {
  getGroupMembers: (groupId) =>
    api.get(`groups/${groupId}/users`).then((res) => res.data),

  addMemberToGroup: (groupId, user) => {
    api.post(`groups/${groupId}/users`, user).then((res) => res.data),
    console.log("USER:", user);
  },

  removeMemberFromGroup: (groupId, memberId) =>
    api.delete(`groups/${groupId}/users/${memberId}`),

  createUser: (userData, token) =>
    api
      .post("users", userData, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => res.data),
};

export default userService;
