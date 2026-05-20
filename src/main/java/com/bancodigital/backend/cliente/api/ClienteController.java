package com.bancodigital.backend.cliente.api;

import com.bancodigital.backend.cliente.application.ClienteService;
import com.bancodigital.backend.cliente.domain.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/clientes", "/api/v1/clientes", "/api/v2/clientes"})
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "HU Sprint 1 - Registro y consulta de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "HU-1 Registrar cliente")
    public ClienteResponse registrar(@Valid @RequestBody CrearClienteRequest request) {
        return toResponse(clienteService.crear(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "HU-2 Consultar cliente por id")
    public ClienteResponse consultar(@PathVariable Long id) {
        return toResponse(clienteService.buscarPorId(id));
    }

    private ClienteResponse toResponse(Cliente cliente) {
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
                cliente.getFechaRegistro()
        );
    }
}
