package com.bancodigital.backend.cuenta.api;

import com.bancodigital.backend.cliente.domain.Cliente;
import com.bancodigital.backend.cuenta.application.CuentaService;
import com.bancodigital.backend.cuenta.domain.Cuenta;
import com.bancodigital.backend.cuenta.domain.EstadoCuenta;
import com.bancodigital.backend.cuenta.domain.TipoCuentaEntity;
import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaService cuentaService;

    private Cuenta crearCuentaMock() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        TipoCuentaEntity tipo = new TipoCuentaEntity();
        tipo.setId(1);
        tipo.setNombre("AHORROS");

        Cuenta cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setCliente(cliente);
        cuenta.setTipoCuenta(tipo);
        cuenta.setNumeroCuenta("ACCT-ABC123456789");
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        return cuenta;
    }

    // ── POST /api/cuentas ─────────────────────────────────────────────────

    @Test
    void crear_Exitoso_Devuelve200ConDatosCuenta() throws Exception {
        when(cuentaService.crear(any())).thenReturn(crearCuentaMock());

        String body = """
                {
                  "clienteId": 1,
                  "tipoCuenta": 1
                }
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.numeroCuenta").value("ACCT-ABC123456789"))
                .andExpect(jsonPath("$.saldo").value(0));
    }

    @Test
    void crear_SinCamposObligatorios_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_ClienteNoExiste_Devuelve404() throws Exception {
        when(cuentaService.crear(any()))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado: 99"));

        String body = """
                {
                  "clienteId": 99,
                  "tipoCuenta": 1
                }
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado: 99"));
    }

    @Test
    void crear_TipoCuentaNoExiste_Devuelve404() throws Exception {
        when(cuentaService.crear(any()))
                .thenThrow(new ResourceNotFoundException("Tipo de cuenta no encontrado: 99"));

        String body = """
                {
                  "clienteId": 1,
                  "tipoCuenta": 99
                }
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_ClienteConCuentaDuplicada_Devuelve400() throws Exception {
        when(cuentaService.crear(any()))
                .thenThrow(new BusinessException("El cliente ya tiene una cuenta de tipo \"AHORROS\". No se permite duplicar el mismo tipo de cuenta por cliente."));

        String body = """
                {
                  "clienteId": 1,
                  "tipoCuenta": 1
                }
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void crear_CuerpoJsonMalFormado_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{roto:"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/cuentas/{numeroCuenta}/saldo ────────────────────────────

    @Test
    void saldo_CuentaExistente_Devuelve200ConSaldo() throws Exception {
        Cuenta cuenta = crearCuentaMock();
        cuenta.setSaldo(new BigDecimal("1500.00"));
        when(cuentaService.buscarPorNumero("ACCT-ABC123456789")).thenReturn(cuenta);

        mockMvc.perform(get("/api/cuentas/ACCT-ABC123456789/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("ACCT-ABC123456789"))
                .andExpect(jsonPath("$.saldo").value(1500.00));
    }

    @Test
    void saldo_CuentaInexistente_Devuelve404() throws Exception {
        when(cuentaService.buscarPorNumero("ACCT-NOEXI"))
                .thenThrow(new ResourceNotFoundException("Cuenta no encontrada: ACCT-NOEXI"));

        mockMvc.perform(get("/api/cuentas/ACCT-NOEXI/saldo"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada: ACCT-NOEXI"));
    }

    @Test
    void crear_CuentaSinClienteNiTipoCuenta_DevuelveNullsEnIds() throws Exception {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(5L);
        cuenta.setNumeroCuenta("ACCT-XYZ");
        cuenta.setSaldo(BigDecimal.ZERO);
        // cliente=null, tipoCuenta=null, estado=null

        when(cuentaService.crear(any())).thenReturn(cuenta);

        String body = """
                {
                  "clienteId": 1,
                  "tipoCuenta": 1
                }
                """;

        mockMvc.perform(post("/api/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").isEmpty())
                .andExpect(jsonPath("$.tipoCuentaId").isEmpty());
    }

    @Test
    void crear_EnRutaV1_FuncionaIgual() throws Exception {
        when(cuentaService.crear(any())).thenReturn(crearCuentaMock());

        String body = """
                {
                  "clienteId": 1,
                  "tipoCuenta": 1
                }
                """;

        mockMvc.perform(post("/api/v1/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }
}
