package io.plan.mate.expense.tracker.backend.user.controller;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.dto.ApiError;
import io.plan.mate.expense.tracker.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "users", description = "User management APIs")
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "Provision the authenticated user",
      description =
          "Creates (or re-syncs) the PlanMate user for the caller's own Keycloak identity",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "User with provided email already exists or invalid user field provided",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PostMapping
  public ResponseEntity<UserDto> createUser() {

    final UserDto userDto = userService.provisionCurrentUser();

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
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller may only look up their own user", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isSelf(#userId)")
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUserById(@PathVariable final Long userId) {

    final UserDto userDto = userService.getUserById(userId);
    return ResponseEntity.ok(userDto);
  }

  @Operation(
      summary = "Get all users",
      description = "Retrieves a page of all users",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Page of users",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<PagedResponse<UserDto>> getAllUsers(
      @PageableDefault(size = 10, sort = "username") final Pageable pageable) {

    return ResponseEntity.ok(userService.getAllUsers(pageable));
  }

  @Operation(
      summary = "Delete a user by ID",
      description = "Deletes the user identified by the provided ID",
      responses = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "User has group memberships or expense history, or caller is attempting to delete their own account", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable final Long userId) {

    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
