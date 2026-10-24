package com.volei.quadras.dto;

import java.util.List;

public record QuadraRequest(String codigo, String tipo, Integer intervaloMinutos, List<HorarioRequest> horarios) {
}
