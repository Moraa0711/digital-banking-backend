package com.bancodigital.backend.transaccion.api;

import com.bancodigital.backend.transaccion.application.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/transacciones", "/api/v1/transacciones", "/api/v2/transacciones"})
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "HU Sprint 2 - Depositos y transferencias")
public class TransaccionController {

    private final TransaccionService transaccionService;

    @PostMapping("/depositos")
    @Operation(summary = "HU-6 Depositar dinero")
    public TransaccionResponse depositar(@Valid @RequestBody DepositoRequest request) {
        return transaccionService.depositar(request);
    }

    @PostMapping("/transferencias")
    @Operation(summary = "HU-5 Transferir dinero")
    public TransaccionResponse transferir(@Valid @RequestBody TransferenciaRequest request) {
        return transaccionService.transferir(request);
    }
}
