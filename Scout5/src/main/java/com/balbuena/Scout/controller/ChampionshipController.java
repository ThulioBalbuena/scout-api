package com.balbuena.Scout.controller;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.service.ChampionshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/championship")
@RequiredArgsConstructor
@Tag(name = "6. Campeonato")
public class ChampionshipController {

    private final ChampionshipService championshipService;

    @PostMapping("/generate-schedule")
    @Operation(summary = "Gerar tabela de jogos (round-robin)", description = "Gera todas as partidas. Chamar uma vez apos iniciar o campeonato.")
    public ResponseEntity<Response.ApiMessage> generateSchedule() {
        List<Response.Match> matches = championshipService.generateSchedule();
        return ResponseEntity.ok(Response.ApiMessage.of(matches.size() + " matches generated.", matches));
    }

    @GetMapping("/matches")
    @Operation(summary = "Listar todas as partidas")
    public ResponseEntity<List<Response.Match>> getAllMatches() {
        return ResponseEntity.ok(championshipService.getAllMatches());
    }

    @GetMapping("/matches/round/{roundNumber}")
    @Operation(summary = "Listar partidas de uma rodada")
    public ResponseEntity<List<Response.Match>> getMatchesByRound(@PathVariable int roundNumber) {
        return ResponseEntity.ok(championshipService.getMatchesByRound(roundNumber));
    }

    @PostMapping("/simulate/round/{roundNumber}")
    @Operation(summary = "Simular rodada especifica",
               description = "Simula com base na forca do time. Fator casa: +10%. Gols distribuidos por posicao.")
    public ResponseEntity<Response.ApiMessage> simulateRound(@PathVariable int roundNumber) {
        List<Response.Match> results = championshipService.simulateRound(roundNumber);
        return ResponseEntity.ok(Response.ApiMessage.of("Round " + roundNumber + " played.", results));
    }

    @PostMapping("/simulate/all")
    @Operation(summary = "Simular todas as rodadas pendentes")
    public ResponseEntity<Response.ApiMessage> simulateAll() {
        List<Response.Match> results = championshipService.simulateAllPendingRounds();
        return ResponseEntity.ok(Response.ApiMessage.of(results.size() + " matches played.", results));
    }
}
