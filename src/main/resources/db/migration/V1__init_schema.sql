CREATE TABLE IF NOT EXISTS cliente (
    id BIGSERIAL PRIMARY KEY,
    id_tipo_documento INTEGER NOT NULL,
    numero_documento VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL,
    fecha_registro TIMESTAMPTZ NOT NULL,
    fecha_nacimiento DATE
);

CREATE TABLE IF NOT EXISTS cuenta (
    id BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(255) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(20) NOT NULL CHECK (tipo_cuenta IN ('AHORROS', 'CORRIENTE')),
    saldo NUMERIC(19,2) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA', 'BLOQUEADA', 'CERRADA')),
    fecha_creacion TIMESTAMPTZ NOT NULL,
    cliente_id BIGINT NOT NULL REFERENCES cliente(id)
);

CREATE TABLE IF NOT EXISTS transaccion (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('DEPOSITO', 'RETIRO', 'TRANSFERENCIA')),
    monto NUMERIC(19,2) NOT NULL,
    fecha TIMESTAMPTZ NOT NULL,
    referencia VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    cuenta_origen_id BIGINT REFERENCES cuenta(id),
    cuenta_destino_id BIGINT REFERENCES cuenta(id)
);

CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_id ON cuenta(cliente_id);
CREATE INDEX IF NOT EXISTS idx_transaccion_origen ON transaccion(cuenta_origen_id);
CREATE INDEX IF NOT EXISTS idx_transaccion_destino ON transaccion(cuenta_destino_id);
