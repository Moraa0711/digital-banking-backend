package com.bancodigital.backend.cuenta.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoCuentaConverter implements AttributeConverter<EstadoCuenta, String> {

    @Override
    public String convertToDatabaseColumn(EstadoCuenta estado) {
        return estado == null ? null : estado.name();
    }

    @Override
    public EstadoCuenta convertToEntityAttribute(String valorDb) {
        if (valorDb == null || valorDb.isBlank()) {
            return null;
        }
        return switch (valorDb.trim().toUpperCase()) {
            case "ACTIVA", "ACTIVO" -> EstadoCuenta.ACTIVA;
            case "BLOQUEADA", "BLOQUEADO", "INACTIVA", "INACTIVO" -> EstadoCuenta.BLOQUEADA;
            case "CERRADA", "CERRADO" -> EstadoCuenta.CERRADA;
            default -> EstadoCuenta.valueOf(valorDb.trim().toUpperCase());
        };
    }
}
