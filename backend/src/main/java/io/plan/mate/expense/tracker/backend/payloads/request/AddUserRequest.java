package io.plan.mate.expense.tracker.backend.payloads.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new user")
public record AddUserRequest(
    @Schema(description = "Name of the user", example = "John Doe")
        @NotBlank(message = "Name must not be blank")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        String name) {}
