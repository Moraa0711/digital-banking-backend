package com.bancodigital.backend.cliente.infrastructure;

import com.bancodigital.backend.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
