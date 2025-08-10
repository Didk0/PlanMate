package io.plan.mate.expense.tracker.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Settlement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private BigDecimal amount;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Group group;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "from_user_id")
  private User fromUser;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "to_user_id")
  private User toUser;

  @Column(name = "settled_at", nullable = false)
  private LocalDateTime settledAt = LocalDateTime.now();
}
