package com.bancodigital.backend.cuenta.api;

import java.math.BigDecimal;

public record SaldoResponse(
        String numeroCuenta,
        BigDecimal saldo
) {}
