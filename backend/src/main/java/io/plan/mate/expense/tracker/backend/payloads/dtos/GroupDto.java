package io.plan.mate.expense.tracker.backend.payloads.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
