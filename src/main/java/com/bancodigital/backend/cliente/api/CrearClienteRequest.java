package com.bancodigital.backend.cliente.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrearClienteRequest(
        @NotNull Integer tipoDocumentoId,
        @NotBlank String numeroDocumento,
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        @NotBlank String telefono,
        @NotNull LocalDate fechaNacimiento
) {}
