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
import jakarta.persistence.PersistenceContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class TransaccionService {

    private static final String INSERT_TRANSACCION_H2 = """
            INSERT INTO transaccion (id_cuenta_origen, id_cuenta_destino, tipo, monto, fecha, descripcion)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    private static final String INSERT_TRANSACCION_POSTGRES = """
            INSERT INTO transaccion (id_cuenta_origen, id_cuenta_destino, tipo, monto, fecha, descripcion)
            VALUES (?, ?, CAST(? AS tipo_transaccion_enum), ?, ?, ?)
            RETURNING id
            """;

    private final CuentaService cuentaService;
    private final CuentaRepository cuentaRepository;
    private final Environment environment;

    @PersistenceContext
    private EntityManager entityManager;

    public TransaccionService(CuentaService cuentaService, CuentaRepository cuentaRepository, Environment environment) {
        this.cuentaService = cuentaService;
        this.cuentaRepository = cuentaRepository;
        this.environment = environment;
    }

    @Transactional
    public TransaccionResponse depositar(DepositoRequest request) {
        Cuenta cuenta = cuentaService.buscarPorNumero(request.numeroCuenta());
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
    public TransaccionResponse transferir(TransferenciaRequest request) {
        if (request.numeroCuentaOrigen().equals(request.numeroCuentaDestino())) {
            throw new BusinessException("La cuenta origen y destino deben ser diferentes");
        }

        Cuenta origen = cuentaService.buscarPorNumero(request.numeroCuentaOrigen());
        Cuenta destino = cuentaService.buscarPorNumero(request.numeroCuentaDestino());

        validarSaldo(origen.getSaldo(), request.monto());
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

    private void validarSaldo(BigDecimal saldoActual, BigDecimal monto) {
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
        String sql = isH2() ? INSERT_TRANSACCION_H2 : INSERT_TRANSACCION_POSTGRES;
        Object idResult = entityManager
                .createNativeQuery(sql)
                .setParameter(1, idCuentaOrigen)
                .setParameter(2, idCuentaDestino)
                .setParameter(3, tipo.name())
                .setParameter(4, monto)
                .setParameter(5, Timestamp.valueOf(fecha))
                .setParameter(6, descripcion)
                .getSingleResult();
        if (idResult instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalStateException("RETURNING id no devolvió un número");
    }

    private boolean isH2() {
        String url = environment.getProperty("spring.datasource.url", "");
        return url.contains("jdbc:h2:");
    }
}
