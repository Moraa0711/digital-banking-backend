package com.bancodigital.backend.cuenta.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CuentaResponse(
        Long id,
        Long clienteId,
        Integer tipoCuentaId,
        String numeroCuenta,
        BigDecimal saldo,
        String estado,
        OffsetDateTime fechaCreacion
) {}
