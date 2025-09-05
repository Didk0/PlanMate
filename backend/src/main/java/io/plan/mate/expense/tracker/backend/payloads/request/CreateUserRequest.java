package io.plan.mate.expense.tracker.backend.payloads.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Request to create a new user")
public record CreateUserRequest(
    @Schema(description = "Username of the user", example = "john.doe")
        @NotBlank(message = "Name must not be blank")
        @Size(max = 30, message = "Name must not exceed 30 characters")
        String username,
    @Schema(description = "Email address of the user", example = "john.doe@gmail.com")
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        String email,
    @Schema(description = "First name", example = "John")
    String firstName,
    @Schema(description = "Last name", example = "Doe")
    String lastName,
    @Schema(description = "Keycloak user ID", example = "1f8b6d02-5b66-4b2d-9f2e-2a01f91e2a11")
    UUID keycloakId) {}
