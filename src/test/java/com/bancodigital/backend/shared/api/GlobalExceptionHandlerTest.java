package com.bancodigital.backend.shared.api;

import com.bancodigital.backend.cliente.api.ClienteController;
import com.bancodigital.backend.cliente.application.ClienteService;
import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para el GlobalExceptionHandler.
 * Se usa ClienteController como "puerta de entrada" para disparar
 * cada rama del manejador global de excepciones.
 */
@WebMvcTest(ClienteController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    private static final String BODY_VALIDO = """
            {
              "tipoDocumentoId": 1,
              "numeroDocumento": "12345678",
              "nombre": "Juan",
              "apellidos": "Perez",
              "email": "juan@test.com",
              "telefono": "555-1234",
              "fechaNacimiento": "1990-01-01"
            }
            """;

    // ── ResourceNotFoundException → 404 ──────────────────────────────────

    @Test
    void handleNotFound_CuandoServiceLanzaResourceNotFound_Devuelve404() throws Exception {
        // Arrange
        when(clienteService.crear(any()))
                .thenThrow(new ResourceNotFoundException("Tipo de documento no encontrado: 99"));

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Tipo de documento no encontrado: 99"))
                .andExpect(jsonPath("$.path").value("/api/clientes"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── BusinessException → 400 ───────────────────────────────────────────

    @Test
    void handleBusiness_CuandoServiceLanzaBusinessException_Devuelve400() throws Exception {
        // Arrange
        when(clienteService.crear(any()))
                .thenThrow(new BusinessException("Regla de negocio violada"));

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Regla de negocio violada"));
    }

    // ── MethodArgumentNotValidException → 400 ────────────────────────────

    @Test
    void handleValidation_CampoEmailInvalido_Devuelve400ConMensajeCampo() throws Exception {
        // Arrange - email inválido para disparar @Email
        String bodyEmailMal = """
                {
                  "tipoDocumentoId": 1,
                  "numeroDocumento": "12345678",
                  "nombre": "Juan",
                  "apellidos": "Perez",
                  "email": "no-es-email",
                  "telefono": "555-1234",
                  "fechaNacimiento": "1990-01-01"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyEmailMal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email")));
    }

    @Test
    void handleValidation_MultiplesCamposFaltantes_Devuelve400ConTodosLosMensajes() throws Exception {
        // Arrange - body vacío para disparar múltiples @NotNull/@NotBlank
        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    // ── HttpMessageNotReadableException → 400 ────────────────────────────

    @Test
    void handleUnreadable_JsonMalFormado_Devuelve400ConDetalle() throws Exception {
        // Arrange - JSON roto que no se puede parsear
        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{campo_roto:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Cuerpo JSON invalido")));
    }

    @Test
    void handleUnreadable_TipoIncorrecto_Devuelve400() throws Exception {
        // Arrange - tipoDocumentoId debería ser Integer pero se envía texto
        String bodyTipoMal = """
                {
                  "tipoDocumentoId": "no-es-numero",
                  "numeroDocumento": "12345678",
                  "nombre": "Juan",
                  "apellidos": "Perez",
                  "email": "juan@test.com",
                  "telefono": "555-1234",
                  "fechaNacimiento": "1990-01-01"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyTipoMal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Cuerpo JSON invalido")));
    }

    // ── DataIntegrityViolationException → 409 ────────────────────────────

    @Test
    void handleDataIntegrity_CuandoServiceLanzaDataIntegrity_Devuelve409() throws Exception {
        // Arrange
        when(clienteService.crear(any()))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violated"));

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Violacion de reglas de datos")));
    }

    // ── DataAccessException → 500 ─────────────────────────────────────────

    @Test
    void handleDataAccess_CuandoServiceLanzaDataAccess_Devuelve500() throws Exception {
        // Arrange - usamos subclase concreta de DataAccessException
        when(clienteService.crear(any()))
                .thenThrow(new org.springframework.dao.QueryTimeoutException("DB timeout"));

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Error al acceder a la base de datos")));
    }

    // ── Exception genérico → 500 ──────────────────────────────────────────

    @Test
    void handleGeneric_CuandoServiceLanzaExcepcionInesperada_Devuelve500() throws Exception {
        // Arrange - excepción sin causa (causeMsg == null)
        when(clienteService.crear(any()))
                .thenThrow(new RuntimeException("Error inesperado del sistema"));

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("RuntimeException")));
    }

    @Test
    void handleGeneric_ExcepcionConCausa_Devuelve500ConMensajeCausa() throws Exception {
        // Arrange - excepción con causa encadenada para cubrir la rama "causeMsg != null"
        RuntimeException causa = new RuntimeException("causa raiz del problema");
        RuntimeException excepcionPrincipal = new RuntimeException("error principal", causa);

        when(clienteService.crear(any())).thenThrow(excepcionPrincipal);

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Causa:")));
    }
}
