package com.kdh.solvego.domain.attempt.entity;


import com.kdh.solvego.domain.common.vo.Position;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attempts")
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "x",
                    column = @Column(name = "selected_x", nullable = false)
            ),
            @AttributeOverride(
                    name = "y",
                    column = @Column(name = "selected_y", nullable = false)
            )
    })
    private Position selectedPosition;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @CreationTimestamp
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private LocalDateTime attemptedAt;

    protected Attempt() {
    }

    public Attempt(User user, Problem problem, Position selectedPosition, boolean isCorrect) {
        this.user = user;
        this.problem = problem;
        this.selectedPosition = selectedPosition;
        this.isCorrect = isCorrect;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Problem getProblem() {
        return problem;
    }

   public Position getSelectedPosition() {
        return selectedPosition;
   }

    public boolean isCorrect() {
        return isCorrect;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
