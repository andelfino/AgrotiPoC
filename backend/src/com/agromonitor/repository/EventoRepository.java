package com.agromonitor.repository;

import com.agromonitor.config.DbConfig;
import com.agromonitor.model.Evento;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventoRepository {

    private final DbConfig dbConfig;

    public EventoRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(
                dbConfig.getJdbcUrl(),
                dbConfig.getUser(),
                dbConfig.getPassword()
        );
    }

    public Evento insert(Evento e) throws SQLException {
        final String sql = """
                INSERT INTO evento (dispositivo_id, tipo_evento, valor, severidad, fecha_hora)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getDispositivoId());
            ps.setString(2, e.getTipoEvento());
            ps.setDouble(3, e.getValor());
            ps.setString(4, e.getSeveridad());
            ps.setObject(5, e.getFechaHora());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getLong(1));
                }
            }
            return e;
        }
    }

    public List<Evento> findAllOrderByFechaDesc() throws SQLException {
        final String sql = """
                SELECT id, dispositivo_id, tipo_evento, valor, severidad, fecha_hora
                FROM evento
                ORDER BY fecha_hora DESC, id DESC
                """;
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Evento> out = new ArrayList<>();
            while (rs.next()) {
                OffsetDateTime fecha = rs.getObject("fecha_hora", OffsetDateTime.class);
                out.add(new Evento(
                        rs.getLong("id"),
                        rs.getString("dispositivo_id"),
                        rs.getString("tipo_evento"),
                        rs.getDouble("valor"),
                        rs.getString("severidad"),
                        fecha
                ));
            }
            return out;
        }
    }
}
