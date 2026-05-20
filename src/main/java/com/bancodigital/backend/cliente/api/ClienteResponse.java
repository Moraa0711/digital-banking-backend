package com.bancodigital.backend.cliente.api;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ClienteResponse(
        Long id,
        Integer tipoDocumentoId,
        String numeroDocumento,
        String nombre,
        String apellidos,
        LocalDateTime fechaNacimiento,
        Integer edad,
        String telefono,
        String email,
        Long direccionId,
        Boolean estadoCuenta,
        OffsetDateTime fechaRegistro
) {}
