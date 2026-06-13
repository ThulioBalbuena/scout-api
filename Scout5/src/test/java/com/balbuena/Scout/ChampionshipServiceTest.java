package com.balbuena.Scout;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.*;
import com.balbuena.Scout.repository.MatchRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.service.ChampionshipService;
import com.balbuena.Scout.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChampionshipService")
class ChampionshipServiceTest {

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameService gameService;

    @InjectMocks
    private ChampionshipService championshipService;

    @Test
    @DisplayName("1- gera tabela quando ha presidentes suficientes")
    void generateSchedule_geraPartidasQuandoHaPresidentesSuficientes() {
        President eduardo = president(1L, "Eduardo");
        President thulio = president(2L, "Thulio");

        when(matchRepository.count()).thenReturn(0L);
        when(presidentRepository.findAll()).thenReturn(List.of(eduardo, thulio));

        List<Response.Match> result = championshipService.generateSchedule();

        verify(gameService).validatePhase(GamePhase.CHAMPIONSHIP, GamePhase.TRANSFER_WINDOW);
        verify(matchRepository).saveAll(anyList());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHomePresident()).isEqualTo("Eduardo");
        assertThat(result.get(0).getAwayPresident()).isEqualTo("Thulio");
        assertThat(result.get(1).getHomePresident()).isEqualTo("Thulio");
        assertThat(result.get(1).getAwayPresident()).isEqualTo("Eduardo");
    }

    @Test
    @DisplayName("2- lanca excecao quando tabela ja existe")
    void generateSchedule_lancaExcecaoQuandoTabelaJaExiste() {
        when(matchRepository.count()).thenReturn(1L);

        assertThatThrownBy(() -> championshipService.generateSchedule())
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("The championship schedule has already been generated");

        verify(gameService).validatePhase(GamePhase.CHAMPIONSHIP, GamePhase.TRANSFER_WINDOW);
        verify(presidentRepository, never()).findAll();
        verify(matchRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("3- lanca excecao quando ha menos de dois presidentes")
    void generateSchedule_lancaExcecaoQuandoHaMenosDeDoisPresidentes() {
        President eduardo = president(1L, "Eduardo");

        when(matchRepository.count()).thenReturn(0L);
        when(presidentRepository.findAll()).thenReturn(List.of(eduardo));

        assertThatThrownBy(() -> championshipService.generateSchedule())
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("At least 2 presidents are required to generate the schedule");

        verify(gameService).validatePhase(GamePhase.CHAMPIONSHIP, GamePhase.TRANSFER_WINDOW);
        verify(matchRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("4- simula partidas da rodada")
    void simulateRound_simulaPartidasDaRodada() {
        President eduardo = president(1L, "Eduardo");
        President thulio = president(2L, "Thulio");

        Match match = Match.builder()
                .id(1L)
                .roundNumber(1)
                .homePresident(eduardo)
                .awayPresident(thulio)
                .played(false)
                .build();

        GameState gameState = GameState.builder()
                .id(1L)
                .phase(GamePhase.CHAMPIONSHIP)
                .currentRound(0)
                .build();

        when(matchRepository.findByRoundNumberAndPlayedFalse(1)).thenReturn(List.of(match));
        when(gameService.getGameState()).thenReturn(gameState);

        List<Response.Match> result = championshipService.simulateRound(1);

        verify(gameService).validatePhase(GamePhase.CHAMPIONSHIP, GamePhase.TRANSFER_WINDOW);
        verify(matchRepository).save(match);
        verify(presidentRepository, times(2)).save(any(President.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isPlayed()).isTrue();
        assertThat(result.get(0).getHomeGoals()).isNotNull();
        assertThat(result.get(0).getAwayGoals()).isNotNull();
        assertThat(match.isPlayed()).isTrue();
        assertThat(gameState.getCurrentRound()).isEqualTo(1);
    }

    @Test
    @DisplayName("5- lanca excecao quando rodada nao existe ou ja foi simulada")
    void simulateRound_lancaExcecaoQuandoRodadaNaoExisteOuJaFoiSimulada() {
        when(matchRepository.findByRoundNumberAndPlayedFalse(99)).thenReturn(List.of());

        assertThatThrownBy(() -> championshipService.simulateRound(99))
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("Round 99 was not found or has already been played");

        verify(gameService).validatePhase(GamePhase.CHAMPIONSHIP, GamePhase.TRANSFER_WINDOW);
        verify(matchRepository, never()).save(any(Match.class));
    }

    private President president(Long id, String name) {
        List<Player> team = new ArrayList<>();

        team.add(player(id * 10 + 1, name + " Goleiro", Position.GOALKEEPER, 10.0));
        team.add(player(id * 10 + 2, name + " Zagueiro", Position.DEFENDER, 20.0));
        team.add(player(id * 10 + 3, name + " Meia", Position.MIDFIELDER, 30.0));
        team.add(player(id * 10 + 4, name + " Atacante", Position.FORWARD, 40.0));
        team.add(player(id * 10 + 5, name + " Atacante 2", Position.FORWARD, 35.0));

        President president = President.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .team(team)
                .build();

        team.forEach(player -> player.setPresident(president));

        return president;
    }

    private Player player(Long id, String name, Position position, Double value) {
        return Player.builder()
                .id(id)
                .name(name)
                .position(position)
                .value(value)
                .auctionPlayer(false)
                .available(false)
                .build();
    }
}
