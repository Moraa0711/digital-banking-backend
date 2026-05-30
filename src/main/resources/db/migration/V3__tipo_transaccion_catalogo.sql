CREATE TABLE IF NOT EXISTS tipo_transaccion (
    id_tipo_transaccion INTEGER PRIMARY KEY,
    nombre_tipo_transaccion VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO tipo_transaccion (id_tipo_transaccion, nombre_tipo_transaccion) VALUES
    (1, 'DEPOSITO'),
    (2, 'RETIRO'),
    (3, 'TRANSFERENCIA')
ON CONFLICT (id_tipo_transaccion) DO NOTHING;

ALTER TABLE transaccion ADD COLUMN IF NOT EXISTS id_tipo_transaccion INTEGER;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'transaccion' AND column_name = 'tipo'
    ) THEN
        UPDATE transaccion t
        SET id_tipo_transaccion = tt.id
        FROM tipo_transaccion tt
        WHERE t.id_tipo_transaccion IS NULL
          AND t.tipo::text = tt.nombre;
    END IF;
END $$;

ALTER TABLE transaccion ALTER COLUMN id_tipo_transaccion SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_transaccion_tipo') THEN
        ALTER TABLE transaccion
            ADD CONSTRAINT fk_transaccion_tipo
            FOREIGN KEY (id_tipo_transaccion) REFERENCES tipo_transaccion(id);
    END IF;
END $$;

ALTER TABLE transaccion DROP COLUMN IF EXISTS tipo;
DROP TYPE IF EXISTS tipo_transaccion_enum;

CREATE INDEX IF NOT EXISTS idx_transaccion_id_tipo ON transaccion(id_tipo_transaccion);
