CREATE TABLE IF NOT EXISTS tipo_documento (
    id INTEGER PRIMARY KEY,
    tipo VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO tipo_documento (tipo) VALUES
    ('CC'),
    ('TI'),
    ('CE'),
    ('PASAPORTE')
ON CONFLICT (tipo) DO NOTHING;

CREATE TABLE IF NOT EXISTS direccion (
    id BIGSERIAL PRIMARY KEY,
    ciudad VARCHAR(255) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    departamento VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_cuenta (
    id INTEGER PRIMARY KEY,
    tipo VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL
);

INSERT INTO tipo_cuenta (tipo, descripcion) VALUES
    ('AHORROS', 'Cuenta de ahorros'),
    ('CORRIENTE', 'Cuenta corriente')
ON CONFLICT (tipo) DO NOTHING;

ALTER TABLE cliente ADD COLUMN IF NOT EXISTS id_direccion BIGINT;
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS correo VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'cliente' AND column_name = 'email'
    ) THEN
        EXECUTE 'UPDATE cliente SET correo = COALESCE(correo, email)';
    END IF;
END $$;

ALTER TABLE cliente ALTER COLUMN correo SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_cliente_correo ON cliente(correo);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_cliente_tipo_documento'
    ) THEN
        ALTER TABLE cliente
            ADD CONSTRAINT fk_cliente_tipo_documento
            FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_cliente_direccion'
    ) THEN
        ALTER TABLE cliente
            ADD CONSTRAINT fk_cliente_direccion
            FOREIGN KEY (id_direccion) REFERENCES direccion(id);
    END IF;
END $$;

ALTER TABLE cuenta ADD COLUMN IF NOT EXISTS id_cliente BIGINT;
ALTER TABLE cuenta ADD COLUMN IF NOT EXISTS id_tipo_cuenta INTEGER;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cuenta' AND column_name = 'cliente_id'
    ) THEN
        UPDATE cuenta SET id_cliente = COALESCE(id_cliente, cliente_id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cuenta' AND column_name = 'tipo_cuenta'
    ) THEN
        UPDATE cuenta c
        SET id_tipo_cuenta = tc.id
        FROM tipo_cuenta tc
        WHERE c.id_tipo_cuenta IS NULL
          AND c.tipo_cuenta = tc.tipo;
    END IF;
END $$;

ALTER TABLE cuenta ALTER COLUMN id_cliente SET NOT NULL;
ALTER TABLE cuenta ALTER COLUMN id_tipo_cuenta SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cuenta_cliente'
    ) THEN
        ALTER TABLE cuenta
            ADD CONSTRAINT fk_cuenta_cliente
            FOREIGN KEY (id_cliente) REFERENCES cliente(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_cuenta_tipo_cuenta'
    ) THEN
        ALTER TABLE cuenta
            ADD CONSTRAINT fk_cuenta_tipo_cuenta
            FOREIGN KEY (id_tipo_cuenta) REFERENCES tipo_cuenta(id);
    END IF;
END $$;

ALTER TABLE transaccion ADD COLUMN IF NOT EXISTS id_cuenta_origen BIGINT;
ALTER TABLE transaccion ADD COLUMN IF NOT EXISTS id_cuenta_destino BIGINT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'transaccion' AND column_name = 'cuenta_origen_id'
    ) THEN
        UPDATE transaccion
        SET id_cuenta_origen = COALESCE(id_cuenta_origen, cuenta_origen_id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'transaccion' AND column_name = 'cuenta_destino_id'
    ) THEN
        UPDATE transaccion
        SET id_cuenta_destino = COALESCE(id_cuenta_destino, cuenta_destino_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_transaccion_cuenta_origen'
    ) THEN
        ALTER TABLE transaccion
            ADD CONSTRAINT fk_transaccion_cuenta_origen
            FOREIGN KEY (id_cuenta_origen) REFERENCES cuenta(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_transaccion_cuenta_destino'
    ) THEN
        ALTER TABLE transaccion
            ADD CONSTRAINT fk_transaccion_cuenta_destino
            FOREIGN KEY (id_cuenta_destino) REFERENCES cuenta(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_cliente_tipo_documento ON cliente(id_tipo_documento);
CREATE INDEX IF NOT EXISTS idx_cliente_direccion ON cliente(id_direccion);
CREATE INDEX IF NOT EXISTS idx_cuenta_id_cliente ON cuenta(id_cliente);
CREATE INDEX IF NOT EXISTS idx_cuenta_id_tipo_cuenta ON cuenta(id_tipo_cuenta);
CREATE INDEX IF NOT EXISTS idx_transaccion_id_cuenta_origen ON transaccion(id_cuenta_origen);
CREATE INDEX IF NOT EXISTS idx_transaccion_id_cuenta_destino ON transaccion(id_cuenta_destino);
