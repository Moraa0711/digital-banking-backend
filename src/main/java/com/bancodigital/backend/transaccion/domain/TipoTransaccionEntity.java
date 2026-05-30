package com.bancodigital.backend.transaccion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_transaccion")
public class TipoTransaccionEntity {

    @Id
    @Column(name = "id_tipo_transaccion")
    private Integer id;

    @Column(name = "nombre_tipo_transaccion", nullable = false, unique = true)
    private String nombre;
}
