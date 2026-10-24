package com.volei.quadras.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.volei.quadras.model.HorarioFuncionamento;
import com.volei.quadras.model.Quadra;

import java.time.LocalTime;

public record DisponibilidadeDTO(Long id, String codigo, String tipo, Boolean ativa, Integer intervaloMinutos, Boolean aberta, @JsonFormat(pattern = "HH:mm") LocalTime abertura, @JsonFormat(pattern = "HH:mm") LocalTime fechamento) {
    public static DisponibilidadeDTO fechada(Quadra quadra) {
        return new DisponibilidadeDTO(quadra.getId(), quadra.getCodigo(), quadra.getTipo().name(), quadra.getAtiva(),
                quadra.getIntervaloMinutos(), false, null, null);
    }

    public static DisponibilidadeDTO aberta(Quadra quadra, HorarioFuncionamento horario) {
        return new DisponibilidadeDTO(quadra.getId(), quadra.getCodigo(), quadra.getTipo().name(), quadra.getAtiva(),
                quadra.getIntervaloMinutos(), true, horario.getAbertura(), horario.getFechamento());
    }
}
