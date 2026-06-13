package com.balbuena.Scout;

import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.*;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.service.ChampionshipService;
import com.balbuena.Scout.service.GameService;
import com.balbuena.Scout.service.PresidentService;
import com.balbuena.Scout.repository.PlayerRepository;
import com.balbuena.Scout.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Com Mocks")
class Tarefa4MockTest {

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private GameService gameService;

    @InjectMocks
    private PresidentService presidentService;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private ChampionshipService championshipService;

    // ── Helper ────────────────────────────────────────────────────────────────
    private President makePresident(Long id, String name) {
        President p = new President();
        p.setId(id);
        p.setName(name);
        p.setEmail(name.toLowerCase() + "@test.com");
        p.setBudget(100.0);
        p.setUsedBudget(0.0);
        p.setTeam(new ArrayList<>());
        return p;
    }
    @Test
    @DisplayName("01 - findAll: mock do repositório retorna lista, serviço mapeia corretamente")
    void findAll_mockRepositorio_retornaListaMapeada() {
        // ARRANGE — mock simula banco retornando 2 presidents sem acesso real
        when(presidentRepository.findAll())
            .thenReturn(List.of(
                makePresident(1L, "Ana"),
                makePresident(2L, "Bob")
            ));

        List<Response.President> resultado = presidentService.findAll();

        verify(presidentRepository, times(1)).findAll(); 
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getName()).isEqualTo("Ana");
        assertThat(resultado.get(1).getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("02 - getPresident: mock retorna Optional vazio, serviço lança ScoutException")
    void getPresident_mockRetornaVazio_lancaExcecao() {
        // ARRANGE — mock simula ID inexistente no banco (Optional.empty)
        when(presidentRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> presidentService.getPresident(99L))
            .isInstanceOf(ScoutException.class)
            .hasMessageContaining("99");

        verify(presidentRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("03 - generateSchedule: mock indica tabela já existente, serviço lança exceção")
    void generateSchedule_mockTabelaExistente_lancaExcecao() {
        // ARRANGE — mock simula 6 partidas já salvas no banco
        doNothing().when(gameService).validatePhase(any(), any());
        when(matchRepository.count()).thenReturn(6L);

        // ACT & ASSERT — serviço deve recusar gerar tabela duplicada
        assertThatThrownBy(() -> championshipService.generateSchedule())
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("already been generated");

        verify(matchRepository, times(1)).count();
        verify(matchRepository, never()).saveAll(any()); // saveAll nunca deve ser chamado
    }

    @Test
    @DisplayName("04 - generateSchedule: mock com 1 presidente, serviço lança exceção de mínimo")
    void generateSchedule_mockUmPresidente_lancaExcecao() {
        // ARRANGE — mock: banco vazio de partidas mas só 1 presidente cadastrado
        doNothing().when(gameService).validatePhase(any(), any());
        when(matchRepository.count()).thenReturn(0L);
        when(presidentRepository.findAll())
                .thenReturn(List.of(makePresident(1L, "Sozinho")));

        // ACT & ASSERT — serviço deve exigir mínimo 2 presidentes
        assertThatThrownBy(() -> championshipService.generateSchedule())
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("2 presidents");

        verify(matchRepository, never()).saveAll(any()); // nunca salva se validação falhou
    }
}
