package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.model.President;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PresidentRepository presidentRepository;
    private final PlayerRepository playerRepository;

    public List<Response.Standing> getStandings() {
        AtomicInteger pos = new AtomicInteger(1);
        return presidentRepository.findAllOrderedByStandings().stream()
                .map(p -> toStanding(p, pos.getAndIncrement()))
                .collect(Collectors.toList());
    }

    public List<Response.TopScorer> getTopScorers() {
        AtomicInteger rank = new AtomicInteger(1);
        return playerRepository.findAll().stream()
                .filter(p -> p.getGoalsScored() > 0)
                .sorted(Comparator.comparingInt(Player::getGoalsScored).reversed())
                .limit(10)
                .map(p -> Response.TopScorer.builder()
                        .rank(rank.getAndIncrement())
                        .playerName(p.getName())
                        .position(p.getPosition())
                        .presidentName(p.getPresident() != null ? p.getPresident().getName() : "Available")
                        .goals(p.getGoalsScored())
                        .build())
                .collect(Collectors.toList());
    }

    public Response.Standing getBestAttack() {
        President best = presidentRepository.findAll().stream()
                .max(Comparator.comparingInt(President::getGoalsFor))
                .orElseThrow();
        return toStanding(best, 1);
    }

    public Response.Standing getBestDefense() {
        President best = presidentRepository.findAll().stream()
                .min(Comparator.comparingInt(President::getGoalsAgainst))
                .orElseThrow();
        return toStanding(best, 1);
    }

    public Response.ChampionshipReport getFullReport() {
        List<Response.Standing> standings = getStandings();
        return Response.ChampionshipReport.builder()
                .standings(standings)
                .topScorers(getTopScorers())
                .bestAttack(standings.isEmpty() ? null : getBestAttack())
                .bestDefense(standings.isEmpty() ? null : getBestDefense())
                .champion(standings.isEmpty() ? null : displayClub(standings.get(0)))
                .build();
    }

    private Response.Standing toStanding(President p, int position) {
        return Response.Standing.builder()
                .position(position)
                .presidentName(p.getName())
                .clubName(p.getClubName())
                .points(p.getPoints())
                .wins(p.getWins())
                .draws(p.getDraws())
                .losses(p.getLosses())
                .goalsFor(p.getGoalsFor())
                .goalsAgainst(p.getGoalsAgainst())
                .goalDifference(p.getGoalDifference())
                .matchesPlayed(p.getWins() + p.getDraws() + p.getLosses())
                .build();
    }

    private String displayClub(Response.Standing standing) {
        return standing.getClubName() != null ? standing.getClubName() : standing.getPresidentName();
    }
}
