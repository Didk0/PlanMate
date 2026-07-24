package io.plan.mate.expense.tracker.backend.group.controller;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.dto.ApiError;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        @ApiResponse(responseCode = "400", description = "Invalid group field provided", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
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
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not a member of the group", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isMember(#groupId)")
  @GetMapping("/{groupId}")
  public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long groupId) {

    return ResponseEntity.ok(groupService.getGroupById(groupId));
  }

  @Operation(
      summary = "Get all groups",
      description =
          "Returns a page of the caller's groups, or every group in the system for an ADMIN caller",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Page of groups",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @GetMapping
  public ResponseEntity<PagedResponse<GroupDto>> getAllGroups(
      @PageableDefault(size = 5, sort = "name") final Pageable pageable) {

    return ResponseEntity.ok(groupService.getAllGroups(pageable));
  }

  @Operation(
      summary = "Delete a group by ID",
      description = "Deletes the group corresponding to the given ID",
      responses = {
        @ApiResponse(responseCode = "204", description = "Group deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not the owner of the group", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Group not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
      })
  @PreAuthorize("@groupAccess.isOwner(#groupId)")
  @DeleteMapping("/{groupId}")
  public ResponseEntity<Void> deleteGroup(@PathVariable final Long groupId) {

    groupService.deleteGroup(groupId);

    return ResponseEntity.noContent().build();
  }
}
