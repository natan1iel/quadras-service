package com.volei.quadras.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Quadra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    private TipoQuadra tipo;

    private Integer intervaloMinutos;

    private Boolean ativa;

    @OneToMany(mappedBy = "quadra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<HorarioFuncionamento> horarios = new ArrayList<>();

    public Quadra() {
    }

    public Quadra(String codigo, TipoQuadra tipo, Integer intervaloMinutos) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.intervaloMinutos = intervaloMinutos;
        this.ativa = true;
    }

    public void adicionarHorario(HorarioFuncionamento horario) {
        horario.setQuadra(this);
        this.horarios.add(horario);
    }

    public void substituirHorarios(List<HorarioFuncionamento> novos) {
        this.horarios.clear();
        for (HorarioFuncionamento h : novos) {
            adicionarHorario(h);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public TipoQuadra getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuadra tipo) {
        this.tipo = tipo;
    }

    public Integer getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(Integer intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public List<HorarioFuncionamento> getHorarios() {
        return horarios;
    }
}
