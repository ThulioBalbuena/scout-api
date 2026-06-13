package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public List<Response.Player> findAll() {
        return playerRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<Response.Player> findAvailable() {
        return playerRepository.findByAvailableTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<Response.Player> findAuctionPlayers() {
        return playerRepository.findByAuctionPlayerTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Response.Player findById(Long id) {
        return toResponse(getPlayer(id));
    }

    public Player getPlayer(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ScoutException("Player not found: ID " + id));
    }

    public Response.Player toResponse(Player p) {
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
}
