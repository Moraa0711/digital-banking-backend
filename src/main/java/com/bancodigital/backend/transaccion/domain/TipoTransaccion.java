package com.bancodigital.backend.transaccion.domain;

public enum TipoTransaccion {
    DEPOSITO(1),
    RETIRO(2),
    TRANSFERENCIA(3);

    private final int id;

    TipoTransaccion(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TipoTransaccion fromNombre(String nombre) {
        return valueOf(nombre);
    }

    public static TipoTransaccion fromId(int id) {
        for (TipoTransaccion tipo : values()) {
            if (tipo.id == id) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de transaccion no valido: " + id);
    }
}
