package com.balbuena.Scout;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.President;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.service.GameService;
import com.balbuena.Scout.service.PresidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PresidentService")
class PresidentServiceTest {

    @Mock
    private PresidentRepository presidentRepository;

    @Mock
    private GameService gameService;

    @InjectMocks
    private PresidentService presidentService;

    @Test
    @DisplayName("1- cadastra presidente valido")
    void create_cadastraPresidenteValido() {
        Request.PresidentCreate request = new Request.PresidentCreate(
                "Eduardo",
                "eduardo@test.com"
        );

        President savedPresident = President.builder()
                .id(1L)
                .name("Eduardo")
                .email("eduardo@test.com")
                .build();

        when(presidentRepository.existsByEmail("eduardo@test.com")).thenReturn(false);
        when(presidentRepository.existsByName("Eduardo")).thenReturn(false);
        when(presidentRepository.save(any(President.class))).thenReturn(savedPresident);

        Response.President response = presidentService.create(request);

        verify(gameService).validatePhase(GamePhase.REGISTRATION);

        ArgumentCaptor<President> presidentCaptor = ArgumentCaptor.forClass(President.class);
        verify(presidentRepository).save(presidentCaptor.capture());

        assertThat(presidentCaptor.getValue().getName()).isEqualTo("Eduardo");
        assertThat(presidentCaptor.getValue().getEmail()).isEqualTo("eduardo@test.com");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Eduardo");
        assertThat(response.getEmail()).isEqualTo("eduardo@test.com");
        assertThat(response.getBudget()).isEqualTo(100.0);
        assertThat(response.getUsedBudget()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("2- recusa email duplicado")
    void create_recusaEmailDuplicado() {
        Request.PresidentCreate request = new Request.PresidentCreate(
                "Eduardo",
                "eduardo@test.com"
        );

        when(presidentRepository.existsByEmail("eduardo@test.com")).thenReturn(true);

        assertThatThrownBy(() -> presidentService.create(request))
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("Email ja cadastrado");

        verify(gameService).validatePhase(GamePhase.REGISTRATION);
        verify(presidentRepository, never()).existsByName("Eduardo");
        verify(presidentRepository, never()).save(any(President.class));
    }

    @Test
    @DisplayName("3- recusa nome duplicado")
    void create_recusaNomeDuplicado() {
        Request.PresidentCreate request = new Request.PresidentCreate(
                "Eduardo",
                "eduardo@test.com"
        );

        when(presidentRepository.existsByEmail("eduardo@test.com")).thenReturn(false);
        when(presidentRepository.existsByName("Eduardo")).thenReturn(true);

        assertThatThrownBy(() -> presidentService.create(request))
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("Nome ja cadastrado");

        verify(gameService).validatePhase(GamePhase.REGISTRATION);
        verify(presidentRepository, never()).save(any(President.class));
    }

    @Test
    @DisplayName("4- retorna presidente existente por id")
    void findById_retornaPresidenteExistente() {
        President president = President.builder()
                .id(1L)
                .name("Eduardo")
                .email("eduardo@test.com")
                .build();

        when(presidentRepository.findById(1L)).thenReturn(Optional.of(president));

        Response.President response = presidentService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Eduardo");
        assertThat(response.getEmail()).isEqualTo("eduardo@test.com");
        assertThat(response.getBudget()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("5- lanca excecao quando presidente nao existe")
    void findById_lancaExcecaoQuandoNaoExiste() {
        when(presidentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> presidentService.findById(99L))
                .isInstanceOf(ScoutException.class)
                .hasMessageContaining("President nao encontrado: ID 99");
    }
}