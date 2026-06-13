package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.GameState;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.repository.AuctionBidRepository;
import com.balbuena.Scout.repository.GameStateRepository;
import com.balbuena.Scout.repository.MatchRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    public static final int MINIMUM_PRESIDENTS = 10;
    public static final List<String> AUCTION_PLAYER_NAMES = List.of(
            "Hulk", "Memphis Depay", "Arrascaeta", "Vitor Roque", "Neymar"
    );

    private final GameStateRepository gameStateRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final PresidentRepository presidentRepository;

    public GameState getGameState() {
        return gameStateRepository.findById(1L)
                .orElseThrow(() -> new ScoutException("Game state was not found"));
    }

    @Transactional
    public Response.GameState advanceToAuctionPhase() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.REGISTRATION) {
            throw new ScoutException("This action is not available during phase: " + state.getPhase());
        }
        long presidentCount = presidentRepository.count();
        if (presidentCount < MINIMUM_PRESIDENTS) {
            throw new ScoutException("At least " + MINIMUM_PRESIDENTS
                    + " presidents are required to start the auction. Registered: " + presidentCount);
        }
        state.setPhase(GamePhase.DRAFT_AUCTION);
        state.setCurrentAuctionPlayerIndex(0);
        gameStateRepository.save(state);
        log.info("Game advanced to DRAFT_AUCTION");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState advanceAuctionToNextPlayer() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.DRAFT_AUCTION) {
            throw new ScoutException("The game is not in the auction phase. Current phase: " + state.getPhase());
        }
        int nextIndex = state.getCurrentAuctionPlayerIndex() + 1;
        if (nextIndex >= AUCTION_PLAYER_NAMES.size()) {
            state.setPhase(GamePhase.DRAFT_LOTTERY);
            log.info("Auction completed. Game advanced to DRAFT_LOTTERY");
        } else {
            state.setCurrentAuctionPlayerIndex(nextIndex);
            log.info("Next auction player: {}", AUCTION_PLAYER_NAMES.get(nextIndex));
        }
        gameStateRepository.save(state);
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState advanceToChampionship() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.DRAFT_LOTTERY) {
            throw new ScoutException("This action is not available during phase: " + state.getPhase());
        }
        state.setPhase(GamePhase.CHAMPIONSHIP);
        state.setCurrentRound(0);
        gameStateRepository.save(state);
        log.info("Championship started");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState openTransferWindow() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.CHAMPIONSHIP) {
            throw new ScoutException("Transfers can only open during the championship. Current phase: " + state.getPhase());
        }
        if (state.getCurrentRound() < 3) {
            throw new ScoutException("Transfers open after round 3. Current round: " + state.getCurrentRound());
        }
        state.setPhase(GamePhase.TRANSFER_WINDOW);
        gameStateRepository.save(state);
        log.info("Transfer window opened");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState closeTransferWindow() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.TRANSFER_WINDOW) {
            throw new ScoutException("There is no open transfer window. Current phase: " + state.getPhase());
        }
        state.setPhase(GamePhase.CHAMPIONSHIP);
        gameStateRepository.save(state);
        log.info("Transfer window closed");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState finishChampionship() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.CHAMPIONSHIP && state.getPhase() != GamePhase.TRANSFER_WINDOW) {
            throw new ScoutException("The season cannot finish during phase: " + state.getPhase());
        }
        if (matchRepository.countByPlayedFalse() > 0) {
            throw new ScoutException("All championship matches must be played before the season can finish");
        }
        state.setPhase(GamePhase.FINISHED);
        gameStateRepository.save(state);
        log.info("Season finished");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState resetSeason() {
        auctionBidRepository.deleteAll();
        matchRepository.deleteAll();

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            player.setPresident(null);
            player.setAvailable(true);
            player.setAuctionPlayer(AUCTION_PLAYER_NAMES.contains(player.getName()));
            player.setGoalsScored(0);
            player.setGoalsConceded(0);
            player.setMatchesPlayed(0);
        }
        playerRepository.saveAll(players);

        presidentRepository.deleteAll();

        GameState state = getGameState();
        state.setPhase(GamePhase.REGISTRATION);
        state.setCurrentRound(0);
        state.setCurrentAuctionPlayerIndex(0);
        state.setTotalRounds(0);
        gameStateRepository.save(state);

        log.info("New season started. Game state reset to REGISTRATION");
        return buildResponse(state);
    }

    public void validatePhase(GamePhase... allowedPhases) {
        GameState state = getGameState();
        for (GamePhase phase : allowedPhases) {
            if (state.getPhase() == phase) return;
        }
        throw new ScoutException("Operation is not allowed during phase: " + state.getPhase());
    }

    @Transactional
    public void updateChampionshipRounds(int totalRounds) {
        GameState state = getGameState();
        state.setTotalRounds(totalRounds);
        gameStateRepository.save(state);
    }

    public Response.GameState buildResponse(GameState state) {
        String currentAuctionPlayer = null;
        if (state.getPhase() == GamePhase.DRAFT_AUCTION &&
            state.getCurrentAuctionPlayerIndex() < AUCTION_PLAYER_NAMES.size()) {
            currentAuctionPlayer = AUCTION_PLAYER_NAMES.get(state.getCurrentAuctionPlayerIndex());
        }
        return Response.GameState.builder()
                .phase(state.getPhase())
                .phaseDescription(getPhaseDescription(state.getPhase()))
                .currentRound(state.getCurrentRound())
                .totalRounds(state.getTotalRounds())
                .currentAuctionPlayerIndex(state.getCurrentAuctionPlayerIndex())
                .currentAuctionPlayerName(currentAuctionPlayer)
                .build();
    }

    private String getPhaseDescription(GamePhase phase) {
        return switch (phase) {
            case REGISTRATION    -> "Register clubs and presidents";
            case DRAFT_AUCTION   -> "Auction the five featured players";
            case DRAFT_LOTTERY   -> "Complete squads through the lottery";
            case CHAMPIONSHIP    -> "Play the championship round by round";
            case TRANSFER_WINDOW -> "Transfer window is open";
            case FINISHED        -> "Season finished";
        };
    }
}
