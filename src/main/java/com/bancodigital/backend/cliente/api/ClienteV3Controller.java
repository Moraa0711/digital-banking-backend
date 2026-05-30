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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v3/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes V3", description = "HU-2 Consulta de clientes por numero de documento")
public class ClienteV3Controller {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(
            operationId = "consultarClientePorDocumentoV3",
            summary = "HU-2 Consultar cliente por numero de documento",
            description = "Busca un cliente por su numero de documento. "
                    + "Ejemplo: GET /api/v3/clientes?documento=1234567890")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado con ese documento")
    })
    public ClienteResponse consultarPorDocumento(
            @Parameter(
                    name = "documento",
                    in = ParameterIn.QUERY,
                    required = true,
                    description = "Numero de documento del cliente",
                    example = "1234567890")
            @RequestParam String documento) {
        return ClienteMapper.toResponse(clienteService.buscarPorNumeroDocumento(documento));
    }
}
