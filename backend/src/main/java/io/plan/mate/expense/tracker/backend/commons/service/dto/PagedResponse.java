package io.plan.mate.expense.tracker.backend.commons.service.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {

  public static <T> PagedResponse<T> from(final Page<T> page) {
    return new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }

  public static <E, T> PagedResponse<T> from(final Page<E> page, final Function<E, T> mapper) {
    return from(page.map(mapper));
  }
}
