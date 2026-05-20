package com.bancodigital.backend.cuenta.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad TipoCuentaEntity.
 */
class TipoCuentaEntityTest {

    @Test
    void tipoCuentaEntity_SettersYGetters_FuncionanCorrectamente() {
        // Arrange
        TipoCuentaEntity tipo = new TipoCuentaEntity();

        // Act
        tipo.setId(1);
        tipo.setNombre("AHORROS");
        tipo.setDescripcion("Cuenta de ahorros estándar");

        // Assert
        assertEquals(1, tipo.getId());
        assertEquals("AHORROS", tipo.getNombre());
        assertEquals("Cuenta de ahorros estándar", tipo.getDescripcion());
    }

    @Test
    void tipoCuentaEntity_NombreNulo_GetterDevuelveNull() {
        // Arrange
        TipoCuentaEntity tipo = new TipoCuentaEntity();

        // Act & Assert
        assertNull(tipo.getNombre());
        assertNull(tipo.getDescripcion());
        assertNull(tipo.getId());
    }
}
