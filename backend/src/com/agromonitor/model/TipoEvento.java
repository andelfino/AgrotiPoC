package com.agromonitor.model;

import java.util.Locale;

public enum TipoEvento {
    temperatura,
    bateria,
    conexion;

    public static TipoEvento fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("tipoEvento es obligatorio");
        }
        String n = raw.trim().toLowerCase(Locale.ROOT);
        for (TipoEvento t : values()) {
            if (t.name().equals(n)) {
                return t;
            }
        }
        throw new IllegalArgumentException("tipoEvento inválido: " + raw);
    }
}
