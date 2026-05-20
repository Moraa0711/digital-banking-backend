package com.bancodigital.backend.transaccion.infrastructure;

import com.bancodigital.backend.transaccion.domain.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}
