package com.bancodigital.backend.cuenta.api;

import jakarta.validation.constraints.NotNull;

public record CrearCuentaRequest(
        @NotNull Long clienteId,
        @NotNull Integer tipoCuenta
) {}
