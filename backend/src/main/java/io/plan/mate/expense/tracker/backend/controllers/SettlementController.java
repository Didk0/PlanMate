package io.plan.mate.expense.tracker.backend.controllers;

import io.plan.mate.expense.tracker.backend.db.dtos.SettlementDto;
import io.plan.mate.expense.tracker.backend.services.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
@RequiredArgsConstructor
@Tag(name = "settlements", description = "APIs to manage settlements within groups")
public class SettlementController {

  private final SettlementService settlementService;

  @Operation(
      summary = "Calculate settlements for a group",
      description =
          "Calculates and returns settlement transactions required to balance expenses in the group",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of calculated settlements",
            content =
                @Content(schema = @Schema(implementation = SettlementDto.class, type = "array"))),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/calculate")
  public ResponseEntity<List<SettlementDto>> calculateSettlements(
      @PathVariable final Long groupId) {

    final List<SettlementDto> settlements = settlementService.calculateSettlements(groupId);
    return ResponseEntity.ok(settlements);
  }
}
