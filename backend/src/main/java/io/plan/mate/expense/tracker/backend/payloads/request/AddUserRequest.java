package io.plan.mate.expense.tracker.backend.payloads.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to add a user to a group")
public record AddUserRequest(
    @Schema(description = "Username of the user", example = "john")
        @NotBlank(message = "Username must not be blank")
        @Size(max = 30, message = "Username must not exceed 30 characters")
        String username) {}
