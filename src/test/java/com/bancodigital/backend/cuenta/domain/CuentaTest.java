package com.bancodigital.backend.cuenta.domain;

import com.bancodigital.backend.cliente.domain.Cliente;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad Cuenta.
 * Cubre getters/setters Lombok y todas las ramas del @PrePersist.
 */
class CuentaTest {

    @Test
    void crearCuenta_SettersYGetters_FuncionanCorrectamente() {
        // Arrange
        Cuenta c = new Cuenta();
        Cliente cliente = new Cliente();
        TipoCuentaEntity tipo = new TipoCuentaEntity();
        OffsetDateTime fecha = OffsetDateTime.now();

        // Act
        c.setId(1L);
        c.setNumeroCuenta("ACCT-TEST");
        c.setTipoCuenta(tipo);
        c.setSaldo(new BigDecimal("1000.00"));
        c.setEstado(EstadoCuenta.ACTIVA);
        c.setFechaCreacion(fecha);
        c.setCliente(cliente);

        // Assert
        assertEquals(1L, c.getId());
        assertEquals("ACCT-TEST", c.getNumeroCuenta());
        assertSame(tipo, c.getTipoCuenta());
        assertEquals(new BigDecimal("1000.00"), c.getSaldo());
        assertEquals(EstadoCuenta.ACTIVA, c.getEstado());
        assertEquals(fecha, c.getFechaCreacion());
        assertSame(cliente, c.getCliente());
    }

    @Test
    void prePersist_CuandoTodoEsNull_AsignaValoresPorDefecto() {
        // Arrange
        Cuenta c = new Cuenta();
        assertNull(c.getSaldo());
        assertNull(c.getEstado());
        assertNull(c.getFechaCreacion());

        // Act
        c.prePersist();

        // Assert
        assertEquals(BigDecimal.ZERO, c.getSaldo());
        assertEquals(EstadoCuenta.ACTIVA, c.getEstado());
        assertNotNull(c.getFechaCreacion());
    }

    @Test
    void prePersist_CuandoSaldoYaExiste_NoLoSobreescribe() {
        // Arrange
        Cuenta c = new Cuenta();
        c.setSaldo(new BigDecimal("500.00"));

        // Act
        c.prePersist();

        // Assert
        assertEquals(new BigDecimal("500.00"), c.getSaldo());
    }

    @Test
    void prePersist_CuandoEstadoYaExiste_NoLoSobreescribe() {
        // Arrange
        Cuenta c = new Cuenta();
        c.setEstado(EstadoCuenta.BLOQUEADA);

        // Act
        c.prePersist();

        // Assert
        assertEquals(EstadoCuenta.BLOQUEADA, c.getEstado());
    }

    @Test
    void prePersist_CuandoFechaCreacionYaExiste_NoLaSobreescribe() {
        // Arrange
        Cuenta c = new Cuenta();
        OffsetDateTime fechaOriginal = OffsetDateTime.now().minusDays(1);
        c.setFechaCreacion(fechaOriginal);

        // Act
        c.prePersist();

        // Assert
        assertEquals(fechaOriginal, c.getFechaCreacion());
    }

    @Test
    void estadoCuenta_Values_ContieneTodosLosValores() {
        // Act
        EstadoCuenta[] valores = EstadoCuenta.values();

        // Assert
        assertTrue(valores.length >= 2);
    }

    @Test
    void tipoCuenta_Values_ContieneTodosLosValores() {
        // Act
        TipoCuenta[] valores = TipoCuenta.values();

        // Assert
        assertEquals(2, valores.length);
    }

    @Test
    void tipoCuenta_Ahorros_ExisteEnEnum() {
        // Act & Assert
        assertEquals(TipoCuenta.AHORROS, TipoCuenta.valueOf("AHORROS"));
    }

    @Test
    void tipoCuenta_Corriente_ExisteEnEnum() {
        // Act & Assert
        assertEquals(TipoCuenta.CORRIENTE, TipoCuenta.valueOf("CORRIENTE"));
    }
}
