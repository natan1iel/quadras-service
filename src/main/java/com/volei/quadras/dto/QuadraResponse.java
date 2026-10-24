package com.volei.quadras.dto;

import com.volei.quadras.model.Quadra;

import java.util.Comparator;
import java.util.List;

public record QuadraResponse(Long id, String codigo, String tipo, Integer intervaloMinutos, Boolean ativa,
        List<HorarioResponse> horarios) {
    public static QuadraResponse de(Quadra quadra) {
        List<HorarioResponse> grade = quadra.getHorarios().stream()
                .sorted(Comparator.comparing(h -> h.getDiaSemana().getValue()))
                .map(HorarioResponse::de)
                .toList();

        return new QuadraResponse(quadra.getId(), quadra.getCodigo(), quadra.getTipo().name(),
                quadra.getIntervaloMinutos(), quadra.getAtiva(), grade);
    }
}
