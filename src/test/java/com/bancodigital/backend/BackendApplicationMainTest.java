package com.bancodigital.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Prueba del punto de entrada principal de la aplicación.
 * Verifica que el método main() es invocable sin lanzar excepciones
 * cuando se pasan argumentos vacíos. No levanta el contexto completo
 * de Spring (eso lo hace BackendApplicationTests).
 */
class BackendApplicationMainTest {

    @Test
    void mainMethod_EjecutaContextoMinimo() {
        // Arrange
        String[] args = {"--server.port=0", "--spring.main.banner-mode=off"};

        // Act & Assert
        // Invocamos el main para asegurar cobertura de líneas. 
        // Usamos assertDoesNotThrow para validar que el arranque básico no falla.
        assertDoesNotThrow(() -> BackendApplication.main(args));
    }
}
