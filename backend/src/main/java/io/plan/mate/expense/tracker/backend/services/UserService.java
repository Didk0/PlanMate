package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.payloads.dtos.UserDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateUserRequest;

import java.util.List;

public interface UserService {

    UserDto createUser(CreateUserRequest createUserRequest);

    UserDto getUserById(Long id);

    List<UserDto> getAllUsers();

    UserDto deleteUser(Long id);
}
