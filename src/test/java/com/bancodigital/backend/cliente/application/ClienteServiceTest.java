package com.bancodigital.backend.cliente.application;

import com.bancodigital.backend.cliente.api.CrearClienteRequest;
import com.bancodigital.backend.cliente.domain.Cliente;
import com.bancodigital.backend.cliente.domain.TipoDocumento;
import com.bancodigital.backend.cliente.infrastructure.ClienteRepository;
import com.bancodigital.backend.cliente.infrastructure.TipoDocumentoRepository;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void crear_ConTipoDocumentoExistente_GuardaYDevuelveCliente() {
        // Arrange
        CrearClienteRequest request = new CrearClienteRequest(
                1, "12345678", "Juan", "Perez", "juan@test.com", "555-1234",
                LocalDate.now().minusYears(25));

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(1);
        tipoDocumento.setTipo("CC");

        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setId(1L);
        clienteGuardado.setNombre("Juan");
        clienteGuardado.setEdad(25);

        when(tipoDocumentoRepository.findById(1)).thenReturn(Optional.of(tipoDocumento));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        // Act
        Cliente resultado = clienteService.crear(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        assertEquals(25, resultado.getEdad());

        verify(tipoDocumentoRepository).findById(1);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void crear_ConTipoDocumentoInexistente_LanzaResourceNotFoundException() {
        // Arrange
        CrearClienteRequest request = new CrearClienteRequest(
                99, "12345678", "Juan", "Perez", "juan@test.com", "555-1234",
                LocalDate.of(1990, 1, 1));

        when(tipoDocumentoRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.crear(request);
        });

        assertEquals("Tipo de documento no encontrado: 99", exception.getMessage());
        verify(tipoDocumentoRepository).findById(99);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void calcularEdad_AsignaEdadCorrecta() {
        // Arrange
        LocalDate fechaNacimiento = LocalDate.now().minusYears(30).minusMonths(1);
        CrearClienteRequest request = new CrearClienteRequest(
                1, "12345678", "Juan", "Perez", "juan@test.com", "555-1234",
                fechaNacimiento);

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(1);

        when(tipoDocumentoRepository.findById(1)).thenReturn(Optional.of(tipoDocumento));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });

        // Act
        Cliente resultado = clienteService.crear(request);

        // Assert
        assertEquals(30, resultado.getEdad());
    }

    @Test
    void buscarPorId_ConIdExistente_DevuelveCliente() {
        // Arrange
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        Cliente resultado = clienteService.buscarPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_ConIdInexistente_LanzaResourceNotFoundException() {
        // Arrange
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clienteService.buscarPorId(99L));
    }
}
