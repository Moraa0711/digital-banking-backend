package com.bancodigital.backend.transaccion.application;

import com.bancodigital.backend.cuenta.application.CuentaService;
import com.bancodigital.backend.cuenta.domain.Cuenta;
import com.bancodigital.backend.cuenta.infrastructure.CuentaRepository;
import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.transaccion.api.DepositoRequest;
import com.bancodigital.backend.transaccion.api.TransaccionResponse;
import com.bancodigital.backend.transaccion.api.TransferenciaRequest;
import com.bancodigital.backend.transaccion.domain.TipoTransaccion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private CuentaService cuentaService;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private Environment environment;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TransaccionService transaccionService;

    @BeforeEach
    void injectEntityManager() throws Exception {
        Field field = TransaccionService.class.getDeclaredField("entityManager");
        field.setAccessible(true);
        field.set(transaccionService, entityManager);
    }

    @Test
    void init() {
        assertNotNull(transaccionService);
    }

    @Test
    void depositar_ConCuentaExistente_RetornaTransaccionResponse() {
        // Arrange
        DepositoRequest request = new DepositoRequest("ACCT-001", new BigDecimal("500.00"), "Deposito test");

        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldo(new BigDecimal("1000.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-001")).thenReturn(cuenta);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:test");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(1L);

        // Act
        TransaccionResponse response = transaccionService.depositar(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(TipoTransaccion.DEPOSITO, response.tipo());
        assertEquals(new BigDecimal("500.00"), response.monto());
        assertEquals("Deposito test", response.descripcion());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void depositar_ConDescripcionNula_UsaDescripcionPorDefecto() {
        // Arrange
        DepositoRequest request = new DepositoRequest("ACCT-001", new BigDecimal("200.00"), null);

        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldo(new BigDecimal("0.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-001")).thenReturn(cuenta);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:test");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(2L);

        // Act
        TransaccionResponse response = transaccionService.depositar(request);

        // Assert
        assertEquals("Deposito a cuenta", response.descripcion());
    }

    @Test
    void depositar_ActualizaSaldoDeLaCuenta() {
        // Arrange
        DepositoRequest request = new DepositoRequest("ACCT-001", new BigDecimal("300.00"), "test");

        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldo(new BigDecimal("100.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-001")).thenReturn(cuenta);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:test");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(3L);

        // Act
        transaccionService.depositar(request);

        // Assert
        assertEquals(new BigDecimal("400.00"), cuenta.getSaldo());
    }

    @Test
    void transferir_CuentasDistintas_RealizaTransferenciaCorrectamente() {
        // Arrange
        TransferenciaRequest request = new TransferenciaRequest(
                "ACCT-ORIGEN", "ACCT-DESTINO", new BigDecimal("200.00"), "Pago");

        Cuenta origen = new Cuenta();
        origen.setId(1L);
        origen.setSaldo(new BigDecimal("500.00"));

        Cuenta destino = new Cuenta();
        destino.setId(2L);
        destino.setSaldo(new BigDecimal("100.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-ORIGEN")).thenReturn(origen);
        when(cuentaService.buscarPorNumero("ACCT-DESTINO")).thenReturn(destino);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:test");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(4L);

        // Act
        TransaccionResponse response = transaccionService.transferir(request);

        // Assert
        assertNotNull(response);
        assertEquals(TipoTransaccion.TRANSFERENCIA, response.tipo());
        assertEquals(new BigDecimal("200.00"), response.monto());
        assertEquals(new BigDecimal("300.00"), origen.getSaldo());
        assertEquals(new BigDecimal("300.00"), destino.getSaldo());
        verify(cuentaRepository).save(origen);
        verify(cuentaRepository).save(destino);
    }

    @Test
    void transferir_MismaCuenta_LanzaBusinessException() {
        // Arrange
        TransferenciaRequest request = new TransferenciaRequest(
                "ACCT-001", "ACCT-001", new BigDecimal("100.00"), null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transaccionService.transferir(request);
        });

        assertEquals("La cuenta origen y destino deben ser diferentes", exception.getMessage());
        verify(cuentaService, never()).buscarPorNumero(any());
    }

    @Test
    void transferir_SaldoInsuficiente_LanzaBusinessException() {
        // Arrange
        TransferenciaRequest request = new TransferenciaRequest(
                "ACCT-ORIGEN", "ACCT-DESTINO", new BigDecimal("1000.00"), null);

        Cuenta origen = new Cuenta();
        origen.setId(1L);
        origen.setSaldo(new BigDecimal("100.00"));

        Cuenta destino = new Cuenta();
        destino.setId(2L);
        destino.setSaldo(new BigDecimal("50.00"));

        when(cuentaService.buscarPorNumero("ACCT-ORIGEN")).thenReturn(origen);
        when(cuentaService.buscarPorNumero("ACCT-DESTINO")).thenReturn(destino);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            transaccionService.transferir(request);
        });

        assertEquals("Saldo insuficiente para realizar la transferencia", exception.getMessage());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void transferir_ConDescripcionNula_UsaDescripcionPorDefecto() {
        // Arrange
        TransferenciaRequest request = new TransferenciaRequest(
                "ACCT-ORIGEN", "ACCT-DESTINO", new BigDecimal("100.00"), null);

        Cuenta origen = new Cuenta();
        origen.setId(1L);
        origen.setSaldo(new BigDecimal("500.00"));

        Cuenta destino = new Cuenta();
        destino.setId(2L);
        destino.setSaldo(new BigDecimal("50.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-ORIGEN")).thenReturn(origen);
        when(cuentaService.buscarPorNumero("ACCT-DESTINO")).thenReturn(destino);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:test");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(5L);

        // Act
        TransaccionResponse response = transaccionService.transferir(request);

        // Assert
        assertEquals("Transferencia entre cuentas", response.descripcion());
    }

    @Test
    void depositar_ConUrlPostgres_UsaInsertPostgres() {
        // Arrange
        DepositoRequest request = new DepositoRequest("ACCT-002", new BigDecimal("150.00"), "Deposito PG");

        Cuenta cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setSaldo(new BigDecimal("100.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-002")).thenReturn(cuenta);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(7L);

        // Act
        var response = transaccionService.depositar(request);

        // Assert
        assertNotNull(response);
        assertEquals(7L, response.id());
        assertEquals(TipoTransaccion.DEPOSITO, response.tipo());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void depositar_ReturningNonNumber_ThrowsIllegalStateException() {
        // Arrange
        DepositoRequest request = new DepositoRequest("ACCT-003", new BigDecimal("50.00"), "Deposito BadId");

        Cuenta cuenta = new Cuenta();
        cuenta.setId(20L);
        cuenta.setSaldo(new BigDecimal("0.00"));

        Query queryMock = mock(Query.class);

        when(cuentaService.buscarPorNumero("ACCT-003")).thenReturn(cuenta);
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:test");
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyInt(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn("ID");

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            transaccionService.depositar(request);
        });

        assertEquals("RETURNING id no devolvió un número", ex.getMessage());
    }
}
