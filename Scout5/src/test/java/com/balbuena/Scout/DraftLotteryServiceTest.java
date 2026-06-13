package com.balbuena.Scout;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.model.Position;
import com.balbuena.Scout.model.President;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.service.DraftLotteryService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DraftLotteryService")
class DraftLotteryServiceTest {

    @Mock
    private ChampionshipService championshipService;

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameService gameService;
    @InjectMocks
    private DraftLotteryService draftLotteryService;

    @Test
    @DisplayName("1- distribui jogadores para presidente com time incompleto")
    void runLottery_distribuiJogadoresParaPresidenteComTimeIncompleto() {
        President president = president(1L, "Eduardo", new ArrayList<>());

        Player goalkeeper = player(1L, "Goleiro", Position.GOALKEEPER, false, true);
        Player defender = player(2L, "Zagueiro", Position.DEFENDER, false, true);
        Player midfielder = player(3L, "Meia", Position.MIDFIELDER, false, true);
        Player forward = player(4L, "Atacante", Position.FORWARD, false, true);
        Player auctionPlayer = player(5L, "Craque", Position.FORWARD, true, true);

        when(presidentRepository.findAll()).thenReturn(List.of(president));
        when(playerRepository.findByAuctionPlayerFalseAndAvailableTrue())
                .thenReturn(List.of(goalkeeper, defender, midfielder, forward));
        when(playerRepository.findByAuctionPlayerTrue())
                .thenReturn(List.of(auctionPlayer));

        String result = draftLotteryService.runLottery();

        verify(gameService).validatePhase(GamePhase.DRAFT_LOTTERY);
        verify(playerRepository, times(5)).save(org.mockito.ArgumentMatchers.any(Player.class));

        assertThat(result).contains("Lottery result");
        assertThat(result).contains("Eduardo");
        assertThat(result).contains("Lottery completed");

        assertThat(goalkeeper.getPresident()).isSameAs(president);
        assertThat(defender.getPresident()).isSameAs(president);
        assertThat(midfielder.getPresident()).isSameAs(president);
        assertThat(forward.getPresident()).isSameAs(president);
        assertThat(auctionPlayer.getPresident()).isSameAs(president);

        assertThat(goalkeeper.isAvailable()).isFalse();
        assertThat(defender.isAvailable()).isFalse();
        assertThat(midfielder.isAvailable()).isFalse();
        assertThat(forward.isAvailable()).isFalse();
        assertThat(auctionPlayer.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("2- lanca excecao quando nao ha presidentes cadastrados")
    void runLottery_lancaExcecaoQuandoNaoHaPresidentes() {
        when(presidentRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> draftLotteryService.runLottery())
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("No presidents are registered");

        verify(gameService).validatePhase(GamePhase.DRAFT_LOTTERY);
    }

    @Test
    @DisplayName("3- lanca excecao quando nao ha jogadores disponiveis")
    void runLottery_lancaExcecaoQuandoNaoHaJogadoresDisponiveis() {
        President president = president(1L, "Eduardo", new ArrayList<>());

        when(presidentRepository.findAll()).thenReturn(List.of(president));
        when(playerRepository.findByAuctionPlayerFalseAndAvailableTrue()).thenReturn(List.of());
        when(playerRepository.findByAuctionPlayerTrue()).thenReturn(List.of());

        assertThatThrownBy(() -> draftLotteryService.runLottery())
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("There are no players available for the lottery");

        verify(gameService).validatePhase(GamePhase.DRAFT_LOTTERY);
    }

    @Test
    @DisplayName("4- avanca para campeonato quando todos os times estao completos")
    void runLottery_avancaParaCampeonatoQuandoTodosTimesEstaoCompletos() {
        President president = president(1L, "Eduardo", List.of(
                player(1L, "Goleiro", Position.GOALKEEPER, false, false),
                player(2L, "Zagueiro", Position.DEFENDER, false, false),
                player(3L, "Meia", Position.MIDFIELDER, false, false),
                player(4L, "Atacante", Position.FORWARD, false, false),
                player(5L, "Craque", Position.FORWARD, false, false)
        ));

        when(presidentRepository.findAll()).thenReturn(List.of(president));

        String result = draftLotteryService.runLottery();

        verify(gameService).validatePhase(GamePhase.DRAFT_LOTTERY);
        verify(gameService).advanceToChampionship();

        assertThat(result).contains("All squads were already complete. The championship has started.");
        assertThat(result).contains("The championship has started");
    }

    @Test
    @DisplayName("5- retorna jogadores disponiveis para sorteio ordenados")
    void getAvailableForLottery_retornaJogadoresDisponiveisOrdenados() {
        Player forward = player(1L, "Atacante", Position.FORWARD, false, true);
        Player goalkeeper = player(2L, "Goleiro", Position.GOALKEEPER, false, true);
        Player midfielder = player(3L, "Meia", Position.MIDFIELDER, true, true);
        Player unavailableAuctionPlayer = player(4L, "Indisponivel", Position.DEFENDER, true, false);

        when(playerRepository.findByAuctionPlayerFalseAndAvailableTrue())
                .thenReturn(List.of(forward, goalkeeper));
        when(playerRepository.findByAuctionPlayerTrue())
                .thenReturn(List.of(midfielder, unavailableAuctionPlayer));

        List<Response.Player> result = draftLotteryService.getAvailableForLottery();

        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(Response.Player::getName)
                .containsExactly("Goleiro", "Meia", "Atacante");

        assertThat(result)
                .extracting(Response.Player::getPosition)
                .containsExactly(Position.GOALKEEPER, Position.MIDFIELDER, Position.FORWARD);
    }

    private President president(Long id, String name, List<Player> team) {
        return President.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .team(team)
                .build();
    }

    private Player player(Long id, String name, Position position, boolean auctionPlayer, boolean available) {
        return Player.builder()
                .id(id)
                .name(name)
                .position(position)
                .value(10.0)
                .auctionPlayer(auctionPlayer)
                .available(available)
                .build();
    }
}