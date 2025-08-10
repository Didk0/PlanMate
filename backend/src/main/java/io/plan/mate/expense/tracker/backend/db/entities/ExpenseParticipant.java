package io.plan.mate.expense.tracker.backend.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "expense_participants",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"expense_id", "user_id"})})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExpenseParticipant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "share_amount", nullable = false)
  private BigDecimal shareAmount;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "expense_id")
  private Expense expense;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Setter
  User participant;
}
