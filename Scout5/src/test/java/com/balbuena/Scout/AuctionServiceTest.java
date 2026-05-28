package com.balbuena.Scout;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.AuctionBid;
import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.GameState;
import com.balbuena.Scout.model.Player;
import com.balbuena.Scout.model.Position;
import com.balbuena.Scout.model.President;
import com.balbuena.Scout.repository.AuctionBidRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.service.AuctionService;
import com.balbuena.Scout.service.GameService;
import com.balbuena.Scout.service.PresidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionService")
class AuctionServiceTest {

    @Mock
    private AuctionBidRepository bidRepository;

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameService gameService;

    @Mock
    private PresidentService presidentService;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    @DisplayName("1- retorna status do jogador atual com lances ordenados")
    void getCurrentAuctionStatus_retornaJogadorAtualELancesOrdenados() {
        Player player = player();
        President ana = president(1L, "Ana", 100.0);
        President bob = president(2L, "Bob", 100.0);
        mockCurrentAuctionPlayer(player);
        when(bidRepository.findByPlayerIdOrderByBidAmountDesc(player.getId()))
                .thenReturn(List.of(bid(player, bob, 45.0), bid(player, ana, 35.0)));

        Response.AuctionStatus status = auctionService.getCurrentAuctionStatus();

        verify(gameService).validatePhase(GamePhase.DRAFT_AUCTION);
        assertThat(status.getPlayerName()).isEqualTo("Hulk");
        assertThat(status.getCurrentHighestBid()).isEqualTo(45.0);
        assertThat(status.getCurrentLeader()).isEqualTo("Bob");
        assertThat(status.getBids())
                .extracting(Response.AuctionStatus.BidInfo::getPresidentName)
                .containsExactly("Bob", "Ana");
    }

    @Test
    @DisplayName("2- salva primeiro lance valido")
    void placeBid_salvaPrimeiroLanceValido() {
        Player player = player();
        President president = president(1L, "Ana", 100.0);
        mockCurrentAuctionPlayer(player);
        when(presidentService.getPresident(president.getId())).thenReturn(president);
        when(bidRepository.findTopByPlayerIdOrderByBidAmountDesc(player.getId())).thenReturn(Optional.empty());
        when(bidRepository.findByPlayerIdOrderByBidAmountDesc(player.getId()))
                .thenReturn(List.of(bid(player, president, 30.0)));

        Response.AuctionStatus status = auctionService.placeBid(player.getId(), new Request.PlaceBid(1L, 30.0));

        ArgumentCaptor<AuctionBid> bidCaptor = ArgumentCaptor.forClass(AuctionBid.class);
        verify(bidRepository).save(bidCaptor.capture());
        assertThat(bidCaptor.getValue().getPlayer()).isSameAs(player);
        assertThat(bidCaptor.getValue().getPresident()).isSameAs(president);
        assertThat(bidCaptor.getValue().getBidAmount()).isEqualTo(30.0);
        assertThat(status.getCurrentLeader()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("3- recusa primeiro lance abaixo do valor base")
    void placeBid_recusaPrimeiroLanceAbaixoDoValorBase() {
        Player player = player();
        President president = president(1L, "Ana", 100.0);
        mockCurrentAuctionPlayer(player);
        when(presidentService.getPresident(president.getId())).thenReturn(president);
        when(bidRepository.findTopByPlayerIdOrderByBidAmountDesc(player.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.placeBid(player.getId(), new Request.PlaceBid(1L, 29.0)))
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("Lance minimo");

        verify(bidRepository, never()).save(any());
    }

    @Test
    @DisplayName("4- finaliza leilao com vencedor")
    void finalizeCurrentAuction_comVencedor() {
        Player player = player();
        President president = president(1L, "Ana", 100.0);
        AuctionBid winner = bid(player, president, 45.0);
        mockCurrentAuctionPlayer(player);
        when(bidRepository.findTopByPlayerIdOrderByBidAmountDesc(player.getId())).thenReturn(Optional.of(winner));
        when(bidRepository.findByPlayerIdOrderByBidAmountDesc(player.getId())).thenReturn(List.of(winner));

        Response.AuctionStatus status = auctionService.finalizeCurrentAuction();

        assertThat(winner.isWinningBid()).isTrue();
        assertThat(president.getBudget()).isEqualTo(55.0);
        assertThat(president.getUsedBudget()).isEqualTo(45.0);
        assertThat(player.isAvailable()).isFalse();
        assertThat(player.getPresident()).isSameAs(president);
        verify(bidRepository).save(winner);
        verify(presidentRepository).save(president);
        verify(playerRepository).save(player);
        verify(gameService).advanceAuctionToNextPlayer();
        assertThat(status.getCurrentLeader()).isEqualTo("Ana");
    }

    private void mockCurrentAuctionPlayer(Player player) {
        when(gameService.getGameState()).thenReturn(GameState.builder()
                .id(1L)
                .phase(GamePhase.DRAFT_AUCTION)
                .currentAuctionPlayerIndex(0)
                .build());
        when(playerRepository.findByAuctionPlayerTrue()).thenReturn(List.of(player));
    }

    private Player player() {
        return Player.builder()
                .id(10L)
                .name("Hulk")
                .position(Position.FORWARD)
                .value(30.0)
                .auctionPlayer(true)
                .available(true)
                .build();
    }

    private President president(Long id, String name, Double budget) {
        return President.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .budget(budget)
                .usedBudget(0.0)
                .team(new ArrayList<>())
                .build();
    }

    private AuctionBid bid(Player player, President president, Double amount) {
        return AuctionBid.builder()
                .player(player)
                .president(president)
                .bidAmount(amount)
                .bidTime(LocalDateTime.of(2026, 5, 17, 20, 0))
                .build();
    }
}
