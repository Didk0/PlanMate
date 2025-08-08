package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.payloads.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.payloads.dtos.MemberDto;
import io.plan.mate.expense.tracker.backend.services.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @PostMapping("/groups/{groupId}/users/{userId}")
  public ResponseEntity<MemberDto> addUserToGroup(
      @PathVariable final Long groupId, @PathVariable final Long userId) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(memberService.addUserToGroup(groupId, userId));
  }

  @DeleteMapping("/groups/{groupId}/users/{userId}")
  public ResponseEntity<MemberDto> removeUserFromGroup(
      @PathVariable final Long groupId, @PathVariable final Long userId) {

    return ResponseEntity.ok(memberService.removeUserFromGroup(groupId, userId));
  }

  @GetMapping("/groups/{groupId}/users")
  public ResponseEntity<List<MemberDto>> getGroupMembers(@PathVariable final Long groupId) {

    return ResponseEntity.ok(memberService.getGroupMembers(groupId));
  }

  @GetMapping("/users/{userId}/groups")
  public ResponseEntity<List<GroupDto>> getUserGroups(@PathVariable final Long userId) {

    return ResponseEntity.ok(memberService.getUserGroups(userId));
  }
}
