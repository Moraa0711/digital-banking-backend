package com.bancodigital.backend.cliente.api;

import com.bancodigital.backend.cliente.domain.Cliente;

final class ClienteMapper {

    private ClienteMapper() {
    }

    static ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getTipoDocumento() != null ? cliente.getTipoDocumento().getId() : null,
                cliente.getNumeroDocumento(),
                cliente.getNombre(),
                cliente.getApellidos(),
                cliente.getFechaNacimiento(),
                cliente.getEdad(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getDireccion() != null ? cliente.getDireccion().getId() : null,
                cliente.getEstadoCuenta(),
                cliente.getFechaRegistro());
    }
}
