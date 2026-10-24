package com.volei.quadras.service;

import com.volei.quadras.dto.DisponibilidadeDTO;
import com.volei.quadras.dto.HorarioRequest;
import com.volei.quadras.dto.QuadraRequest;
import com.volei.quadras.dto.QuadraResponse;
import com.volei.quadras.exception.RegraDeNegocioException;
import com.volei.quadras.model.HorarioFuncionamento;
import com.volei.quadras.model.Quadra;
import com.volei.quadras.model.TipoQuadra;
import com.volei.quadras.repository.QuadraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuadraServiceTest {
    @Mock
    private QuadraRepository quadraRepository;

    @InjectMocks
    private QuadraService quadraService;

    @Test
    @DisplayName("cadastro válido devolve a quadra com a grade montada")
    void cadastroValido() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(quadraRepository.save(any(Quadra.class))).thenAnswer(invocacao -> {
            Quadra salva = invocacao.getArgument(0);
            salva.setId(1L);
            return salva;
        });

        QuadraResponse resposta = quadraService.cadastrar(requisicaoValida());

        assertEquals(1L, resposta.id());
        assertEquals("Areia B", resposta.codigo());
        assertEquals("AREIA", resposta.tipo());
        assertTrue(resposta.ativa());
        assertEquals(2, resposta.horarios().size());
    }

    @Test
    @DisplayName("código duplicado é rejeitado mesmo com caixa diferente")
    void codigoDuplicado() {
        Quadra existente = new Quadra("areia b", TipoQuadra.AREIA, 15);
        existente.setId(7L);
        when(quadraRepository.findByCodigoIgnoreCase("Areia B")).thenReturn(Optional.of(existente));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraService.cadastrar(requisicaoValida()));

        assertTrue(erro.getMessage().contains("Já existe"));

        verify(quadraRepository, never()).save(any(Quadra.class));
    }

    @Test
    @DisplayName("intervalo negativo é rejeitado")
    void intervaloNegativo() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        QuadraRequest requisicao = new QuadraRequest("Areia B", "AREIA", -1, gradeValida());

        assertThrows(RegraDeNegocioException.class, () -> quadraService.cadastrar(requisicao));
    }

    @Test
    @DisplayName("tipo fora do enum é rejeitado")
    void tipoInvalido() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        QuadraRequest requisicao = new QuadraRequest("Areia B", "GRAMA", 15, gradeValida());

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraService.cadastrar(requisicao));

        assertTrue(erro.getMessage().contains("Tipo inválido"));
    }

    @Test
    @DisplayName("grade vazia é rejeitada")
    void gradeVazia() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        QuadraRequest requisicao = new QuadraRequest("Areia B", "AREIA", 15, List.of());

        assertThrows(RegraDeNegocioException.class, () -> quadraService.cadastrar(requisicao));
    }

    @Test
    @DisplayName("abertura igual ao fechamento é rejeitada")
    void aberturaIgualAoFechamento() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        List<HorarioRequest> grade = List.of(new HorarioRequest("MONDAY", LocalTime.of(8, 0), LocalTime.of(8, 0)));
        QuadraRequest requisicao = new QuadraRequest("Areia B", "AREIA", 15, grade);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraService.cadastrar(requisicao));

        assertTrue(erro.getMessage().contains("anterior ao fechamento"));
    }

    @Test
    @DisplayName("abertura depois do fechamento é rejeitada")
    void aberturaDepoisDoFechamento() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        List<HorarioRequest> grade = List.of(new HorarioRequest("MONDAY", LocalTime.of(20, 0), LocalTime.of(8, 0)));
        QuadraRequest requisicao = new QuadraRequest("Areia B", "AREIA", 15, grade);

        assertThrows(RegraDeNegocioException.class, () -> quadraService.cadastrar(requisicao));
    }

    @Test
    @DisplayName("dia da semana repetido na grade é rejeitado")
    void diaRepetidoNaGrade() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        List<HorarioRequest> grade = List.of(new HorarioRequest("MONDAY", LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new HorarioRequest("MONDAY", LocalTime.of(14, 0), LocalTime.of(18, 0)));
        QuadraRequest requisicao = new QuadraRequest("Areia B", "AREIA", 15, grade);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraService.cadastrar(requisicao));

        assertTrue(erro.getMessage().contains("mais de uma vez"));
    }

    @Test
    @DisplayName("dia da semana inexistente é rejeitado")
    void diaInvalido() {
        when(quadraRepository.findByCodigoIgnoreCase(anyString())).thenReturn(Optional.empty());

        List<HorarioRequest> grade = List.of(new HorarioRequest("SEGUNDA", LocalTime.of(8, 0), LocalTime.of(12, 0)));
        QuadraRequest requisicao = new QuadraRequest("Areia B", "AREIA", 15, grade);

        assertThrows(RegraDeNegocioException.class, () -> quadraService.cadastrar(requisicao));
    }

    @Test
    @DisplayName("código em branco é rejeitado")
    void codigoEmBranco() {
        QuadraRequest requisicao = new QuadraRequest("   ", "AREIA", 15, gradeValida());

        assertThrows(RegraDeNegocioException.class, () -> quadraService.cadastrar(requisicao));
    }

    @Test
    @DisplayName("disponibilidade num dia SEM grade devolve aberta=false")
    void disponibilidadeEmDiaFechado() {
        Quadra quadra = quadraPersistida();
        when(quadraRepository.findById(1L)).thenReturn(Optional.of(quadra));

        DisponibilidadeDTO resposta =
                quadraService.consultarDisponibilidade(1L, LocalDate.of(2026, 9, 27));

        assertFalse(resposta.aberta());
        assertNull(resposta.abertura());
        assertNull(resposta.fechamento());

        assertEquals("Areia B", resposta.codigo());
        assertEquals(15, resposta.intervaloMinutos());
    }

    @Test
    @DisplayName("disponibilidade num dia COM grade devolve a janela daquele dia")
    void disponibilidadeEmDiaAberto() {
        Quadra quadra = quadraPersistida();
        when(quadraRepository.findById(1L)).thenReturn(Optional.of(quadra));

        DisponibilidadeDTO resposta =
                quadraService.consultarDisponibilidade(1L, LocalDate.of(2026, 9, 21));

        assertTrue(resposta.aberta());
        assertEquals(LocalTime.of(8, 0), resposta.abertura());
        assertEquals(LocalTime.of(18, 0), resposta.fechamento());
    }

    @Test
    @DisplayName("cada dia da grade devolve a SUA janela, não a do primeiro dia")
    void disponibilidadeRespeitaODiaConsultado() {
        Quadra quadra = quadraPersistida();
        when(quadraRepository.findById(1L)).thenReturn(Optional.of(quadra));

        DisponibilidadeDTO sabado =
                quadraService.consultarDisponibilidade(1L, LocalDate.of(2026, 9, 26));

        assertTrue(sabado.aberta());
        assertEquals(LocalTime.of(9, 0), sabado.abertura());
        assertEquals(LocalTime.of(14, 0), sabado.fechamento());
    }

    @Test
    @DisplayName("disponibilidade de quadra inexistente devolve 404")
    void disponibilidadeDeQuadraInexistente() {
        when(quadraRepository.findById(99L)).thenReturn(Optional.empty());

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> quadraService.consultarDisponibilidade(99L, LocalDate.of(2026, 9, 21)));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatus());
        assertEquals("QUADRA_NAO_ENCONTRADA", erro.getCodigo());
    }

    @Test
    @DisplayName("listar devolve apenas as quadras ativas")
    void listarAtivas() {
        when(quadraRepository.findByAtivaTrue()).thenReturn(List.of(quadraPersistida()));

        List<QuadraResponse> lista = quadraService.listarAtivas();

        assertEquals(1, lista.size());
        assertEquals("Areia B", lista.get(0).codigo());
    }

    @Test
    @DisplayName("buscar por id inexistente devolve 404")
    void buscarInexistente() {
        when(quadraRepository.findById(99L)).thenReturn(Optional.empty());

        RegraDeNegocioException erro =
                assertThrows(RegraDeNegocioException.class, () -> quadraService.buscarPorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatus());
    }

    @Test
    @DisplayName("inativar não apaga: apenas marca ativa=false")
    void inativar() {
        Quadra quadra = quadraPersistida();
        when(quadraRepository.findById(1L)).thenReturn(Optional.of(quadra));

        quadraService.inativar(1L);

        assertFalse(quadra.getAtiva());
        verify(quadraRepository).save(quadra);
        verify(quadraRepository, never()).delete(any(Quadra.class));
    }

    @Test
    @DisplayName("atualizar aceita a quadra manter o próprio código")
    void atualizarMantendoOProprioCodigo() {
        Quadra quadra = quadraPersistida();
        when(quadraRepository.findById(1L)).thenReturn(Optional.of(quadra));

        when(quadraRepository.findByCodigoIgnoreCase("Areia B")).thenReturn(Optional.of(quadra));
        when(quadraRepository.save(any(Quadra.class))).thenAnswer(i -> i.getArgument(0));

        QuadraRequest requisicao = new QuadraRequest("Areia B", "QUADRA", 30, gradeValida());
        QuadraResponse resposta = quadraService.atualizar(1L, requisicao);

        assertEquals("QUADRA", resposta.tipo());
        assertEquals(30, resposta.intervaloMinutos());
    }

    @Test
    @DisplayName("atualizar recusa um código que já é de outra quadra")
    void atualizarComCodigoDeOutraQuadra() {
        Quadra quadra = quadraPersistida();
        Quadra outra = new Quadra("Areia B", TipoQuadra.AREIA, 15);
        outra.setId(2L);

        when(quadraRepository.findById(1L)).thenReturn(Optional.of(quadra));
        when(quadraRepository.findByCodigoIgnoreCase("Areia B")).thenReturn(Optional.of(outra));

        assertThrows(RegraDeNegocioException.class, () -> quadraService.atualizar(1L, requisicaoValida()));
    }

    private QuadraRequest requisicaoValida() {
        return new QuadraRequest("Areia B", "AREIA", 15, gradeValida());
    }

    private List<HorarioRequest> gradeValida() {
        return List.of(new HorarioRequest("MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new HorarioRequest("SATURDAY", LocalTime.of(9, 0), LocalTime.of(14, 0)));
    }

    private Quadra quadraPersistida() {
        Quadra quadra = new Quadra("Areia B", TipoQuadra.AREIA, 15);
        quadra.setId(1L);
        quadra.adicionarHorario(new HorarioFuncionamento(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)));
        quadra.adicionarHorario(new HorarioFuncionamento(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(14, 0)));
        return quadra;
    }
}
