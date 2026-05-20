package com.bancodigital.backend.cuenta.application;

import com.bancodigital.backend.cliente.domain.Cliente;
import com.bancodigital.backend.cliente.infrastructure.ClienteRepository;
import com.bancodigital.backend.cuenta.api.CrearCuentaRequest;
import com.bancodigital.backend.cuenta.domain.Cuenta;
import com.bancodigital.backend.cuenta.domain.TipoCuentaEntity;
import com.bancodigital.backend.cuenta.infrastructure.CuentaRepository;
import com.bancodigital.backend.cuenta.infrastructure.TipoCuentaRepository;
import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final TipoCuentaRepository tipoCuentaRepository;

    public Cuenta crear(CrearCuentaRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + request.clienteId()));

        TipoCuentaEntity tipoCuenta = tipoCuentaRepository.findById(request.tipoCuenta())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de cuenta no encontrado: " + request.tipoCuenta()
                ));

        if (cuentaRepository.existsByCliente_IdAndTipoCuenta_Id(cliente.getId(), tipoCuenta.getId())) {
            String nombreTipo = tipoCuenta.getNombre() != null ? tipoCuenta.getNombre() : "id " + tipoCuenta.getId();
            throw new BusinessException(
                    "El cliente ya tiene una cuenta de tipo \"" + nombreTipo + "\". "
                            + "No se permite duplicar el mismo tipo de cuenta por cliente."
            );
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(cliente);
        cuenta.setTipoCuenta(tipoCuenta);
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setSaldo(BigDecimal.ZERO);
        return cuentaRepository.save(cuenta);
    }

    public Cuenta buscarPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
    }

    private String generarNumeroCuenta() {
        return "ACCT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
