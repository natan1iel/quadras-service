package com.volei.quadras.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
public class HorarioFuncionamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quadra_id")
    private Quadra quadra;

    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemana;

    private LocalTime abertura;

    private LocalTime fechamento;

    public HorarioFuncionamento() {
    }

    public HorarioFuncionamento(DayOfWeek diaSemana, LocalTime abertura, LocalTime fechamento) {
        this.diaSemana = diaSemana;
        this.abertura = abertura;
        this.fechamento = fechamento;
    }

    public Long getId() {
        return id;
    }

    public void setQuadra(Quadra quadra) {
        this.quadra = quadra;
    }

    public DayOfWeek getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getAbertura() {
        return abertura;
    }

    public LocalTime getFechamento() {
        return fechamento;
    }
}
