package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.db.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.db.dtos.MemberDto;
import io.plan.mate.expense.tracker.backend.payloads.events.MemberChangeEnum;
import io.plan.mate.expense.tracker.backend.payloads.events.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.payloads.request.AddUserRequest;
import io.plan.mate.expense.tracker.backend.services.MemberService;
import io.plan.mate.expense.tracker.backend.services.websocket.WebSocketEventPublisher;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "members", description = "APIs to manage group membership")
public class MemberController {

  private final MemberService memberService;
  private final WebSocketEventPublisher eventPublisher;

  @Operation(
      summary = "Add user to group",
      description = "Adds a user to the specified group",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully added to group",
            content = @Content(schema = @Schema(implementation = MemberDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "User is already a member of the given group"),
        @ApiResponse(responseCode = "404", description = "Group or user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping("/groups/{groupId}/users")
  public ResponseEntity<MemberDto> addUserToGroup(
      @PathVariable final Long groupId, @Valid @RequestBody final AddUserRequest addUserRequest) {

    final MemberDto memberDto = memberService.addUserToGroup(groupId, addUserRequest);

    eventPublisher.publishMembersUpdate(
        new MemberChangedEvent(MemberChangeEnum.ADD_MEMBER, groupId, memberDto));

    return ResponseEntity.status(HttpStatus.CREATED).body(memberDto);
  }

  @Operation(
      summary = "Remove user from group",
      description = "Removes a user from a specified group",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User successfully removed from group",
            content = @Content(schema = @Schema(implementation = MemberDto.class))),
        @ApiResponse(responseCode = "404", description = "Group or user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/groups/{groupId}/users/{memberId}")
  public ResponseEntity<Void> removeUserFromGroup(
      @PathVariable final Long groupId, @PathVariable final Long memberId) {

    memberService.removeUserFromGroup(groupId, memberId);

    eventPublisher.publishMembersUpdate(
        new MemberChangedEvent(
            MemberChangeEnum.REMOVE_MEMBER, groupId, MemberDto.builder().id(memberId).build()));

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(
      summary = "Get members of a group",
      description = "Returns a list of all members in the specified group",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of group members",
            content = @Content(schema = @Schema(implementation = MemberDto.class, type = "array"))),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/groups/{groupId}/users")
  public ResponseEntity<List<MemberDto>> getGroupMembers(@PathVariable final Long groupId) {

    return ResponseEntity.ok(memberService.getGroupMembers(groupId));
  }

  @Operation(
      summary = "Get groups of a user",
      description = "Returns a list of groups the specified user is a member of",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of groups for the user",
            content = @Content(schema = @Schema(implementation = GroupDto.class, type = "array"))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/users/{userId}/groups")
  public ResponseEntity<List<GroupDto>> getUserGroups(@PathVariable final Long userId) {

    return ResponseEntity.ok(memberService.getUserGroups(userId));
  }
}
