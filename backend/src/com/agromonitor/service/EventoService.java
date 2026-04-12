package com.agromonitor.service;

import com.agromonitor.dto.CrearEventoRequest;
import com.agromonitor.model.Evento;
import com.agromonitor.model.Severidad;
import com.agromonitor.model.TipoEvento;
import com.agromonitor.repository.EventoRepository;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public Evento crear(CrearEventoRequest req) throws SQLException {
        validar(req);
        TipoEvento tipo = TipoEvento.fromString(req.getTipoEvento());
        double valor = req.getValor();

        String severidad = calcularSeveridad(tipo, valor);

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        Evento e = new Evento();
        e.setDispositivoId(req.getDispositivoId().trim());
        e.setTipoEvento(tipo.name());
        e.setValor(valor);
        e.setSeveridad(severidad);
        e.setFechaHora(ahora);

        return repository.insert(e);
    }

    public List<Evento> listar() throws SQLException {
        return repository.findAllOrderByFechaDesc();
    }

    private void validar(CrearEventoRequest req) {
        if (req.getDispositivoId() == null || req.getDispositivoId().isBlank()) {
            throw new IllegalArgumentException("dispositivoId es obligatorio");
        }
        if (req.getTipoEvento() == null || req.getTipoEvento().isBlank()) {
            throw new IllegalArgumentException("tipoEvento es obligatorio");
        }
        if (req.getValor() == null) {
            throw new IllegalArgumentException("valor es obligatorio");
        }
        TipoEvento tipo = TipoEvento.fromString(req.getTipoEvento());
        double v = req.getValor();
        switch (tipo) {
            case conexion -> {
                if (v != 0d && v != 1d) {
                    throw new IllegalArgumentException("conexion: valor debe ser 0 o 1");
                }
            }
            case bateria -> {
                if (v < 0d || v > 100d) {
                    throw new IllegalArgumentException("bateria: valor debe estar entre 0 y 100");
                }
            }
            case temperatura -> {
                // sin rango extra
            }
        }
    }

    private String calcularSeveridad(TipoEvento tipo, double valor) {
        return switch (tipo) {
            case temperatura -> valor > 30d ? Severidad.ADVERTENCIA : Severidad.NORMAL;
            case bateria -> valor < 20d ? Severidad.CRITICO : Severidad.NORMAL;
            case conexion -> valor == 0d ? Severidad.CRITICO : Severidad.NORMAL;
        };
    }
}
