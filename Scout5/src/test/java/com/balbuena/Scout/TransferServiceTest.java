package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.*;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService")
class TransferServiceTest {

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameService gameService;

    @Mock
    private PresidentService presidentService;

    @InjectMocks
    private TransferService transferService;

    private Player makePlayer(Long id, Position position, double value, boolean available) {
        Player p = new Player();
        p.setId(id);
        p.setName("Jogador " + id);
        p.setPosition(position);
        p.setValue(value);
        p.setAvailable(available);
        return p;
    }

    private President makePresident(Long id, double budget, boolean transferUsed, List<Player> team) {
        President p = new President();
        p.setId(id);
        p.setName("Presidente " + id);
        p.setBudget(budget);
        p.setUsedBudget(0.0);
        p.setTransferUsed(transferUsed);
        p.setTeam(new ArrayList<>(team));
        return p;
    }

    // ---------------
    // FLUXO NORMAL

    @Nested
    @DisplayName("Fluxo Normal")
    class FluxoNormal {

        @Test
        @DisplayName("01 - swapWithAvailablePlayer realiza transferência válida")
        void swapWithAvailablePlayer_transferValida() {
            Player playerOut = makePlayer(1L, Position.MIDFIELDER, 100.0, false); // jogador que sai do time
            Player playerIn  = makePlayer(2L, Position.MIDFIELDER,  80.0, true);  // jogador disponível no mercado
            President president = makePresident(1L, 500.0, false, List.of(playerOut)); // presidente com saldo e sem transferência usada

            Request.Transfer request = new Request.Transfer();
            request.setPresidentId(1L);
            request.setPlayerOutId(1L);
            request.setPlayerInId(2L);

            when(presidentService.getPresident(1L)).thenReturn(president);
            when(playerRepository.findById(2L)).thenReturn(Optional.of(playerIn));
            when(playerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(presidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(presidentService.toResponse(any())).thenReturn(Response.President.builder().build());

            Response.President result = transferService.swapWithAvailablePlayer(request);

            assertThat(result).isNotNull();
            assertThat(president.isTransferUsed()).isTrue(); // verifica que a transferência foi marcada como usada
        }
    }

    // ---------------
    // FLUXO DE EXTENSÃO

    @Nested
    @DisplayName("Fluxo de Extensão")
    class FluxoExtensao {

        @Test
        @DisplayName("02 - swapWithAvailablePlayer lança exceção para jogador inexistente")
        void swapWithAvailablePlayer_jogadorInexistente() {
            Player playerOut = makePlayer(1L, Position.MIDFIELDER, 100.0, false);
            President president = makePresident(1L, 500.0, false, List.of(playerOut));

            Request.Transfer request = new Request.Transfer();
            request.setPresidentId(1L);
            request.setPlayerOutId(1L);
            request.setPlayerInId(99L); // ID que não existe no repositório

            when(presidentService.getPresident(1L)).thenReturn(president);
            when(playerRepository.findById(99L)).thenReturn(Optional.empty()); // repositório não encontra o jogador

            assertThatThrownBy(() -> transferService.swapWithAvailablePlayer(request))
                    .isInstanceOf(ScoutException.class);
        }

        @Test
        @DisplayName("03 - swapWithAvailablePlayer lança exceção para presidente inexistente")
        void swapWithAvailablePlayer_presidenteInexistente() {
            Request.Transfer request = new Request.Transfer();
            request.setPresidentId(99L); // ID que não existe
            request.setPlayerOutId(1L);
            request.setPlayerInId(2L);

            when(presidentService.getPresident(99L))
                    .thenThrow(new ScoutException("Presidente nao encontrado: ID 99")); // simula presidente não encontrado

            assertThatThrownBy(() -> transferService.swapWithAvailablePlayer(request))
                    .isInstanceOf(ScoutException.class);
        }

        @Test
        @DisplayName("04 - swapWithAvailablePlayer lança exceção quando transferência já foi usada")
        void swapWithAvailablePlayer_transferenciaJaUsada() {
            Player playerOut = makePlayer(1L, Position.MIDFIELDER, 100.0, false);
            President president = makePresident(1L, 500.0, true, List.of(playerOut)); // transferUsed = true

            Request.Transfer request = new Request.Transfer();
            request.setPresidentId(1L);
            request.setPlayerOutId(1L);
            request.setPlayerInId(2L);

            when(presidentService.getPresident(1L)).thenReturn(president);

            assertThatThrownBy(() -> transferService.swapWithAvailablePlayer(request))
                    .isInstanceOf(ScoutException.class);
        }
    }
}
