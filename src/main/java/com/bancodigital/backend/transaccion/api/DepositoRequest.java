package com.bancodigital.backend.transaccion.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositoRequest(
        @NotBlank String numeroCuenta,
        @NotNull @DecimalMin(value = "0.01") BigDecimal monto,
        String descripcion
) {}
