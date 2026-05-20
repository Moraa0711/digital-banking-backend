package com.bancodigital.backend.cliente.infrastructure;

import com.bancodigital.backend.cliente.domain.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {
}
