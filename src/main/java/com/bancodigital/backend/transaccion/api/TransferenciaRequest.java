package com.bancodigital.backend.transaccion.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferenciaRequest(
        @NotBlank String numeroCuentaOrigen,
        @NotBlank String numeroCuentaDestino,
        @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
        String descripcion
) {}
