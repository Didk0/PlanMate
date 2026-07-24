package io.plan.mate.expense.tracker.backend.user.service;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import org.springframework.data.domain.Pageable;

public interface UserService {

  UserDto provisionCurrentUser();

  UserDto getUserById(Long userId);

  PagedResponse<UserDto> getAllUsers(Pageable pageable);

  void deleteUser(Long userId);
}
