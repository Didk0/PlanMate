import expenseService from "../../api/services/expenseService";
import groupService from "../../api/services/groupService";
import userService from "../../api/services/userService";

export const loadAllGroups = () => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    const data = await groupService.getAllGroups();
    dispatch({ type: "IS_SUCCESS" });
    return data;
  } catch (error) {
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to fetch groups",
    });
  }
};

export const createGroup = (groupData) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    const newGroup = await groupService.createGroup(groupData);
    dispatch({ type: "IS_SUCCESS" });
    return newGroup;
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to create group",
    });
  }
};

export const loadGroupDetailsData = (groupId) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    const [groupData, membersData, expensesData] = await Promise.all([
      groupService.getGroupById(groupId),
      userService.getGroupMembers(groupId),
      expenseService.getGroupExpenses(groupId),
    ]);
    dispatch({ type: "IS_SUCCESS" });
    return { groupData, membersData, expensesData };
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to load group details",
    });
  }
};

export const addMemberToGroup = (groupId, memberData) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    const addedMember = await userService.addMemberToGroup(groupId, memberData);
    dispatch({ type: "IS_SUCCESS" });
    return addedMember;
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to add member to group",
    });
  }
};

export const removeMemberFromGroup = (groupId, userId) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    await userService.removeMemberFromGroup(groupId, userId);
    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to remove member from group",
    });
  }
};

export const calculateSettlements = (groupId) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    const settlements = await expenseService.calculateSettlements(groupId);
    dispatch({ type: "IS_SUCCESS" });
    return settlements;
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to remove member from group",
    });
  }
};

export const getGroupMembers = (groupId) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    const members = await userService.getGroupMembers(groupId);
    dispatch({ type: "IS_SUCCESS" });
    return members;
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to load group members",
    });
  }
};

export const createExpense = (groupId, expense) => async (dispatch) => {
  dispatch({ type: "IS_LOADING" });
  try {
    await expenseService.createExpense(groupId, expense);
    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error.message || "Failed to create expense",
    });
  }
};

export const setAuthData = (user, token) => (dispatch) => {
  dispatch({
    type: "LOGIN_USER",
    payload: { user, token },
  });
  localStorage.setItem("auth", JSON.stringify({ user, token }));
};

export const clearAuthData = () => (dispatch) => {
  dispatch({ type: "LOGOUT_USER" });
  localStorage.removeItem("auth");
  localStorage.removeItem("userCreated");
};
