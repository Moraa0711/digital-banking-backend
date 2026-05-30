package com.bancodigital.backend.cliente.api;

import com.bancodigital.backend.cliente.application.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/clientes", "/api/v1/clientes", "/api/v2/clientes"})
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "HU Sprint 1 - Registro y consulta de clientes por id")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "HU-1 Registrar cliente")
    public ClienteResponse registrar(@Valid @RequestBody CrearClienteRequest request) {
        return ClienteMapper.toResponse(clienteService.crear(request));
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "consultarClientePorId",
            summary = "HU-2 Consultar cliente por id",
            description = "Busca un cliente por su identificador interno (id). "
                    + "Para buscar por documento use GET /api/v3/clientes?documento=...")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ClienteResponse consultarPorId(
            @Parameter(
                    name = "id",
                    in = ParameterIn.PATH,
                    required = true,
                    description = "Identificador del cliente",
                    example = "1")
            @PathVariable Long id) {
        return ClienteMapper.toResponse(clienteService.buscarPorId(id));
    }
}
