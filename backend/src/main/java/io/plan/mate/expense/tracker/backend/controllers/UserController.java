package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.db.dtos.UserDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateUserRequest;
import io.plan.mate.expense.tracker.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "users", description = "User management APIs")
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "Create a new user",
      description = "Creates a new user with the provided details",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "User with provided email already exists or invalid user field provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<UserDto> createUser(
      @Valid @RequestBody final CreateUserRequest createUserRequest) {

    final UserDto userDto = userService.createUser(createUserRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @Operation(
      summary = "Get user by ID",
      description = "Retrieves details of a user by their ID",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUserById(@PathVariable final Long userId) {

    final UserDto userDto = userService.getUserById(userId);
    return ResponseEntity.ok(userDto);
  }

  @Operation(
      summary = "Get all users",
      description = "Retrieves a list of all users",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content = @Content(schema = @Schema(implementation = UserDto.class, type = "array"))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {

    final List<UserDto> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @Operation(
      summary = "Delete a user by ID",
      description = "Deletes the user identified by the provided ID",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User deleted",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{userId}")
  public ResponseEntity<UserDto> deleteUser(@PathVariable final Long userId) {

    final UserDto userDto = userService.deleteUser(userId);
    return ResponseEntity.ok(userDto);
  }
}
