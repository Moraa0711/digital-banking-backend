package com.bancodigital.backend.cuenta.application;

import com.bancodigital.backend.cliente.domain.Cliente;
import com.bancodigital.backend.cliente.infrastructure.ClienteRepository;
import com.bancodigital.backend.cuenta.api.CrearCuentaRequest;
import com.bancodigital.backend.cuenta.domain.Cuenta;
import com.bancodigital.backend.cuenta.domain.TipoCuentaEntity;
import com.bancodigital.backend.cuenta.infrastructure.CuentaRepository;
import com.bancodigital.backend.cuenta.infrastructure.TipoCuentaRepository;
import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private TipoCuentaRepository tipoCuentaRepository;

    @InjectMocks private CuentaService cuentaService;

    private Cliente cliente;
    private TipoCuentaEntity tipoCuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        tipoCuenta = new TipoCuentaEntity();
        tipoCuenta.setId(1);
        tipoCuenta.setNombre("AHORROS");
    }

    @Test
    void crear_Exitoso() {
        CrearCuentaRequest req = new CrearCuentaRequest(1L, 1);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tipoCuentaRepository.findById(1)).thenReturn(Optional.of(tipoCuenta));
        when(cuentaRepository.existsByCliente_IdAndTipoCuenta_Id(1L, 1)).thenReturn(false);
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(i -> i.getArguments()[0]);

        Cuenta resultado = cuentaService.crear(req);

        assertNotNull(resultado);
        assertEquals(cliente, resultado.getCliente());
        assertTrue(resultado.getNumeroCuenta().startsWith("ACCT-"));
        verify(cuentaRepository).save(any(Cuenta.class));
    }

    @Test
    void crear_ClienteNoEncontrado_LanzaExcepcion() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.crear(new CrearCuentaRequest(1L, 1)));
    }

    @Test
    void crear_TipoCuentaNoEncontrado_LanzaExcepcion() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tipoCuentaRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.crear(new CrearCuentaRequest(1L, 1)));
    }

    @Test
    void crear_CuentaDuplicada_LanzaBusinessException() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tipoCuentaRepository.findById(1)).thenReturn(Optional.of(tipoCuenta));
        when(cuentaRepository.existsByCliente_IdAndTipoCuenta_Id(1L, 1)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> cuentaService.crear(new CrearCuentaRequest(1L, 1)));
        assertTrue(ex.getMessage().contains("ya tiene una cuenta de tipo \"AHORROS\""));
    }

    @Test
    void buscarPorNumero_Exitoso() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("ACCT-123");
        when(cuentaRepository.findByNumeroCuenta("ACCT-123")).thenReturn(Optional.of(cuenta));

        Cuenta resultado = cuentaService.buscarPorNumero("ACCT-123");

        assertEquals("ACCT-123", resultado.getNumeroCuenta());
    }

    @Test
    void buscarPorNumero_NoExiste_LanzaExcepcion() {
        when(cuentaRepository.findByNumeroCuenta("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cuentaService.buscarPorNumero("INVALID"));
    }
}