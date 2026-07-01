package io.plan.mate.expense.tracker.backend.user.service;

import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import io.plan.mate.expense.tracker.backend.user.controller.payload.request.CreateUserRequest;
import java.util.List;

public interface UserService {

  UserDto createUser(CreateUserRequest createUserRequest);

  UserDto getUserById(Long userId);

  List<UserDto> getAllUsers();

  UserDto deleteUser(Long userId);
}
