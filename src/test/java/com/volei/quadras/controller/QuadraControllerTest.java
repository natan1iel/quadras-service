package com.volei.quadras.controller;

import com.volei.quadras.dto.HorarioRequest;
import com.volei.quadras.dto.QuadraRequest;
import com.volei.quadras.exception.RegraDeNegocioException;
import com.volei.quadras.service.QuadraService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuadraControllerTest {
    @Mock
    private QuadraService quadraService;

    @InjectMocks
    private QuadraController quadraController;

    @Test
    @DisplayName("USUARIO não pode cadastrar quadra")
    void usuarioNaoCadastra() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraController.cadastrar("USUARIO", requisicao()));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatus());
        verify(quadraService, never()).cadastrar(any(QuadraRequest.class));
    }

    @Test
    @DisplayName("requisição sem o header de papel também é barrada")
    void semHeaderNaoCadastra() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraController.cadastrar(null, requisicao()));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatus());
        verify(quadraService, never()).cadastrar(any(QuadraRequest.class));
    }

    @Test
    @DisplayName("USUARIO não pode atualizar nem inativar quadra")
    void usuarioNaoAlteraNemInativa() {
        assertThrows(RegraDeNegocioException.class, () -> quadraController.atualizar("USUARIO", 1L, requisicao()));
        assertThrows(RegraDeNegocioException.class, () -> quadraController.inativar("USUARIO", 1L));

        verify(quadraService, never()).atualizar(any(), any());
        verify(quadraService, never()).inativar(any());
    }

    @Test
    @DisplayName("ADMIN passa e a chamada chega no service")
    void adminCadastra() {
        QuadraRequest requisicao = requisicao();

        quadraController.cadastrar("ADMIN", requisicao);

        verify(quadraService).cadastrar(requisicao);
    }

    private QuadraRequest requisicao() {
        return new QuadraRequest("Areia B", "AREIA", 15,
                List.of(new HorarioRequest("MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0))));
    }
}
