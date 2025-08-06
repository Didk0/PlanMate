package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.payloads.dtos.UserDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateUserRequest;
import io.plan.mate.expense.tracker.backend.services.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> createUser(
      @RequestBody final CreateUserRequest createUserRequest) {

    final UserDto userDto = userService.createUser(createUserRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable final Long id) {

    final UserDto userDto = userService.getUserById(id);
    return ResponseEntity.ok(userDto);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {

    final List<UserDto> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<UserDto> deleteUser(@PathVariable final Long id) {

    final UserDto userDto = userService.deleteUser(id);
    return ResponseEntity.ok(userDto);
  }
}
