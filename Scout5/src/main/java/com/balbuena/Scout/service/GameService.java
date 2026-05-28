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

    private static final String[] AUCTION_PLAYER_NAMES = {
        "Hulk", "Memphis Depay", "Arrascaeta", "Vitor Roque", "Neymar"
    };

    private final GameStateRepository gameStateRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final PresidentRepository presidentRepository;

    public GameState getGameState() {
        return gameStateRepository.findById(1L)
                .orElseThrow(() -> new ScoutException("Estado do jogo nao encontrado"));
    }

    @Transactional
    public Response.GameState advanceToAuctionPhase() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.REGISTRATION) {
            throw new ScoutException("Fase atual nao permite esta acao: " + state.getPhase());
        }
        state.setPhase(GamePhase.DRAFT_AUCTION);
        state.setCurrentAuctionPlayerIndex(0);
        gameStateRepository.save(state);
        log.info("🎯 Fase avancada para DRAFT_AUCTION");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState advanceAuctionToNextPlayer() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.DRAFT_AUCTION) {
            throw new ScoutException("Nao estamos na fase de leilao. Fase atual: " + state.getPhase());
        }
        int nextIndex = state.getCurrentAuctionPlayerIndex() + 1;
        if (nextIndex >= AUCTION_PLAYER_NAMES.length) {
            state.setPhase(GamePhase.DRAFT_LOTTERY);
            log.info("✅ Leilao concluido! Avancando para DRAFT_LOTTERY");
        } else {
            state.setCurrentAuctionPlayerIndex(nextIndex);
            log.info("➡️ Proximo no leilao: {}", AUCTION_PLAYER_NAMES[nextIndex]);
        }
        gameStateRepository.save(state);
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState advanceToChampionship() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.DRAFT_LOTTERY) {
            throw new ScoutException("Fase atual nao permite esta acao: " + state.getPhase());
        }
        state.setPhase(GamePhase.CHAMPIONSHIP);
        state.setCurrentRound(0);
        gameStateRepository.save(state);
        log.info("🏆 Campeonato iniciado!");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState openTransferWindow() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.CHAMPIONSHIP) {
            throw new ScoutException("Janela so abre durante o campeonato. Fase: " + state.getPhase());
        }
        if (state.getCurrentRound() < 3) {
            throw new ScoutException("Janela abre apenas apos a rodada 3. Rodada atual: " + state.getCurrentRound());
        }
        state.setPhase(GamePhase.TRANSFER_WINDOW);
        gameStateRepository.save(state);
        log.info("🪟 Janela de transferencias aberta!");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState closeTransferWindow() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.TRANSFER_WINDOW) {
            throw new ScoutException("Nao ha janela aberta. Fase: " + state.getPhase());
        }
        state.setPhase(GamePhase.CHAMPIONSHIP);
        gameStateRepository.save(state);
        log.info("🔒 Janela de transferencias fechada!");
        return buildResponse(state);
    }

    @Transactional
    public Response.GameState finishChampionship() {
        GameState state = getGameState();
        if (state.getPhase() != GamePhase.CHAMPIONSHIP && state.getPhase() != GamePhase.TRANSFER_WINDOW) {
            throw new ScoutException("Nao e possivel encerrar agora. Fase: " + state.getPhase());
        }
        state.setPhase(GamePhase.FINISHED);
        gameStateRepository.save(state);
        log.info("🏁 Campeonato encerrado!");
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
        state.setTotalRounds(6);
        gameStateRepository.save(state);

        log.info("Nova temporada iniciada. Estado resetado para REGISTRATION");
        return buildResponse(state);
    }

    public void validatePhase(GamePhase... allowedPhases) {
        GameState state = getGameState();
        for (GamePhase phase : allowedPhases) {
            if (state.getPhase() == phase) return;
        }
        throw new ScoutException("Operacao nao permitida na fase: " + state.getPhase());
    }

    public Response.GameState buildResponse(GameState state) {
        String currentAuctionPlayer = null;
        if (state.getPhase() == GamePhase.DRAFT_AUCTION &&
            state.getCurrentAuctionPlayerIndex() < AUCTION_PLAYER_NAMES.length) {
            currentAuctionPlayer = AUCTION_PLAYER_NAMES[state.getCurrentAuctionPlayerIndex()];
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
            case REGISTRATION    -> "📝 Cadastro de presidentes";
            case DRAFT_AUCTION   -> "🎯 Leilao dos jogadores especiais";
            case DRAFT_LOTTERY   -> "🎰 Sorteio dos demais jogadores";
            case CHAMPIONSHIP    -> "⚽ Campeonato em andamento";
            case TRANSFER_WINDOW -> "🪟 Janela de transferencias aberta";
            case FINISHED        -> "🏁 Campeonato encerrado";
        };
    }
}
