package com.bancodigital.backend.transaccion.domain;

import com.bancodigital.backend.cuenta.domain.Cuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Transaccion.
 * Cubre getters/setters Lombok y el método @PrePersist.
 */
class TransaccionTest {

    @Test
    void crearTransaccion_SettersYGetters_FuncionanCorrectamente() {
        // Arrange
        Transaccion t = new Transaccion();
        LocalDateTime fecha = LocalDateTime.of(2024, 6, 15, 10, 0);
        Cuenta origen = new Cuenta();
        Cuenta destino = new Cuenta();

        // Act
        t.setId(1L);
        t.setTipo(TipoTransaccion.DEPOSITO);
        t.setMonto(new BigDecimal("500.00"));
        t.setFecha(fecha);
        t.setDescripcion("Deposito de prueba");
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);

        // Assert
        assertEquals(1L, t.getId());
        assertEquals(TipoTransaccion.DEPOSITO, t.getTipo());
        assertEquals(new BigDecimal("500.00"), t.getMonto());
        assertEquals(fecha, t.getFecha());
        assertEquals("Deposito de prueba", t.getDescripcion());
        assertSame(origen, t.getCuentaOrigen());
        assertSame(destino, t.getCuentaDestino());
    }

    @Test
    void prePersist_CuandoFechaEsNull_AsignaFechaActual() {
        // Arrange
        Transaccion t = new Transaccion();
        assertNull(t.getFecha());

        // Act
        t.prePersist();

        // Assert
        assertNotNull(t.getFecha());
        assertTrue(t.getFecha().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void prePersist_CuandoFechaYaExiste_NoLaSobreescribe() {
        // Arrange
        Transaccion t = new Transaccion();
        LocalDateTime fechaOriginal = LocalDateTime.of(2020, 1, 1, 0, 0);
        t.setFecha(fechaOriginal);

        // Act
        t.prePersist();

        // Assert
        assertEquals(fechaOriginal, t.getFecha());
    }

    @Test
    void tipoTransaccion_Deposito_ExisteEnEnum() {
        // Act & Assert
        assertEquals(TipoTransaccion.DEPOSITO, TipoTransaccion.valueOf("DEPOSITO"));
    }

    @Test
    void tipoTransaccion_Transferencia_ExisteEnEnum() {
        // Act & Assert
        assertEquals(TipoTransaccion.TRANSFERENCIA, TipoTransaccion.valueOf("TRANSFERENCIA"));
    }

    @Test
    void tipoTransaccion_Retiro_ExisteEnEnum() {
        // Act & Assert
        assertEquals(TipoTransaccion.RETIRO, TipoTransaccion.valueOf("RETIRO"));
    }

    @Test
    void tipoTransaccion_Values_ContieneTodosLosValores() {
        // Act
        TipoTransaccion[] valores = TipoTransaccion.values();

        // Assert
        assertEquals(3, valores.length);
    }
}
