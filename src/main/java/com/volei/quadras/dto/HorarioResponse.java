package com.volei.quadras.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.volei.quadras.model.HorarioFuncionamento;

import java.time.LocalTime;

public record HorarioResponse(String diaSemana, @JsonFormat(pattern = "HH:mm") LocalTime abertura,
        @JsonFormat(pattern = "HH:mm") LocalTime fechamento) {
    public static HorarioResponse de(HorarioFuncionamento horario) {
        return new HorarioResponse(horario.getDiaSemana().name(), horario.getAbertura(), horario.getFechamento());
    }
}
