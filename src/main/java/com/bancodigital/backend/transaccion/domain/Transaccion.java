package com.bancodigital.backend.transaccion.domain;

import com.bancodigital.backend.cuenta.domain.Cuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transaccion")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_transaccion", nullable = false)
    private TipoTransaccionEntity tipoTransaccion;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(updatable = false)
    private LocalDateTime fecha;

    @Column
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_origen")
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_destino")
    private Cuenta cuentaDestino;

    @PrePersist
    void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public TipoTransaccion getTipo() {
        if (tipoTransaccion == null || tipoTransaccion.getNombre() == null) {
            throw new IllegalStateException("La transaccion no tiene tipo asociado");
        }
        return TipoTransaccion.fromNombre(tipoTransaccion.getNombre());
    }
}
