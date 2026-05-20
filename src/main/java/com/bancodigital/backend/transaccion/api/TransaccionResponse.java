package com.bancodigital.backend.transaccion.api;

import com.bancodigital.backend.transaccion.domain.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransaccionResponse(
        Long id,
        TipoTransaccion tipo,
        BigDecimal monto,
        LocalDateTime fecha,
        String descripcion
) {}
