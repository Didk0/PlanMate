package io.plan.mate.expense.tracker.backend.entities;

        import jakarta.persistence.Entity;
        import jakarta.persistence.GeneratedValue;
        import jakarta.persistence.Id;
        import jakarta.persistence.JoinColumn;
        import jakarta.persistence.ManyToOne;
        import jakarta.persistence.Table;

        import java.math.BigDecimal;

@Entity
@Table(name = "expense_participants")
public class ExpenseParticipant {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Member participant;

    private BigDecimal amountOwed;
}
