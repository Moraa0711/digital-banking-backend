package com.bancodigital.backend.transaccion.infrastructure;

import com.bancodigital.backend.transaccion.domain.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    @Query("""
            SELECT t FROM Transaccion t
            JOIN FETCH t.tipoTransaccion
            LEFT JOIN FETCH t.cuentaOrigen
            LEFT JOIN FETCH t.cuentaDestino
            WHERE t.cuentaOrigen.id = :cuentaId
               OR t.cuentaDestino.id = :cuentaId
            ORDER BY t.fecha ASC, t.id ASC
            """)
    List<Transaccion> findMovimientosByCuentaIdAsc(@Param("cuentaId") Long cuentaId);
}
