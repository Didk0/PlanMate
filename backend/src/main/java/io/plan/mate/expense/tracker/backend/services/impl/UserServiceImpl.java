package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.entities.User;
import io.plan.mate.expense.tracker.backend.payloads.dtos.UserDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateUserRequest;
import io.plan.mate.expense.tracker.backend.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.services.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;

  @Override
  public UserDto createUser(final CreateUserRequest createUserRequest) {

    if (userRepository.findByEmail(createUserRequest.email()).isPresent()) {

      throw new IllegalArgumentException(
          String.format("Email %s already exists", createUserRequest.email()));
    }

    final User user =
        User.builder().name(createUserRequest.name()).email(createUserRequest.email()).build();

    final User createdUser = userRepository.save(user);

    return modelMapper.map(createdUser, UserDto.class);
  }

  @Override
  public UserDto getUserById(final Long userId) {

    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format("No user with id %s found", userId)));

    return modelMapper.map(user, UserDto.class);
  }

  @Override
  public List<UserDto> getAllUsers() {

    return userRepository.findAll().stream()
        .map(user -> modelMapper.map(user, UserDto.class))
        .toList();
  }

  @Override
  public UserDto deleteUser(final Long userId) {

    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format("No user with id %s found", userId)));

    final UserDto userToDelete = modelMapper.map(user, UserDto.class);

    userRepository.delete(user);

    return userToDelete;
  }
}
