package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.payloads.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.services.GroupService;
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

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@RequestBody final CreateGroupRequest createGroupRequest) {

        final GroupDto groupDto = groupService.createGroup(createGroupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable final Long id) {

        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @GetMapping
    public ResponseEntity<List<GroupDto>> getAllGroups() {

        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<GroupDto> deleteGroup(@PathVariable final Long id) {

        GroupDto groupDto = groupService.deleteGroup(id);

        return ResponseEntity.ok(groupDto);
    }
}
