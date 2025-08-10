package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.payloads.dtos.SettlementDto;
import io.plan.mate.expense.tracker.backend.services.SettlementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class SettlementController {

  private final SettlementService settlementService;

  @GetMapping
  public ResponseEntity<List<SettlementDto>> getSettlements(@PathVariable final Long groupId) {

    final List<SettlementDto> settlements = settlementService.getSettlementsByGroup(groupId);
    return ResponseEntity.ok(settlements);
  }

  @PostMapping("/calculate")
  public ResponseEntity<List<SettlementDto>> calculateSettlements(
      @PathVariable final Long groupId) {

    final List<SettlementDto> settlements = settlementService.calculateSettlements(groupId);
    return ResponseEntity.ok(settlements);
  }
}
