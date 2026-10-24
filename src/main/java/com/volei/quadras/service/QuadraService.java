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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class QuadraService {
    private final QuadraRepository quadraRepository;

    public QuadraService(QuadraRepository quadraRepository) {
        this.quadraRepository = quadraRepository;
    }

    @Transactional
    public QuadraResponse cadastrar(QuadraRequest requisicao) {
        validar(requisicao, null);

        Quadra quadra = new Quadra(requisicao.codigo().trim(), converterTipo(requisicao.tipo()),
                requisicao.intervaloMinutos());
        quadra.substituirHorarios(converterGrade(requisicao.horarios()));

        return QuadraResponse.de(quadraRepository.save(quadra));
    }

    @Transactional(readOnly = true)
    public List<QuadraResponse> listarAtivas() {
        return quadraRepository.findByAtivaTrue().stream()
                .map(QuadraResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuadraResponse buscarPorId(Long id) {
        return QuadraResponse.de(buscarOuFalhar(id));
    }

    @Transactional
    public QuadraResponse atualizar(Long id, QuadraRequest requisicao) {
        Quadra quadra = buscarOuFalhar(id);

        validar(requisicao, id);

        quadra.setCodigo(requisicao.codigo().trim());
        quadra.setTipo(converterTipo(requisicao.tipo()));
        quadra.setIntervaloMinutos(requisicao.intervaloMinutos());
        quadra.substituirHorarios(converterGrade(requisicao.horarios()));

        return QuadraResponse.de(quadraRepository.save(quadra));
    }

    @Transactional
    public void inativar(Long id) {
        Quadra quadra = buscarOuFalhar(id);
        quadra.setAtiva(false);
        quadraRepository.save(quadra);
    }

    @Transactional(readOnly = true)
    public DisponibilidadeDTO consultarDisponibilidade(Long quadraId, LocalDate data) {
        if (data == null) {
            throw RegraDeNegocioException.invalida("O parâmetro data é obrigatório");
        }

        Quadra quadra = buscarOuFalhar(quadraId);
        DayOfWeek dia = data.getDayOfWeek();

        Optional<HorarioFuncionamento> horario = quadra.getHorarios().stream()
                .filter(h -> h.getDiaSemana() == dia)
                .findFirst();

        if (horario.isEmpty()) {
            return DisponibilidadeDTO.fechada(quadra);
        }

        return DisponibilidadeDTO.aberta(quadra, horario.get());
    }

    public Quadra buscarOuFalhar(Long id) {
        return quadraRepository.findById(id)
                .orElseThrow(() -> RegraDeNegocioException.quadraNaoEncontrada(id));
    }

    private void validar(QuadraRequest requisicao, Long idAtual) {
        if (requisicao.codigo() == null || requisicao.codigo().isBlank()) {
            throw RegraDeNegocioException.invalida("O código da quadra é obrigatório");
        }

        Optional<Quadra> mesmoCodigo = quadraRepository.findByCodigoIgnoreCase(requisicao.codigo().trim());
        if (mesmoCodigo.isPresent() && !mesmoCodigo.get().getId().equals(idAtual)) {
            throw RegraDeNegocioException.invalida("Já existe uma quadra com o código " + requisicao.codigo());
        }

        if (requisicao.intervaloMinutos() == null || requisicao.intervaloMinutos() < 0) {
            throw RegraDeNegocioException.invalida("O intervalo em minutos não pode ser negativo");
        }

        converterTipo(requisicao.tipo());

        if (requisicao.horarios() == null || requisicao.horarios().isEmpty()) {
            throw RegraDeNegocioException.invalida("A quadra precisa de ao menos um dia de funcionamento");
        }

        validarGrade(requisicao.horarios());
    }

    private void validarGrade(List<HorarioRequest> grade) {
        Set<DayOfWeek> diasJaInformados = new HashSet<>();

        for (HorarioRequest linha : grade) {
            DayOfWeek dia = converterDia(linha.diaSemana());

            if (linha.abertura() == null || linha.fechamento() == null) {
                throw RegraDeNegocioException.invalida("Abertura e fechamento são obrigatórios em " + dia);
            }

            if (!linha.abertura().isBefore(linha.fechamento())) {
                throw RegraDeNegocioException.invalida("Em " + dia + ", a abertura deve ser anterior ao fechamento");
            }

            if (!diasJaInformados.add(dia)) {
                throw RegraDeNegocioException.invalida("O dia " + dia + " aparece mais de uma vez na grade");
            }
        }
    }

    private List<HorarioFuncionamento> converterGrade(List<HorarioRequest> grade) {
        List<HorarioFuncionamento> horarios = new ArrayList<>();
        for (HorarioRequest linha : grade) {
            horarios.add(new HorarioFuncionamento(converterDia(linha.diaSemana()), linha.abertura(), linha.fechamento()));
        }
        return horarios;
    }

    private TipoQuadra converterTipo(String texto) {
        try {
            return TipoQuadra.valueOf(texto == null ? "" : texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw RegraDeNegocioException.invalida("Tipo inválido: " + texto + ". Use AREIA ou QUADRA");
        }
    }

    private DayOfWeek converterDia(String texto) {
        try {
            return DayOfWeek.valueOf(texto == null ? "" : texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw RegraDeNegocioException.invalida(
                    "Dia da semana inválido: " + texto + ". Use MONDAY, TUESDAY ... SUNDAY");
        }
    }
}
