package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.model.President;
import com.balbuena.Scout.repository.PresidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresidentService {

    private static final List<DefaultClub> DEFAULT_CLUBS = List.of(
            new DefaultClub("Atlético Mineiro", "Rafael Menin", "rafael.menin@email.com"),
            new DefaultClub("Palmeiras", "Leila Pereira", "leila.pereira@email.com"),
            new DefaultClub("Flamengo", "Luiz Baptista", "luiz.baptista@email.com"),
            new DefaultClub("Cruzeiro", "Pedro Lourenço", "pedro.lourenco@email.com"),
            new DefaultClub("Corinthians", "Osmar Stabile", "osmar.stabile@email.com"),
            new DefaultClub("São Paulo", "Harry Massis", "harry.massis@email.com"),
            new DefaultClub("Santos", "Marcelo Teixeira", "marcelo.teixeira@email.com"),
            new DefaultClub("Fluminense", "Mattheus Montenegro", "mattheus.montenegro@email.com"),
            new DefaultClub("Grêmio", "Odorico Roman", "odorico.roman@email.com"),
            new DefaultClub("Internacional", "Alessandro Barcellos", "alessandro.barcellos@email.com")
    );

    private final PresidentRepository presidentRepository;
    private final GameService gameService;

    @Transactional
    public Response.President create(Request.PresidentCreate request) {
        gameService.validatePhase(GamePhase.REGISTRATION);
        if (presidentRepository.existsByEmail(request.getEmail())) {
            throw new ScoutException("Email is already registered: " + request.getEmail());
        }
        if (presidentRepository.existsByName(request.getName())) {
            throw new ScoutException("President name is already registered: " + request.getName());
        }
        if (presidentRepository.existsByClubName(request.getClubName())) {
            throw new ScoutException("Club is already registered: " + request.getClubName());
        }
        President president = President.builder()
                .name(request.getName())
                .email(request.getEmail())
                .clubName(request.getClubName())
                .build();
        return toResponse(presidentRepository.save(president));
    }

    @Transactional
    public List<Response.President> createDefaults() {
        gameService.validatePhase(GamePhase.REGISTRATION);

        for (DefaultClub club : DEFAULT_CLUBS) {
            boolean alreadyRegistered = presidentRepository.existsByEmail(club.email())
                    || presidentRepository.existsByName(club.presidentName())
                    || presidentRepository.existsByClubName(club.clubName());
            if (!alreadyRegistered) {
                presidentRepository.save(President.builder()
                        .name(club.presidentName())
                        .email(club.email())
                        .clubName(club.clubName())
                        .build());
            }
        }

        return findAll();
    }

    public List<Response.President> findAll() {
        return presidentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Response.President findById(Long id) {
        return toResponse(getPresident(id));
    }

    public President getPresident(Long id) {
        return presidentRepository.findById(id)
                .orElseThrow(() -> new ScoutException("President not found: ID " + id));
    }

    public Response.President toResponse(President p) {
        List<Response.Player> team = p.getTeam().stream()
                .map(this::playerToResponse)
                .collect(Collectors.toList());
        return Response.President.builder()
                .id(p.getId())
                .name(p.getName())
                .email(p.getEmail())
                .clubName(p.getClubName())
                .budget(p.getBudget())
                .usedBudget(p.getUsedBudget())
                .teamComplete(p.isTeamComplete())
                .hasGoalkeeper(p.hasGoalkeeper())
                .team(team)
                .points(p.getPoints())
                .wins(p.getWins())
                .draws(p.getDraws())
                .losses(p.getLosses())
                .goalsFor(p.getGoalsFor())
                .goalsAgainst(p.getGoalsAgainst())
                .goalDifference(p.getGoalDifference())
                .transferUsed(p.isTransferUsed())
                .build();
    }

    public Response.Player playerToResponse(Player p) {
        return Response.Player.builder()
                .id(p.getId())
                .name(p.getName())
                .position(p.getPosition())
                .value(p.getValue())
                .auctionPlayer(p.isAuctionPlayer())
                .available(p.isAvailable())
                .goalsScored(p.getGoalsScored())
                .goalsConceded(p.getGoalsConceded())
                .presidentName(p.getPresident() != null ? p.getPresident().getName() : null)
                .build();
    }

    private record DefaultClub(String clubName, String presidentName, String email) {
    }
}
