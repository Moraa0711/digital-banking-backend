package com.bancodigital.backend.cuenta.infrastructure;

import com.bancodigital.backend.cuenta.domain.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    boolean existsByCliente_IdAndTipoCuenta_Id(Long clienteId, Integer tipoCuentaId);
}
