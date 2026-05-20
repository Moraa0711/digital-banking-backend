package com.bancodigital.backend.transaccion.api;

import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import com.bancodigital.backend.transaccion.application.TransaccionService;
import com.bancodigital.backend.transaccion.domain.TipoTransaccion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransaccionController.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransaccionService transaccionService;

    private TransaccionResponse buildResponse(TipoTransaccion tipo, BigDecimal monto) {
        return new TransaccionResponse(1L, tipo, monto, LocalDateTime.now(), "Test");
    }

    // ── POST /api/transacciones/depositos ────────────────────────────────

    @Test
    void depositar_Exitoso_Devuelve200ConResponse() throws Exception {
        when(transaccionService.depositar(any()))
                .thenReturn(buildResponse(TipoTransaccion.DEPOSITO, new BigDecimal("500.00")));

        String body = """
                {
                  "numeroCuenta": "ACCT-ABC123",
                  "monto": 500.00,
                  "descripcion": "Deposito inicial"
                }
                """;

        mockMvc.perform(post("/api/transacciones/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.monto").value(500.00));
    }

    @Test
    void depositar_SinNumeroCuenta_Devuelve400() throws Exception {
        String body = """
                {
                  "monto": 500.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void depositar_MontoMenorAlMinimo_Devuelve400() throws Exception {
        String body = """
                {
                  "numeroCuenta": "ACCT-ABC123",
                  "monto": 0.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void depositar_CuentaNoExiste_Devuelve404() throws Exception {
        when(transaccionService.depositar(any()))
                .thenThrow(new ResourceNotFoundException("Cuenta no encontrada: ACCT-NOEXI"));

        String body = """
                {
                  "numeroCuenta": "ACCT-NOEXI",
                  "monto": 100.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada: ACCT-NOEXI"));
    }

    @Test
    void depositar_CuerpoJsonMalFormado_Devuelve400() throws Exception {
        mockMvc.perform(post("/api/transacciones/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{roto:"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/transacciones/transferencias ──────────────────────────

    @Test
    void transferir_Exitoso_Devuelve200ConResponse() throws Exception {
        when(transaccionService.transferir(any()))
                .thenReturn(buildResponse(TipoTransaccion.TRANSFERENCIA, new BigDecimal("200.00")));

        String body = """
                {
                  "numeroCuentaOrigen": "ACCT-ORI123",
                  "numeroCuentaDestino": "ACCT-DES456",
                  "monto": 200.00,
                  "descripcion": "Transferencia test"
                }
                """;

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.monto").value(200.00));
    }

    @Test
    void transferir_SinCuentaOrigen_Devuelve400() throws Exception {
        String body = """
                {
                  "numeroCuentaDestino": "ACCT-DES456",
                  "monto": 200.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferir_OrigenIgualDestino_Devuelve400() throws Exception {
        when(transaccionService.transferir(any()))
                .thenThrow(new BusinessException("La cuenta origen y destino deben ser diferentes"));

        String body = """
                {
                  "numeroCuentaOrigen": "ACCT-IGUAL",
                  "numeroCuentaDestino": "ACCT-IGUAL",
                  "monto": 100.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La cuenta origen y destino deben ser diferentes"));
    }

    @Test
    void transferir_SaldoInsuficiente_Devuelve400() throws Exception {
        when(transaccionService.transferir(any()))
                .thenThrow(new BusinessException("Saldo insuficiente para realizar la transferencia"));

        String body = """
                {
                  "numeroCuentaOrigen": "ACCT-ORI",
                  "numeroCuentaDestino": "ACCT-DES",
                  "monto": 9999999.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo insuficiente para realizar la transferencia"));
    }

    @Test
    void transferir_CuentaOrigenNoExiste_Devuelve404() throws Exception {
        when(transaccionService.transferir(any()))
                .thenThrow(new ResourceNotFoundException("Cuenta no encontrada: ACCT-NOEXI"));

        String body = """
                {
                  "numeroCuentaOrigen": "ACCT-NOEXI",
                  "numeroCuentaDestino": "ACCT-DES456",
                  "monto": 100.00
                }
                """;

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferir_EnRutaV2_FuncionaIgual() throws Exception {
        when(transaccionService.transferir(any()))
                .thenReturn(buildResponse(TipoTransaccion.TRANSFERENCIA, new BigDecimal("100.00")));

        String body = """
                {
                  "numeroCuentaOrigen": "ACCT-ORI",
                  "numeroCuentaDestino": "ACCT-DES",
                  "monto": 100.00
                }
                """;

        mockMvc.perform(post("/api/v2/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"));
    }
}
