package com.balbuena.Scout.config;

import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.GameState;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.model.Position;
import com.balbuena.Scout.repository.GameStateRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component //Cria a lista quando a aplicacao iniciar
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final GameStateRepository gameStateRepository;

    @Override
    public void run(String... args) {
        if (playerRepository.count() == 0) {
            initializePlayers();
        }
        ensureAuctionPlayers();
        if (gameStateRepository.count() == 0) {
            initializeGameState();
        }
    }

    private void ensureAuctionPlayers() {
        if (playerRepository.findByName("Memphis Depay").isEmpty()) {
            playerRepository.save(Player.builder()
                    .name("Memphis Depay")
                    .position(Position.FORWARD)
                    .value(21.0)
                    .auctionPlayer(true)
                    .available(true)
                    .build());
        }

        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            player.setAuctionPlayer(GameService.AUCTION_PLAYER_NAMES.contains(player.getName()));
        }
        playerRepository.saveAll(players);
    }

    private void initializePlayers() {
        List<Player> players = List.of(
                // === GOLEIROS (8) ===
                Player.builder().name("Carlos Miguel").position(Position.GOALKEEPER).value(14.0).auctionPlayer(false).available(true).build(), // Palmeiras
                Player.builder().name("Hugo Souza").position(Position.GOALKEEPER).value(13.0).auctionPlayer(false).available(true).build(),   // Corinthians
                Player.builder().name("Fábio").position(Position.GOALKEEPER).value(12.0).auctionPlayer(false).available(true).build(),        // Fluminense
                Player.builder().name("Rossi").position(Position.GOALKEEPER).value(11.0).auctionPlayer(false).available(true).build(),        // Flamengo
                Player.builder().name("Everson").position(Position.GOALKEEPER).value(13.0).auctionPlayer(false).available(true).build(),      // Atlético-MG
                Player.builder().name("Cássio").position(Position.GOALKEEPER).value(12.0).auctionPlayer(false).available(true).build(),       // Cruzeiro
                Player.builder().name("Gabriel Brazão").position(Position.GOALKEEPER).value(11.0).auctionPlayer(false).available(true).build(), // Santos
                Player.builder().name("Rochet").position(Position.GOALKEEPER).value(12.0).auctionPlayer(false).available(true).build(),       // Internacional

// === DEFENSORES (16) ===
                Player.builder().name("Barboza").position(Position.DEFENDER).value(13.0).auctionPlayer(false).available(true).build(),        // Botafogo
                Player.builder().name("Gustavo Gómez").position(Position.DEFENDER).value(15.0).auctionPlayer(true).available(true).build(),  // Palmeiras
                Player.builder().name("Léo Ortiz").position(Position.DEFENDER).value(13.0).auctionPlayer(true).available(true).build(),    // Flamengo
                Player.builder().name("Fabrício Bruno").position(Position.DEFENDER).value(13.0).auctionPlayer(false).available(true).build(), // Cruzeiro
                Player.builder().name("Renan Lodi").position(Position.DEFENDER).value(12.0).auctionPlayer(false).available(true).build(),     // Atlético-MG
                Player.builder().name("Piquerez").position(Position.DEFENDER).value(11.0).auctionPlayer(false).available(true).build(),       // Palmeiras
                Player.builder().name("Jemmes").position(Position.DEFENDER).value(11.0).auctionPlayer(false).available(true).build(),         // Fluminense
                Player.builder().name("Arboleda").position(Position.DEFENDER).value(12.0).auctionPlayer(false).available(true).build(),       // São Paulo
                Player.builder().name("Vitão").position(Position.DEFENDER).value(12.0).auctionPlayer(false).available(true).build(),          // Flamengo
                Player.builder().name("Murilo").position(Position.DEFENDER).value(12.0).auctionPlayer(false).available(true).build(),         // Palmeiras
                Player.builder().name("Gabriel Paulista").position(Position.DEFENDER).value(11.0).auctionPlayer(false).available(true).build(), // Corinthians
                Player.builder().name("Luciano Juba").position(Position.DEFENDER).value(11.0).auctionPlayer(false).available(true).build(),   // Bahia
                Player.builder().name("Ramos Mingo").position(Position.DEFENDER).value(10.0).auctionPlayer(false).available(true).build(),    // Bahia
                Player.builder().name("Luan Peres").position(Position.DEFENDER).value(10.0).auctionPlayer(false).available(true).build(),     // Santos
                Player.builder().name("Adonís Frías").position(Position.DEFENDER).value(10.0).auctionPlayer(false).available(true).build(),   // Santos
                Player.builder().name("Bernabei").position(Position.DEFENDER).value(10.0).auctionPlayer(false).available(true).build(),       // Internacional

// === MEIO-CAMPISTAS (18) ===
                Player.builder().name("Arrascaeta").position(Position.MIDFIELDER).value(21.0).auctionPlayer(true).available(true).build(),    // Flamengo
                Player.builder().name("Gerson").position(Position.MIDFIELDER).value(18.0).auctionPlayer(false).available(true).build(),       // Cruzeiro
                Player.builder().name("Alan Patrick").position(Position.MIDFIELDER).value(17.0).auctionPlayer(false).available(true).build(), // Internacional
                Player.builder().name("Matheus Pereira").position(Position.MIDFIELDER).value(18.0).auctionPlayer(true).available(true).build(), // Cruzeiro
                Player.builder().name("Andreas Pereira").position(Position.MIDFIELDER).value(18.0).auctionPlayer(false).available(true).build(), // Palmeiras
                Player.builder().name("Everton Ribeiro").position(Position.MIDFIELDER).value(15.0).auctionPlayer(false).available(true).build(), // Bahia
                Player.builder().name("Jean Lucas").position(Position.MIDFIELDER).value(14.0).auctionPlayer(false).available(true).build(),   // Bahia
                Player.builder().name("Jorginho").position(Position.MIDFIELDER).value(16.0).auctionPlayer(false).available(true).build(),     // Flamengo
                Player.builder().name("Lucas Romero").position(Position.MIDFIELDER).value(13.0).auctionPlayer(false).available(true).build(), // Cruzeiro
                Player.builder().name("Marlon Freitas").position(Position.MIDFIELDER).value(15.0).auctionPlayer(false).available(true).build(), // Palmeiras
                Player.builder().name("Maurício").position(Position.MIDFIELDER).value(14.0).auctionPlayer(false).available(true).build(),     // Palmeiras
                Player.builder().name("Savarino").position(Position.MIDFIELDER).value(15.0).auctionPlayer(false).available(true).build(),     // Fluminense
                Player.builder().name("Martinelli").position(Position.MIDFIELDER).value(12.0).auctionPlayer(false).available(true).build(),   // Fluminense
                Player.builder().name("Marcos Antônio").position(Position.MIDFIELDER).value(12.0).auctionPlayer(false).available(true).build(), // São Paulo
                Player.builder().name("Neymar").position(Position.MIDFIELDER).value(22.0).auctionPlayer(true).available(true).build(),        // Santos
                Player.builder().name("Gabriel Bontempo").position(Position.MIDFIELDER).value(11.0).auctionPlayer(false).available(true).build(), // Santos
                Player.builder().name("Zapelli").position(Position.MIDFIELDER).value(12.0).auctionPlayer(false).available(true).build(),      // Athletico-PR
                Player.builder().name("Patrick de Paula").position(Position.MIDFIELDER).value(10.0).auctionPlayer(false).available(true).build(), // Remo

// === ATACANTES (18) ===
                Player.builder().name("Hulk").position(Position.FORWARD).value(22.0).auctionPlayer(true).available(true).build(),             // Atlético-MG
                Player.builder().name("Vitor Roque").position(Position.FORWARD).value(21.0).auctionPlayer(true).available(true).build(),      // Palmeiras
                Player.builder().name("Pedro").position(Position.FORWARD).value(19.0).auctionPlayer(false).available(true).build(),            // Flamengo
                Player.builder().name("Willian José").position(Position.FORWARD).value(13.0).auctionPlayer(false).available(true).build(),    // Bahia
                Player.builder().name("Gabriel Barbosa").position(Position.FORWARD).value(18.0).auctionPlayer(false).available(true).build(), // Santos
                Player.builder().name("Yuri Alberto").position(Position.FORWARD).value(17.0).auctionPlayer(false).available(true).build(),    // Corinthians
                Player.builder().name("Calleri").position(Position.FORWARD).value(16.0).auctionPlayer(false).available(true).build(),         // São Paulo
                Player.builder().name("Kaio Jorge").position(Position.FORWARD).value(18.0).auctionPlayer(false).available(true).build(),      // Cruzeiro
                Player.builder().name("Flaco López").position(Position.FORWARD).value(17.0).auctionPlayer(false).available(true).build(),     // Palmeiras
                Player.builder().name("Luciano").position(Position.FORWARD).value(15.0).auctionPlayer(false).available(true).build(),         // São Paulo
                Player.builder().name("Lucas").position(Position.FORWARD).value(18.0).auctionPlayer(true).available(true).build(),           // São Paulo
                Player.builder().name("Borré").position(Position.FORWARD).value(16.0).auctionPlayer(false).available(true).build(),           // Internacional
                Player.builder().name("Carbonero").position(Position.FORWARD).value(14.0).auctionPlayer(false).available(true).build(),       // Internacional
                Player.builder().name("Rony").position(Position.FORWARD).value(15.0).auctionPlayer(false).available(true).build(),            // Santos
                Player.builder().name("John Kennedy").position(Position.FORWARD).value(14.0).auctionPlayer(false).available(true).build(),    // Fluminense
                Player.builder().name("Canobbio").position(Position.FORWARD).value(14.0).auctionPlayer(false).available(true).build(),        // Fluminense
                Player.builder().name("Eduardo Sasha").position(Position.FORWARD).value(13.0).auctionPlayer(false).available(true).build(),   // Bragantino
                Player.builder().name("Viveros").position(Position.FORWARD).value(12.0).auctionPlayer(false).available(true).build()          // Athletico-PR
        );

        playerRepository.saveAll(players);
        log.info("{} players initialized", players.size());
    }

    private void initializeGameState() {
        GameState state = GameState.builder()
                .id(1L)
                .phase(GamePhase.REGISTRATION)
                .currentRound(0)
                .currentAuctionPlayerIndex(0)
                .totalRounds(0)
                .build();
        gameStateRepository.save(state);
        log.info("Game state initialized in phase {}", state.getPhase());
    }
}
