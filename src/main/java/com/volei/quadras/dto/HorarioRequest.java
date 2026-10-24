package com.volei.quadras.dto;

import java.time.LocalTime;

public record HorarioRequest(String diaSemana, LocalTime abertura, LocalTime fechamento) {
}
