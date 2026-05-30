package com.bancodigital.backend.cuenta.api;

import com.bancodigital.backend.cuenta.application.CuentaService;
import com.bancodigital.backend.cuenta.domain.Cuenta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/cuentas", "/api/v1/cuentas", "/api/v2/cuentas"})
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "HU Sprint 2 - Creacion de cuenta y consulta de saldo")
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    @Operation(summary = "HU-3 Crear cuenta bancaria")
    public CuentaResponse crear(@Valid @RequestBody CrearCuentaRequest request) {
        return toResponse(cuentaService.crear(request));
    }

    @GetMapping("/{numeroCuenta}/saldo")
    @Operation(summary = "HU-4 Consultar saldo")
    public SaldoResponse saldo(@PathVariable String numeroCuenta) {
        Cuenta cuenta = cuentaService.consultarSaldo(numeroCuenta);
        return new SaldoResponse(cuenta.getNumeroCuenta(), cuenta.getSaldo());
    }

    private CuentaResponse toResponse(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getCliente() != null ? cuenta.getCliente().getId() : null,
                cuenta.getTipoCuenta() != null ? cuenta.getTipoCuenta().getId() : null,
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                cuenta.getEstado() != null ? cuenta.getEstado().name() : null,
                cuenta.getFechaCreacion()
        );
    }
}
