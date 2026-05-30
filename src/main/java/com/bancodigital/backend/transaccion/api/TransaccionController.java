package com.bancodigital.backend.transaccion.api;

import com.bancodigital.backend.transaccion.application.TransaccionService;
import com.bancodigital.backend.transaccion.domain.TipoTransaccion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/transacciones", "/api/v1/transacciones", "/api/v2/transacciones"})
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "HU Sprint 2-3 - Depositos, transferencias, retiros e historial")
public class TransaccionController {

    private final TransaccionService transaccionService;

    @PostMapping("/depositos")
    @Operation(summary = "HU-6 Depositar dinero")
    public TransaccionResponse depositar(@Valid @RequestBody DepositoRequest request) {
        return transaccionService.depositar(request);
    }

    @PostMapping("/retiros")
    @Operation(summary = "HU-7 Retirar dinero")
    public RetiroResponse retirar(@Valid @RequestBody RetiroRequest request) {
        return transaccionService.retirar(request);
    }

    @PostMapping("/transferencias")
    @Operation(summary = "HU-5 Transferir dinero")
    public TransaccionResponse transferir(@Valid @RequestBody TransferenciaRequest request) {
        return transaccionService.transferir(request);
    }

    @GetMapping("/historial")
    @Operation(summary = "HU-8 Consultar historial de transacciones")
    public HistorialResponse historial(
            @RequestParam String numeroCuenta,
            @RequestParam Long clienteId,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) TipoTransaccion tipo) {
        return transaccionService.consultarHistorial(numeroCuenta, clienteId, fechaInicio, fechaFin, tipo);
    }
}
