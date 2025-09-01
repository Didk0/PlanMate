package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.db.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.services.GroupService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "groups", description = "Group management APIs")
public class GroupController {

  private final GroupService groupService;

  @Operation(
      summary = "Create a new group",
      description = "Creates a new group with the provided information",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Group created successfully",
            content = @Content(schema = @Schema(implementation = GroupDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid group field provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<GroupDto> createGroup(
      @Valid @RequestBody final CreateGroupRequest createGroupRequest) {
    final GroupDto groupDto = groupService.createGroup(createGroupRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(groupDto);
  }

  @Operation(
      summary = "Get group by ID",
      description = "Returns the group details for the given group ID",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Group found",
            content = @Content(schema = @Schema(implementation = GroupDto.class))),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{groupId}")
  public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long groupId) {

    return ResponseEntity.ok(groupService.getGroupById(groupId));
  }

  @Operation(
      summary = "Get all groups",
      description = "Returns a list of all groups",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of groups",
            content = @Content(schema = @Schema(implementation = GroupDto.class, type = "array"))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  // @PreAuthorize("hasRole('role_admin')")
  public ResponseEntity<List<GroupDto>> getAllGroups() {

    return ResponseEntity.ok(groupService.getAllGroups());
  }

  @Operation(
      summary = "Delete a group by ID",
      description =
          "Deletes the group corresponding to the given ID and returns the deleted group details",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Group deleted",
            content = @Content(schema = @Schema(implementation = GroupDto.class))),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("{groupId}")
  public ResponseEntity<GroupDto> deleteGroup(@PathVariable final Long groupId) {

    final GroupDto groupDto = groupService.deleteGroup(groupId);

    return ResponseEntity.ok(groupDto);
  }
}
