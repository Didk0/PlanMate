package io.plan.mate.expense.tracker.backend.user.service;

import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import java.util.List;

public interface UserService {

  UserDto provisionCurrentUser();

  UserDto getUserById(Long userId);

  List<UserDto> getAllUsers();

  void deleteUser(Long userId);
}
