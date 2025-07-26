package io.plan.mate.expense.tracker.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "paidBy")
    private List<Expense> expensesPaid = new ArrayList<>();

    @OneToMany(mappedBy = "participant")
    private List<ExpenseParticipant> owedExpenses = new ArrayList<>();
}
