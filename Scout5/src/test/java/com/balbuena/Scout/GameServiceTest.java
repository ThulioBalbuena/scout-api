package com.balbuena.Scout;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.GameState;
import com.balbuena.Scout.repository.AuctionBidRepository;
import com.balbuena.Scout.repository.GameStateRepository;
import com.balbuena.Scout.repository.MatchRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.service.GameService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameService")
class GameServiceTest {

    @Mock
    private GameStateRepository gameStateRepository;

    @Mock
    private AuctionBidRepository auctionBidRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PresidentRepository presidentRepository;

    @InjectMocks
    private GameService gameService;

    private GameState makeState(GamePhase phase) {
        GameState s = new GameState();
        s.setId(1L);
        s.setPhase(phase);
        s.setCurrentRound(0);
        s.setCurrentAuctionPlayerIndex(0);
        s.setTotalRounds(6);
        return s;
    }

    // ---------------
    // FLUXO NORMAL

    @Nested
    @DisplayName("Fluxo Normal")
    class FluxoNormal {

        @Test
        @DisplayName("01 - getGameState retorna estado existente")
        void getGameState() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.REGISTRATION))); //faz a busca pelo estado  

            GameState result = gameService.getGameState(); //  armazena o resultado da busca

            assertThat(result.getPhase()).isEqualTo(GamePhase.REGISTRATION); //verifica se o estado é o esperado
        }

        @Test
        @DisplayName("02 - advanceToAuctionPhase transiciona de REGISTRATION para DRAFT_AUCTION")
        void advanceToAuction() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.REGISTRATION))); //faz a busca pelo estado  
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0)); //salva o estado (IA ajudou aqui,sem isso pode retornar null e quebrar o teste)
            when(presidentRepository.count()).thenReturn(10L);

            Response.GameState result = gameService.advanceToAuctionPhase(); //armazena o resultado da busca

            assertThat(result.getPhase()).isEqualTo(GamePhase.DRAFT_AUCTION); //verifica se o estado é o esperado
        }

        @Test
        @DisplayName("03 - advanceAuctionToNextPlayer no índice 4 vai para DRAFT_LOTTERY")
        void advanceAuction() {
            GameState state = makeState(GamePhase.DRAFT_AUCTION); //cria o estado
            state.setCurrentAuctionPlayerIndex(4); // define o índice do jogador atual
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(state)); //faz a busca pelo estado  
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0)); //salva o estado (IA ajudou aqui,sem isso pode retornar null e quebrar o teste)

            Response.GameState result = gameService.advanceAuctionToNextPlayer(); //armazena o resultado da busca

            assertThat(result.getPhase()).isEqualTo(GamePhase.DRAFT_LOTTERY); //verifica se o estado é o esperado
        }

        @Test
        @DisplayName("04 - advanceToChampionship transiciona de DRAFT_LOTTERY para CHAMPIONSHIP")
        void advanceToChampionship() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.DRAFT_LOTTERY))); //faz a busca pelo estado  
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0)); //salva o estado (IA ajudou aqui,sem isso pode retornar null e quebrar o teste)

            Response.GameState result = gameService.advanceToChampionship(); //armazena o resultado da busca

            assertThat(result.getPhase()).isEqualTo(GamePhase.CHAMPIONSHIP);
        }

        @Test
        @DisplayName("05 - advanceAuctionToNextPlayer incrementa índice quando ainda não é o último jogador")
        void advanceAuction_incrementaIndice() {
            GameState state = makeState(GamePhase.DRAFT_AUCTION);
            state.setCurrentAuctionPlayerIndex(2);

            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(state));
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Response.GameState result = gameService.advanceAuctionToNextPlayer();

            assertThat(result.getPhase()).isEqualTo(GamePhase.DRAFT_AUCTION);
            assertThat(result.getCurrentAuctionPlayerIndex()).isEqualTo(3);
        }

        @Test
        @DisplayName("06 - openTransferWindow transiciona de CHAMPIONSHIP para TRANSFER_WINDOW após a rodada 3")
        void openTransferWindow() {
            GameState state = makeState(GamePhase.CHAMPIONSHIP);
            state.setCurrentRound(3);

            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(state));
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Response.GameState result = gameService.openTransferWindow();

            assertThat(result.getPhase()).isEqualTo(GamePhase.TRANSFER_WINDOW);
        }

	@Test
        @DisplayName("07 - closeTransferWindow transiciona de TRANSFER_WINDOW para CHAMPIONSHIP")
        void closeTransferWindow() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.TRANSFER_WINDOW)));
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
 
            Response.GameState result = gameService.closeTransferWindow();
 
            assertThat(result.getPhase()).isEqualTo(GamePhase.CHAMPIONSHIP);
        }
 
        @Test
        @DisplayName("08 - finishChampionship transiciona de CHAMPIONSHIP para FINISHED")
        void finishChampionship_deChampionship() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.CHAMPIONSHIP)));
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
 
            Response.GameState result = gameService.finishChampionship();
 
            assertThat(result.getPhase()).isEqualTo(GamePhase.FINISHED);
        }
 
        @Test
        @DisplayName("09 - finishChampionship transiciona de TRANSFER_WINDOW para FINISHED")
        void finishChampionship_deTransferWindow() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.TRANSFER_WINDOW)));
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
 
            Response.GameState result = gameService.finishChampionship();
 
            assertThat(result.getPhase()).isEqualTo(GamePhase.FINISHED);
        }
 
        @Test
        @DisplayName("10 - openTransferWindow abre com rodada igual a 3")
        void openTransferWindow_rodadaValida() {
            GameState state = makeState(GamePhase.CHAMPIONSHIP);
            state.setCurrentRound(3); 
 
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(state));
            when(gameStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
 
            Response.GameState result = gameService.openTransferWindow();
 
            assertThat(result.getPhase()).isEqualTo(GamePhase.TRANSFER_WINDOW);
        }
    }
 
    // --------------------------------------
    // FLUXO DE EXTENSÃO

    @Nested
    @DisplayName("Fluxo de Extensão")
    class FluxoExtensao {

        @Test
        @DisplayName("11 - getGameState lança exceção quando estado não existe")
        void getGameState() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.empty()); //faz a busca pelo estado  

            assertThatThrownBy(() -> gameService.getGameState()) //verifica se o estado é o esperado
                .isInstanceOf(ScoutException.class); //verifica se a exceção é a esperada
        }

        @Test
        @DisplayName("12 - advanceToAuctionPhase fora de REGISTRATION lança exceção")
        void advanceToAuction_faseErrada_lancaExcecao() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.DRAFT_AUCTION))); //faz a busca pelo estado  

            assertThatThrownBy(() -> gameService.advanceToAuctionPhase())   
                .isInstanceOf(ScoutException.class); //verifica se a exceção é a esperada
        }

        @Test
        @DisplayName("13 - advanceAuctionToNextPlayer fora do leilão lança exceção")
        void advanceAuction_faseErrada_lancaExcecao() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.CHAMPIONSHIP))); //faz a busca pelo estado  

            assertThatThrownBy(() -> gameService.advanceAuctionToNextPlayer()) //verifica se o estado é o esperado
                .isInstanceOf(ScoutException.class); //verifica se a exceção é a esperada
        }

        @Test
        @DisplayName("14 - advanceToChampionship fora de DRAFT_LOTTERY lança exceção")
        void advanceToChampionship_faseErrada_lancaExcecao() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.REGISTRATION)));//faz a busca pelo estado  

            assertThatThrownBy(() -> gameService.advanceToChampionship()) //verifica se o estado é o esperado
                .isInstanceOf(ScoutException.class); //verifica se a exceção é a esperada
        }

        @Test
        @DisplayName("15 - openTransferWindow fora de CHAMPIONSHIP lança exceção")
        void openTransferWindow_faseErrada_lancaExcecao() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.of(makeState(GamePhase.REGISTRATION))); //faz a busca pelo estado  
 
            assertThatThrownBy(() -> gameService.openTransferWindow()) //verifica se o estado é o esperado
                .isInstanceOf(ScoutException.class); //verifica se a exceção é a esperada
        }

        @Test
        @DisplayName("16 - openTransferWindow quando estado não existe lança exceção")
        void openTransferWindow_estadoNaoExiste_lancaExcecao() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameService.openTransferWindow())
                    .isInstanceOf(ScoutException.class);
        }

        @Test
        @DisplayName("17 - advanceAuctionToNextPlayer quando estado não existe lança exceção")
        void advanceAuction_estadoNaoExiste_lancaExcecao() {
            when(gameStateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameService.advanceAuctionToNextPlayer())
                    .isInstanceOf(ScoutException.class);
        }
        @Test
        @DisplayName("18 - closeTransferWindow fora de TRANSFER_WINDOW lança exceção")
        void closeTransferWindow_faseErrada_lancaExcecao() {
            when(gameStateRepository.findById(1L))
                    .thenReturn(Optional.of(makeState(GamePhase.CHAMPIONSHIP)));

            assertThatThrownBy(() -> gameService.closeTransferWindow())
                    .isInstanceOf(ScoutException.class);
        }
        @Test
        @DisplayName("19 - finishChampionship fora de CHAMPIONSHIP ou TRANSFER_WINDOW lança exceção")
        void finishChampionship_faseErrada_lancaExcecao() {
            when(gameStateRepository.findById(1L))
                    .thenReturn(Optional.of(makeState(GamePhase.REGISTRATION)));

            assertThatThrownBy(() -> gameService.finishChampionship())
                    .isInstanceOf(ScoutException.class);
        }

        @Test
        @DisplayName("20 - advanceToAuctionPhase exige ao menos 10 presidentes")
        void advanceToAuction_poucosPresidentes_lancaExcecao() {
            when(gameStateRepository.findById(1L))
                    .thenReturn(Optional.of(makeState(GamePhase.REGISTRATION)));
            when(presidentRepository.count()).thenReturn(9L);

            assertThatThrownBy(() -> gameService.advanceToAuctionPhase())
                    .isInstanceOf(ScoutException.class)
                    .hasMessageContaining("At least 10 presidents");
        }

        @Test
        @DisplayName("21 - finishChampionship exige todas as partidas finalizadas")
        void finishChampionship_partidasPendentes_lancaExcecao() {
            when(gameStateRepository.findById(1L))
                    .thenReturn(Optional.of(makeState(GamePhase.CHAMPIONSHIP)));
            when(matchRepository.countByPlayedFalse()).thenReturn(1L);

            assertThatThrownBy(() -> gameService.finishChampionship())
                    .isInstanceOf(ScoutException.class)
                    .hasMessageContaining("All championship matches");
        }
    }
}
