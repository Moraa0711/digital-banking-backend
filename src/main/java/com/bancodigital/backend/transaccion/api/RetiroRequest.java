package com.bancodigital.backend.transaccion.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RetiroRequest(
        @NotBlank String numeroCuenta,
        @NotNull
        @DecimalMin(value = "0.01", message = "El monto del retiro debe ser mayor a cero")
        BigDecimal monto,
        String descripcion
) {}
