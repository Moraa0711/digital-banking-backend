package com.bancodigital.backend.cliente.application;

import com.bancodigital.backend.cliente.api.CrearClienteRequest;
import com.bancodigital.backend.cliente.domain.Cliente;
import com.bancodigital.backend.cliente.infrastructure.ClienteRepository;
import com.bancodigital.backend.cliente.infrastructure.TipoDocumentoRepository;
import com.bancodigital.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public Cliente crear(CrearClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento(tipoDocumentoRepository.findById(request.tipoDocumentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de documento no encontrado: " + request.tipoDocumentoId()
                )));
        cliente.setNumeroDocumento(request.numeroDocumento());
        cliente.setNombre(request.nombre());
        cliente.setApellidos(request.apellidos());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setFechaNacimiento(request.fechaNacimiento().atStartOfDay());
        cliente.setEdad(calcularEdad(request.fechaNacimiento()));
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    private Integer calcularEdad(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
