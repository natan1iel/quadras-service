package com.volei.quadras.config;

import com.volei.quadras.model.HorarioFuncionamento;
import com.volei.quadras.model.Quadra;
import com.volei.quadras.model.TipoQuadra;
import com.volei.quadras.repository.QuadraRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Component
public class DadosIniciais implements CommandLineRunner {
    private final QuadraRepository quadraRepository;

    public DadosIniciais(QuadraRepository quadraRepository) {
        this.quadraRepository = quadraRepository;
    }

    @Override
    public void run(String... args) {
        if (quadraRepository.count() > 0) {
            return;
        }

        Quadra areia = new Quadra("Areia A", TipoQuadra.AREIA, 15);
        adicionarFaixa(areia, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, LocalTime.of(7, 0), LocalTime.of(22, 0));
        adicionarFaixa(areia, DayOfWeek.SATURDAY, DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));

        Quadra coberta = new Quadra("Quadra Coberta 1", TipoQuadra.QUADRA, 30);
        adicionarFaixa(coberta, DayOfWeek.MONDAY, DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(23, 0));
        adicionarFaixa(coberta, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(20, 0));

        quadraRepository.save(areia);
        quadraRepository.save(coberta);
    }

    private void adicionarFaixa(Quadra quadra, DayOfWeek de, DayOfWeek ate, LocalTime abertura, LocalTime fechamento) {
        for (int dia = de.getValue(); dia <= ate.getValue(); dia++) {
            quadra.adicionarHorario(new HorarioFuncionamento(DayOfWeek.of(dia), abertura, fechamento));
        }
    }
}
