package com.balbuena.Scout.controller;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Tag(name = "1. Gerenciamento do Jogo")
public class GameController {

    private final GameService gameService;

    @GetMapping("/state")
    @Operation(summary = "Ver estado atual do jogo")
    public ResponseEntity<Response.GameState> getState() {
        return ResponseEntity.ok(gameService.buildResponse(gameService.getGameState()));
    }

    @PostMapping("/start-auction")
    @Operation(summary = "Iniciar fase de leilao", description = "Avanca de REGISTRATION para DRAFT_AUCTION.")
    public ResponseEntity<Response.ApiMessage> startAuction() {
        Response.GameState state = gameService.advanceToAuctionPhase();
        return ResponseEntity.ok(Response.ApiMessage.of(
            "🎯 Leilao iniciado! Primeiro jogador: " + state.getCurrentAuctionPlayerName(), state));
    }

    @PostMapping("/advance-auction")
    @Operation(summary = "Avancar para proximo jogador do leilao", description = "Fecha o leilao atual e passa para o proximo. Apos o 5o, vai para DRAFT_LOTTERY.")
    public ResponseEntity<Response.ApiMessage> advanceAuction() {
        Response.GameState state = gameService.advanceAuctionToNextPlayer();
        String msg = "DRAFT_LOTTERY".equals(state.getPhase().name())
            ? "✅ Leilao concluido! Fase de sorteio iniciada."
            : "➡️ Proximo jogador: " + state.getCurrentAuctionPlayerName();
        return ResponseEntity.ok(Response.ApiMessage.of(msg, state));
    }

    @PostMapping("/advance-to-championship")
    @Operation(summary = "Iniciar campeonato", description = "Avanca de DRAFT_LOTTERY para CHAMPIONSHIP.")
    public ResponseEntity<Response.ApiMessage> startChampionship() {
        Response.GameState state = gameService.advanceToChampionship();
        return ResponseEntity.ok(Response.ApiMessage.of("🏆 Campeonato iniciado!", state));
    }

    @PostMapping("/open-transfer-window")
    @Operation(summary = "Abrir janela de transferencias", description = "Disponivel apos a rodada 3.")
    public ResponseEntity<Response.ApiMessage> openTransferWindow() {
        Response.GameState state = gameService.openTransferWindow();
        return ResponseEntity.ok(Response.ApiMessage.of("🪟 Janela de transferencias aberta!", state));
    }

    @PostMapping("/close-transfer-window")
    @Operation(summary = "Fechar janela de transferencias")
    public ResponseEntity<Response.ApiMessage> closeTransferWindow() {
        Response.GameState state = gameService.closeTransferWindow();
        return ResponseEntity.ok(Response.ApiMessage.of("🔒 Janela fechada! Campeonato retomado.", state));
    }

    @PostMapping("/finish")
    @Operation(summary = "Encerrar campeonato")
    public ResponseEntity<Response.ApiMessage> finishChampionship() {
        Response.GameState state = gameService.finishChampionship();
        return ResponseEntity.ok(Response.ApiMessage.of("🏁 Campeonato encerrado! Consulte os relatorios.", state));
    }
    @PostMapping("/reset")
    @Operation(summary = "Iniciar nova temporada", description = "Limpa presidentes, lances, partidas, estatisticas dos jogadores e volta para REGISTRATION.")
    public ResponseEntity<Response.ApiMessage> resetSeason() {
        Response.GameState state = gameService.resetSeason();
        return ResponseEntity.ok(Response.ApiMessage.of("Nova temporada iniciada!", state));
    }
}   
