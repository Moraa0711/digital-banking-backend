package com.bancodigital.backend.cliente.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String tipo;
}
