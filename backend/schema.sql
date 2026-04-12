CREATE TABLE IF NOT EXISTS evento (
    id             BIGSERIAL PRIMARY KEY,
    dispositivo_id VARCHAR(128) NOT NULL,
    tipo_evento    VARCHAR(32)  NOT NULL,
    valor          DOUBLE PRECISION NOT NULL,
    severidad      VARCHAR(16)  NOT NULL,
    fecha_hora     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
