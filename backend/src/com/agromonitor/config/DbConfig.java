package com.agromonitor.config;

/**
 * Lectura de la conexión JDBC a PostgreSQL desde variables de entorno.
 * <p>
 * Flujo oficial del proyecto: Postgres levantado con {@code docker-compose.yml} en {@code backend/}
 * (puerto host {@code 15432}). Los valores por defecto coinciden con ese compose (base {@code agromonitor},
 * usuario y contraseña {@code agro}).
 */
public final class DbConfig {

    private final String jdbcUrl;
    private final String user;
    private final String password;

    public DbConfig() {
        // Variables opcionales (si no están definidas, se usan los defaults alineados a docker-compose.yml):
        //   JDBC_URL     -> jdbc:postgresql://localhost:15432/agromonitor
        //   DB_USER      -> agro
        //   DB_PASSWORD  -> agro
        this.jdbcUrl = env("JDBC_URL", "jdbc:postgresql://localhost:15432/agromonitor");
        this.user = env("DB_USER", "agro");
        this.password = env("DB_PASSWORD", "agro");
    }

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
