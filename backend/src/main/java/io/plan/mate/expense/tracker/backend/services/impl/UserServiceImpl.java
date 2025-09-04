package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.db.dtos.UserDto;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import io.plan.mate.expense.tracker.backend.db.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateUserRequest;
import io.plan.mate.expense.tracker.backend.services.UserService;
import io.plan.mate.expense.tracker.backend.services.keycloak.KeycloakService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;
  private final KeycloakService keycloakService;

  @Override
  public UserDto createUser(final CreateUserRequest createUserRequest) {

    final User existingUser =
        userRepository.findByKeycloakId(createUserRequest.keycloakId()).orElse(null);

    if (existingUser != null) {

      final UserRepresentation userRepresentation =
          keycloakService.getUser(createUserRequest.keycloakId());

      existingUser.setEmail(userRepresentation.getEmail());
      existingUser.setUsername(userRepresentation.getUsername());
      existingUser.setFirstName(userRepresentation.getFirstName());
      existingUser.setLastName(userRepresentation.getLastName());

      final User updatedUser = userRepository.save(existingUser);

      return modelMapper.map(updatedUser, UserDto.class);
    }

    final User user =
        User.builder()
            .username(createUserRequest.username())
            .email(createUserRequest.email())
            .firstName(createUserRequest.firstName())
            .lastName(createUserRequest.lastName())
            .createdAt(LocalDateTime.now())
            .keycloakId(createUserRequest.keycloakId())
            .build();

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
                    new ResourceNotFoundException(
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
                    new ResourceNotFoundException(
                        String.format("No user with id %s found", userId)));

    final UserDto userToDelete = modelMapper.map(user, UserDto.class);

    userRepository.delete(user);

    return userToDelete;
  }
}
