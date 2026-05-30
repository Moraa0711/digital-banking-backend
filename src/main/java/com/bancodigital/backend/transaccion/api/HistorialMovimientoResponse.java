package com.bancodigital.backend.transaccion.api;

import com.bancodigital.backend.transaccion.domain.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialMovimientoResponse(
        Long id,
        LocalDateTime fecha,
        TipoTransaccion tipo,
        BigDecimal monto,
        BigDecimal saldoResultante,
        String descripcion
) {}
