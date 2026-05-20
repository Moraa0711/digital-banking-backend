package com.bancodigital.backend.cliente.api;

import com.bancodigital.backend.cliente.application.ClienteService;
import com.bancodigital.backend.cliente.domain.Cliente;
import com.bancodigital.backend.cliente.domain.TipoDocumento;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    private Cliente crearClienteMock() {
        TipoDocumento td = new TipoDocumento();
        td.setId(1);
        td.setTipo("CC");

        Cliente c = new Cliente();
        c.setId(1L);
        c.setTipoDocumento(td);
        c.setNumeroDocumento("12345678");
        c.setNombre("Juan");
        c.setApellidos("Perez");
        c.setEmail("juan@test.com");
        c.setTelefono("555-1234");
        c.setFechaNacimiento(LocalDate.of(1990, 1, 1).atStartOfDay());
        c.setEdad(34);
        return c;
    }

    // ── POST /api/clientes ────────────────────────────────────────────────

    @Test
    void registrar_Exitoso_Devuelve200ConDatosCliente() throws Exception {
        // Arrange
        when(clienteService.crear(any())).thenReturn(crearClienteMock());

        String body = """
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

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.numeroDocumento").value("12345678"));
    }

    @Test
    void registrar_SinCamposObligatorios_Devuelve400() throws Exception {
        // Arrange - cuerpo vacío omite campos @NotNull/@NotBlank
        String bodyInvalido = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_EmailInvalido_Devuelve400() throws Exception {
        String body = """
                {
                  "tipoDocumentoId": 1,
                  "numeroDocumento": "12345678",
                  "nombre": "Juan",
                  "apellidos": "Perez",
                  "email": "no-es-un-email",
                  "telefono": "555-1234",
                  "fechaNacimiento": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_CuerpoJsonMalFormado_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{campo_roto: "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_CuandoServiceLanzaResourceNotFound_Devuelve404() throws Exception {
        when(clienteService.crear(any())).thenThrow(new ResourceNotFoundException("Tipo de documento no encontrado: 99"));

        String body = """
                {
                  "tipoDocumentoId": 99,
                  "numeroDocumento": "12345678",
                  "nombre": "Juan",
                  "apellidos": "Perez",
                  "email": "juan@test.com",
                  "telefono": "555-1234",
                  "fechaNacimiento": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Tipo de documento no encontrado: 99"));
    }

    // ── GET /api/clientes/{id} ────────────────────────────────────────────

    @Test
    void consultar_ConIdExistente_Devuelve200ConDatosCliente() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(crearClienteMock());

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void consultar_ConIdInexistente_Devuelve404() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado: 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente no encontrado: 99"));
    }

    @Test
    void consultar_ClienteSinDireccionNiTipoDocumento_DevuelveNullsEnIds() throws Exception {
        Cliente c = new Cliente();
        c.setId(2L);
        c.setNombre("Ana");
        c.setApellidos("Lopez");
        c.setNumeroDocumento("99999999");
        c.setEmail("ana@test.com");
        c.setTelefono("111-2222");
        c.setFechaNacimiento(LocalDateTime.now());
        c.setEdad(28);
        // tipoDocumento = null, direccion = null

        when(clienteService.buscarPorId(2L)).thenReturn(c);

        mockMvc.perform(get("/api/clientes/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumentoId").isEmpty())
                .andExpect(jsonPath("$.direccionId").isEmpty());
    }

    @Test
    void registrar_EnRutaV1_FuncionaIgual() throws Exception {
        when(clienteService.crear(any())).thenReturn(crearClienteMock());

        String body = """
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

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registrar_EnRutaV2_FuncionaIgual() throws Exception {
        when(clienteService.crear(any())).thenReturn(crearClienteMock());

        String body = """
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

        mockMvc.perform(post("/api/v2/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
