package com.bancodigital.backend.transaccion.api;

import java.util.List;

public record HistorialResponse(
        List<HistorialMovimientoResponse> movimientos,
        String mensaje
) {}
