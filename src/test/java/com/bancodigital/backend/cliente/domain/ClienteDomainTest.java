package com.bancodigital.backend.cliente.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de las entidades del dominio cliente:
 * Cliente, Direccion y TipoDocumento.
 */
class ClienteDomainTest {

    // ── Cliente ───────────────────────────────────────────────────────────

    @Test
    void cliente_SettersYGetters_FuncionanCorrectamente() {
        // Arrange
        Cliente c = new Cliente();
        TipoDocumento td = new TipoDocumento();
        Direccion dir = new Direccion();

        // Act
        c.setId(1L);
        c.setTipoDocumento(td);
        c.setNumeroDocumento("12345678");
        c.setNombre("Juan");
        c.setApellidos("Perez");
        c.setEmail("juan@test.com");
        c.setTelefono("555-1234");
        c.setEstadoCuenta(true);
        c.setFechaRegistro(OffsetDateTime.now());
        c.setFechaNacimiento(LocalDateTime.of(1990, 1, 1, 0, 0));
        c.setEdad(34);
        c.setDireccion(dir);

        // Assert
        assertEquals(1L, c.getId());
        assertSame(td, c.getTipoDocumento());
        assertEquals("12345678", c.getNumeroDocumento());
        assertEquals("Juan", c.getNombre());
        assertEquals("Perez", c.getApellidos());
        assertEquals("juan@test.com", c.getEmail());
        assertEquals("555-1234", c.getTelefono());
        assertTrue(c.getEstadoCuenta());
        assertNotNull(c.getFechaRegistro());
        assertEquals(LocalDateTime.of(1990, 1, 1, 0, 0), c.getFechaNacimiento());
        assertEquals(34, c.getEdad());
        assertSame(dir, c.getDireccion());
    }

    @Test
    void cliente_PrePersist_CuandoEstadoCuentaEsNull_LoSetaEnTrue() {
        // Arrange
        Cliente c = new Cliente();
        assertNull(c.getEstadoCuenta());
        assertNull(c.getFechaRegistro());

        // Act
        c.prePersist();

        // Assert
        assertTrue(c.getEstadoCuenta());
        assertNotNull(c.getFechaRegistro());
    }

    @Test
    void cliente_PrePersist_CuandoEstadoCuentaYaExiste_NoLoSobreescribe() {
        // Arrange
        Cliente c = new Cliente();
        c.setEstadoCuenta(false);

        // Act
        c.prePersist();

        // Assert
        assertFalse(c.getEstadoCuenta());
    }

    @Test
    void cliente_PrePersist_CuandoFechaRegistroYaExiste_NoLaSobreescribe() {
        // Arrange
        Cliente c = new Cliente();
        OffsetDateTime fechaOriginal = OffsetDateTime.now().minusDays(10);
        c.setFechaRegistro(fechaOriginal);

        // Act
        c.prePersist();

        // Assert
        assertEquals(fechaOriginal, c.getFechaRegistro());
    }

    // ── Direccion ─────────────────────────────────────────────────────────

    @Test
    void direccion_SettersYGetters_FuncionanCorrectamente() {
        // Arrange
        Direccion d = new Direccion();

        // Act
        d.setId(1L);
        d.setCiudad("Medellín");
        d.setLinea("Calle 10 #20-30");
        d.setDepartamento("Antioquia");

        // Assert
        assertEquals(1L, d.getId());
        assertEquals("Medellín", d.getCiudad());
        assertEquals("Calle 10 #20-30", d.getLinea());
        assertEquals("Antioquia", d.getDepartamento());
    }

    // ── TipoDocumento ─────────────────────────────────────────────────────

    @Test
    void tipoDocumento_SettersYGetters_FuncionanCorrectamente() {
        // Arrange
        TipoDocumento td = new TipoDocumento();

        // Act
        td.setId(1);
        td.setTipo("CC");

        // Assert
        assertEquals(1, td.getId());
        assertEquals("CC", td.getTipo());
    }
}
