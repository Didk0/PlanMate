package io.plan.mate.expense.tracker.backend.payloads.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new group")
public record CreateGroupRequest(
    @Schema(description = "Name of the group", example = "Trip to Spain")
        @NotBlank(message = "Group name must not be blank")
        @Size(max = 30, message = "Group name must not exceed 30 characters")
        String name,
    @Schema(description = "Description of the group", example = "This is a group for the trip to Madrid.")
    @Size(max = 100, message = "Group description must not exceed 100 characters")
    String description) {}
