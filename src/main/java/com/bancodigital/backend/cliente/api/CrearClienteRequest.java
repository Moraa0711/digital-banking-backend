package com.bancodigital.backend.cliente.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CrearClienteRequest(
        @NotNull Integer tipoDocumentoId,
        @NotBlank String numeroDocumento,
        @NotBlank String nombre,
        @NotBlank String apellidos,
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(regexp = "^[0-9]{7,}$", message = "El numero de telefono no es valido")
        String telefono,
        @NotNull LocalDate fechaNacimiento
) {}
