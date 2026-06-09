package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.model.Position;
import com.balbuena.Scout.model.President;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService")
class ReportServiceTest {

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private ReportService reportService;

    // -----------------------------------------------
    // Helpers para montar objetos de teste

    private President makePresident(String name, int points, int wins, int draws, int losses,
                                    int goalsFor, int goalsAgainst) {
        President p = new President();
        p.setName(name);
        p.setPoints(points);
        p.setWins(wins);
        p.setDraws(draws);
        p.setLosses(losses);
        p.setGoalsFor(goalsFor);
        p.setGoalsAgainst(goalsAgainst);
        return p;
    }

    private Player makePlayer(String name, Position position, int goalsScored, President president) {
        Player p = new Player();
        p.setName(name);
        p.setPosition(position);
        p.setGoalsScored(goalsScored);
        p.setPresident(president);
        return p;
    }

    // ---------------
    // FLUXO NORMAL

    @Nested
    @DisplayName("Fluxo Normal")
    class FluxoNormal {

        @Test
        @DisplayName("01 - getStandings retorna classificação ordenada")
        void getStandings_retornaClassificacao() {
            President p1 = makePresident("Time A", 9, 3, 0, 0, 7, 2);
            President p2 = makePresident("Time B", 3, 1, 0, 2, 3, 5);

            // findAllOrderedByStandings já retorna os presidentes na ordem correta pelo banco
            when(presidentRepository.findAllOrderedByStandings()).thenReturn(List.of(p1, p2));

            List<Response.Standing> result = reportService.getStandings();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPresidentName()).isEqualTo("Time A"); // primeiro colocado
            assertThat(result.get(0).getPosition()).isEqualTo(1);            // posição 1
            assertThat(result.get(1).getPosition()).isEqualTo(2);            // posição 2
        }

        @Test
        @DisplayName("02 - getTopScorers retorna artilheiros quando existem gols marcados")
        void getTopScorers_retornaArtilheiros() {
            President president = makePresident("Time A", 9, 3, 0, 0, 7, 2);

            Player top    = makePlayer("Neymar",  Position.FORWARD,  5, president); // artilheiro
            Player second = makePlayer("Hulk",    Position.FORWARD,  3, president); // segundo
            Player noGoal = makePlayer("Goleiro", Position.GOALKEEPER, 0, president); // sem gols, não deve aparecer

            when(playerRepository.findAll()).thenReturn(List.of(top, second, noGoal));

            List<Response.TopScorer> result = reportService.getTopScorers();

            assertThat(result).hasSize(2);                                     // jogador sem gol é filtrado
            assertThat(result.get(0).getPlayerName()).isEqualTo("Neymar");     // maior artilheiro primeiro
            assertThat(result.get(0).getGoals()).isEqualTo(5);
            assertThat(result.get(1).getPlayerName()).isEqualTo("Hulk");
        }

        @Test
        @DisplayName("03 - getBestAttack retorna presidente com mais gols marcados")
        void getBestAttack_retornaMelhorAtaque() {
            President melhorAtaque = makePresident("Time A", 9, 3, 0, 0, 10, 2); // 10 gols marcados
            President piorAtaque   = makePresident("Time B", 3, 1, 0, 2,  3, 5); //  3 gols marcados

            when(presidentRepository.findAll()).thenReturn(List.of(melhorAtaque, piorAtaque));

            Response.Standing result = reportService.getBestAttack();

            assertThat(result.getPresidentName()).isEqualTo("Time A"); // quem tem mais gols marcados
            assertThat(result.getGoalsFor()).isEqualTo(10);
        }

        @Test
        @DisplayName("04 - getBestDefense retorna presidente com menos gols sofridos")
        void getBestDefense_retornaMelhorDefesa() {
            President melhorDefesa = makePresident("Time A", 9, 3, 0, 0, 7, 1); // 1 gol sofrido
            President piorDefesa   = makePresident("Time B", 3, 1, 0, 2, 3, 8); // 8 gols sofridos

            when(presidentRepository.findAll()).thenReturn(List.of(melhorDefesa, piorDefesa));

            Response.Standing result = reportService.getBestDefense();

            assertThat(result.getPresidentName()).isEqualTo("Time A"); // quem sofreu menos gols
            assertThat(result.getGoalsAgainst()).isEqualTo(1);
        }

        @Test
        @DisplayName("05 - getFullReport retorna relatório completo com campeão")
        void getFullReport_retornaRelatorioCompleto() {
            President lider   = makePresident("Time A", 9, 3, 0, 0, 7, 1);
            President segundo = makePresident("Time B", 3, 1, 0, 2, 3, 8);

            Player artilheiro = makePlayer("Neymar", Position.FORWARD, 5, lider);

            // getStandings usa findAllOrderedByStandings
            when(presidentRepository.findAllOrderedByStandings()).thenReturn(List.of(lider, segundo));
            // getBestAttack e getBestDefense usam findAll
            when(presidentRepository.findAll()).thenReturn(List.of(lider, segundo));
            // getTopScorers usa playerRepository.findAll
            when(playerRepository.findAll()).thenReturn(List.of(artilheiro));

            Response.ChampionshipReport result = reportService.getFullReport();

            assertThat(result).isNotNull();
            assertThat(result.getChampion()).isEqualTo("Time A");         // líder da classificação
            assertThat(result.getStandings()).hasSize(2);                  // dois times na tabela
            assertThat(result.getTopScorers()).hasSize(1);                 // um artilheiro
            assertThat(result.getBestAttack()).isNotNull();
            assertThat(result.getBestDefense()).isNotNull();
        }
    }
}
