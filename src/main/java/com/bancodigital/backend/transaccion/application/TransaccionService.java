package com.bancodigital.backend.transaccion.application;

import com.bancodigital.backend.cuenta.application.CuentaService;
import com.bancodigital.backend.cuenta.domain.Cuenta;
import com.bancodigital.backend.cuenta.infrastructure.CuentaRepository;
import com.bancodigital.backend.shared.exception.BusinessException;
import com.bancodigital.backend.shared.exception.ForbiddenException;
import com.bancodigital.backend.transaccion.api.*;
import com.bancodigital.backend.transaccion.domain.TipoTransaccion;
import com.bancodigital.backend.transaccion.domain.Transaccion;
import com.bancodigital.backend.transaccion.infrastructure.TransaccionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransaccionService {

    private static final String MENSAJE_SIN_MOVIMIENTOS =
            "Aun no hay movimientos registrados en su cuenta";

    private static final String INSERT_TRANSACCION = """
            INSERT INTO transaccion (id_cuenta_origen, id_cuenta_destino, id_tipo_transaccion, monto, fecha, descripcion)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id_transaccion
            """;

    private final CuentaService cuentaService;
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TransaccionService(
            CuentaService cuentaService,
            CuentaRepository cuentaRepository,
            TransaccionRepository transaccionRepository) {
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional
    public TransaccionResponse depositar(DepositoRequest request) {
        Cuenta cuenta = cuentaService.buscarPorNumero(request.numeroCuenta());
        cuentaService.validarCuentaParaMovimientos(cuenta);
        cuenta.setSaldo(cuenta.getSaldo().add(request.monto()));
        cuentaRepository.save(cuenta);

        String descripcion = request.descripcion() == null ? "Deposito a cuenta" : request.descripcion();
        LocalDateTime fecha = LocalDateTime.now();
        Long id = insertTransaccionRow(
                cuenta.getId(),
                null,
                TipoTransaccion.DEPOSITO,
                request.monto(),
                fecha,
                descripcion);
        return toResponse(id, TipoTransaccion.DEPOSITO, request.monto(), fecha, descripcion);
    }

    @Transactional
    public RetiroResponse retirar(RetiroRequest request) {
        Cuenta cuenta = cuentaService.buscarPorNumero(request.numeroCuenta());
        cuentaService.validarCuentaParaMovimientos(cuenta);
        validarSaldoRetiro(cuenta.getSaldo(), request.monto());

        cuenta.setSaldo(cuenta.getSaldo().subtract(request.monto()));
        cuentaRepository.save(cuenta);

        String descripcion = request.descripcion() == null ? "Retiro de cuenta" : request.descripcion();
        LocalDateTime fecha = LocalDateTime.now();
        Long id = insertTransaccionRow(
                cuenta.getId(),
                null,
                TipoTransaccion.RETIRO,
                request.monto(),
                fecha,
                descripcion);

        return new RetiroResponse(
                id,
                TipoTransaccion.RETIRO,
                request.monto(),
                fecha,
                descripcion,
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo());
    }

    @Transactional
    public TransaccionResponse transferir(TransferenciaRequest request) {
        if (request.numeroCuentaOrigen().equals(request.numeroCuentaDestino())) {
            throw new BusinessException("La cuenta origen y destino deben ser diferentes");
        }

        Cuenta origen = cuentaService.buscarPorNumero(request.numeroCuentaOrigen());
        Cuenta destino = cuentaService.buscarPorNumero(request.numeroCuentaDestino());
        cuentaService.validarCuentaParaMovimientos(origen);
        cuentaService.validarCuentaParaMovimientos(destino);

        validarSaldoTransferencia(origen.getSaldo(), request.monto());
        origen.setSaldo(origen.getSaldo().subtract(request.monto()));
        destino.setSaldo(destino.getSaldo().add(request.monto()));
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        String descripcion = request.descripcion() == null ? "Transferencia entre cuentas" : request.descripcion();
        LocalDateTime fecha = LocalDateTime.now();
        Long id = insertTransaccionRow(
                origen.getId(),
                destino.getId(),
                TipoTransaccion.TRANSFERENCIA,
                request.monto(),
                fecha,
                descripcion);
        return toResponse(id, TipoTransaccion.TRANSFERENCIA, request.monto(), fecha, descripcion);
    }

    @Transactional(readOnly = true)
    public HistorialResponse consultarHistorial(
            String numeroCuenta,
            Long clienteId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            TipoTransaccion tipo) {
        Cuenta cuenta = cuentaService.buscarPorNumero(numeroCuenta);
        validarPropietarioCuenta(cuenta, clienteId);

        List<Transaccion> movimientos = transaccionRepository.findMovimientosByCuentaIdAsc(cuenta.getId());

        if (movimientos.isEmpty()) {
            return new HistorialResponse(List.of(), MENSAJE_SIN_MOVIMIENTOS);
        }

        Map<Long, BigDecimal> saldosPorMovimiento = calcularSaldosResultantes(movimientos, cuenta.getId());
        List<Transaccion> filtrados = aplicarFiltros(movimientos, fechaInicio, fechaFin, tipo);

        if (filtrados.isEmpty()) {
            return new HistorialResponse(List.of(), null);
        }

        List<HistorialMovimientoResponse> items = filtrados.stream()
                .sorted(Comparator.comparing(Transaccion::getFecha).reversed()
                        .thenComparing(Transaccion::getId, Comparator.reverseOrder()))
                .map(tx -> new HistorialMovimientoResponse(
                        tx.getId(),
                        tx.getFecha(),
                        tx.getTipo(),
                        tx.getMonto(),
                        saldosPorMovimiento.get(tx.getId()),
                        tx.getDescripcion()))
                .toList();

        return new HistorialResponse(items, null);
    }

    private void validarPropietarioCuenta(Cuenta cuenta, Long clienteId) {
        if (clienteId == null) {
            throw new BusinessException("El identificador del cliente es obligatorio");
        }
        if (cuenta.getCliente() == null || !clienteId.equals(cuenta.getCliente().getId())) {
            throw new ForbiddenException("No tiene permiso para ver los movimientos de esa cuenta");
        }
    }

    private List<Transaccion> aplicarFiltros(
            List<Transaccion> movimientos,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            TipoTransaccion tipo) {
        LocalDateTime desde = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime hasta = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : null;

        return movimientos.stream()
                .filter(tx -> tipo == null || tx.getTipo() == tipo)
                .filter(tx -> desde == null || !tx.getFecha().isBefore(desde))
                .filter(tx -> hasta == null || !tx.getFecha().isAfter(hasta))
                .toList();
    }

    private Map<Long, BigDecimal> calcularSaldosResultantes(List<Transaccion> movimientos, Long cuentaId) {
        List<Transaccion> cronologico = movimientos.stream()
                .sorted(Comparator.comparing(Transaccion::getFecha).thenComparing(Transaccion::getId))
                .toList();

        Map<Long, BigDecimal> saldos = new HashMap<>();
        BigDecimal saldo = BigDecimal.ZERO;
        for (Transaccion tx : cronologico) {
            saldo = aplicarMovimiento(tx, cuentaId, saldo);
            saldos.put(tx.getId(), saldo);
        }
        return saldos;
    }

    private BigDecimal aplicarMovimiento(Transaccion tx, Long cuentaId, BigDecimal saldo) {
        return switch (tx.getTipo()) {
            case DEPOSITO -> saldo.add(tx.getMonto());
            case RETIRO -> saldo.subtract(tx.getMonto());
            case TRANSFERENCIA -> {
                BigDecimal result = saldo;
                if (tx.getCuentaOrigen() != null && cuentaId.equals(tx.getCuentaOrigen().getId())) {
                    result = result.subtract(tx.getMonto());
                }
                if (tx.getCuentaDestino() != null && cuentaId.equals(tx.getCuentaDestino().getId())) {
                    result = result.add(tx.getMonto());
                }
                yield result;
            }
        };
    }

    private void validarSaldoRetiro(BigDecimal saldoActual, BigDecimal monto) {
        if (saldoActual.compareTo(monto) < 0) {
            throw new BusinessException("No cuenta con saldo suficiente para esa operacion");
        }
    }

    private void validarSaldoTransferencia(BigDecimal saldoActual, BigDecimal monto) {
        if (saldoActual.compareTo(monto) < 0) {
            throw new BusinessException("Saldo insuficiente para realizar la transferencia");
        }
    }

    private TransaccionResponse toResponse(
            Long id, TipoTransaccion tipo, BigDecimal monto, LocalDateTime fecha, String descripcion) {
        return new TransaccionResponse(id, tipo, monto, fecha, descripcion);
    }

    private Long insertTransaccionRow(
            Long idCuentaOrigen,
            Long idCuentaDestino,
            TipoTransaccion tipo,
            BigDecimal monto,
            LocalDateTime fecha,
            String descripcion) {
        Object idResult = entityManager
                .createNativeQuery(INSERT_TRANSACCION)
                .setParameter(1, idCuentaOrigen)
                .setParameter(2, idCuentaDestino)
                .setParameter(3, tipo.getId())
                .setParameter(4, monto)
                .setParameter(5, Timestamp.valueOf(fecha))
                .setParameter(6, descripcion)
                .getSingleResult();
        if (idResult instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalStateException("RETURNING id no devolvio un numero");
    }
}
