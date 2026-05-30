package com.bancodigital.backend.transaccion.infrastructure;

import com.bancodigital.backend.transaccion.domain.TipoTransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoTransaccionRepository extends JpaRepository<TipoTransaccionEntity, Integer> {

    Optional<TipoTransaccionEntity> findByNombre(String nombre);
}
