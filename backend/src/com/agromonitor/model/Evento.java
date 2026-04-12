package com.agromonitor.model;

import java.time.OffsetDateTime;

public class Evento {

    private Long id;
    private String dispositivoId;
    private String tipoEvento;
    private double valor;
    private String severidad;
    private OffsetDateTime fechaHora;

    public Evento() {
    }

    public Evento(Long id, String dispositivoId, String tipoEvento, double valor, String severidad,
                  OffsetDateTime fechaHora) {
        this.id = id;
        this.dispositivoId = dispositivoId;
        this.tipoEvento = tipoEvento;
        this.valor = valor;
        this.severidad = severidad;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDispositivoId() {
        return dispositivoId;
    }

    public void setDispositivoId(String dispositivoId) {
        this.dispositivoId = dispositivoId;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public OffsetDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(OffsetDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
