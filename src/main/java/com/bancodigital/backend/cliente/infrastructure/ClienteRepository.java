package com.bancodigital.backend.cliente.infrastructure;

import com.bancodigital.backend.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
}
