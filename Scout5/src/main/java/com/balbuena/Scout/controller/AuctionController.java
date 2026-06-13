package com.balbuena.Scout.controller;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.service.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auction")
@RequiredArgsConstructor
@Tag(name = "4. Draft - Leilao")
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping("/current")
    @Operation(summary = "Ver jogador atual em leilao com todos os lances")
    public ResponseEntity<Response.AuctionStatus> getCurrentAuction() {
        return ResponseEntity.ok(auctionService.getCurrentAuctionStatus());
    }

    @PostMapping("/players/{playerId}/bid")
    @Operation(summary = "Fazer lance",
               description = "Lance deve ser maior que o atual e que o valor base. Presidente deve ter saldo e vaga no time.")
    public ResponseEntity<Response.AuctionStatus> placeBid(
            @PathVariable Long playerId,
            @Valid @RequestBody Request.PlaceBid request) {
        return ResponseEntity.ok(auctionService.placeBid(playerId, request));
    }

    @PostMapping("/players/{playerId}/finalize")
    @Operation(summary = "Finalizar leilao do jogador atual",
               description = "Maior licitante leva o jogador. Sem lances, jogador vai para o sorteio. Avanca automaticamente.")
    public ResponseEntity<Response.ApiMessage> finalizeAuction(@PathVariable Long playerId) {
        Response.AuctionStatus status = auctionService.finalizeCurrentAuction();
        return ResponseEntity.ok(Response.ApiMessage.of("Player auction finalized.", status));
    }
}
