package com.bancodigital.backend.cuenta.infrastructure;

import com.bancodigital.backend.cuenta.domain.TipoCuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoCuentaRepository extends JpaRepository<TipoCuentaEntity, Integer> {
    Optional<TipoCuentaEntity> findByNombre(String nombre);
}
