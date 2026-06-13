package com.balbuena.Scout.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "presidents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class President {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "club_name", unique = true)
    private String clubName;

    @Builder.Default //@builder.default estabelece que na criação desse classe, o valor sempre será esse
    private Double budget = 100.0;

    @Column(name = "used_budget")
    @Builder.Default
    private Double usedBudget = 0.0;

    @OneToMany(mappedBy = "president", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Player> team = new ArrayList<>();

    @Builder.Default
    private int points = 0;

    @Builder.Default
    private int wins = 0;

    @Builder.Default
    private int draws = 0;

    @Builder.Default
    private int losses = 0;

    @Column(name = "goals_for")
    @Builder.Default
    private int goalsFor = 0;

    @Column(name = "goals_against")
    @Builder.Default
    private int goalsAgainst = 0;

    @Column(name = "transfer_used")
    @Builder.Default
    private boolean transferUsed = false;

    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    public boolean hasGoalkeeper() {
        return team.stream().anyMatch(p -> p.getPosition() == Position.GOALKEEPER);
    }

    public boolean isTeamComplete() {
        return team.size() == 5;
    }
}
