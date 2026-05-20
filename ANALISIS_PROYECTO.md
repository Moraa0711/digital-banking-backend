# Análisis técnico del proyecto `banco-digital-backend`

## Resumen ejecutivo

El proyecto es un backend **Spring Boot 3.2.4 + Java 21** orientado a un banco digital, organizado por dominios (`cliente`, `cuenta`, `transaccion`, `shared`) con una separación clara entre API, aplicación, dominio e infraestructura.  
Implementa las HU 1 a 6 con endpoints REST para creación/consulta de clientes, creación/consulta de cuentas, depósitos y transferencias.

## Stack y configuración

- Framework: Spring Boot (Web, Validation, Data JPA)
- Persistencia: PostgreSQL (Neon) + JPA/Hibernate
- Migraciones: Flyway (dependencia presente)
- Documentación API: Springdoc OpenAPI (Swagger UI)
- Testing: Spring Boot Test + H2 en memoria
- Build: Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Estructura del código

### 1) Módulo `cliente`

- `ClienteController`: expone:
  - `POST /api/clientes` (registro)
  - `GET /api/clientes/{id}` (consulta)
- `ClienteService`: crea cliente, calcula edad, resuelve tipo de documento.
- Entidades: `Cliente`, `TipoDocumento`, `Direccion`.
- Repositorios: `ClienteRepository`, `TipoDocumentoRepository`.

### 2) Módulo `cuenta`

- `CuentaController`: expone:
  - `POST /api/cuentas` (creación)
  - `GET /api/cuentas/{numeroCuenta}/saldo` (consulta de saldo)
- `CuentaService`:
  - valida existencia de cliente y tipo de cuenta
  - evita duplicidad por cliente+tipo de cuenta
  - genera número de cuenta con UUID parcial
- Entidades: `Cuenta`, `TipoCuentaEntity`, `EstadoCuenta`, `TipoCuenta`.
- Repositorios: `CuentaRepository`, `TipoCuentaRepository`.

### 3) Módulo `transaccion`

- `TransaccionController`: expone:
  - `POST /api/transacciones/depositos`
  - `POST /api/transacciones/transferencias`
- `TransaccionService`:
  - aplica reglas de saldo en transferencias
  - actualiza saldos origen/destino con transacción (`@Transactional`)
  - inserta transacción con SQL nativo y `RETURNING id`
- Entidades/DTOs: `Transaccion`, `TipoTransaccion`, requests/responses.

### 4) Módulo `shared`

- Manejo global de errores (`GlobalExceptionHandler`) con `ApiError` uniforme.
- Excepciones de negocio y de recurso no encontrado.

## Base de datos y migraciones

- `V1__init_schema.sql` crea tablas principales (`cliente`, `cuenta`, `transaccion`) e índices.
- `V2__align_with_erd.sql` agrega tablas catálogo (`tipo_documento`, `tipo_cuenta`, `direccion`) y columnas FK alineadas al ERD.

## Hallazgos clave (riesgos y puntos a corregir)

1. **Flyway está deshabilitado en runtime** (`spring.flyway.enabled=false`) aunque existen migraciones y dependencia; esto puede dejar el esquema fuera de sincronía respecto al código.

2. **Credenciales por defecto en `application.properties`** (incluyendo password) representan riesgo de seguridad y de exposición accidental de secretos.

3. **Desalineación entidad-esquema en `tipo_cuenta`**:
   - Entidad `TipoCuentaEntity` mapea columna `nombre`.
   - Migración `V2` define columna `tipo`.
   Esto puede romper lecturas/escrituras de ese catálogo.

4. **Potencial desalineación en `transaccion`**:
   - `TransaccionService` usa SQL nativo con cast a `tipo_transaccion_enum` para PostgreSQL.
   - Las migraciones vistas definen `tipo` como `VARCHAR` con `CHECK`, no como enum SQL.
   Puede fallar en inserción según el esquema real desplegado.

5. **Campo `referencia` en `transaccion` (V1) es `NOT NULL UNIQUE`**, pero:
   - la entidad `Transaccion` no lo mapea,
   - los inserts nativos de `TransaccionService` no lo envían.
   Si ese campo existe en producción, la operación de depósito/transferencia puede fallar.

6. **Cobertura de pruebas mínima**: solo existe `contextLoads`; no hay pruebas funcionales de reglas de negocio críticas (saldo insuficiente, duplicidad de cuenta, validaciones de entrada, etc.).

## Fortalezas del proyecto

- Arquitectura modular por dominio, fácil de escalar.
- Uso de validaciones declarativas (`jakarta.validation`).
- Manejo centralizado de errores consistente.
- Reglas de negocio importantes implementadas (no duplicar tipo de cuenta por cliente, control de saldo insuficiente).
- API documentable con OpenAPI.

## Recomendaciones priorizadas

1. **Alinear esquema y entidades primero** (columnas `tipo/nombre`, `referencia`, tipo de dato de `transaccion.tipo`).
2. **Habilitar y usar Flyway en todos los entornos** con un pipeline de migraciones controlado.
3. **Eliminar secretos por defecto del repositorio** y forzar variables de entorno/secret manager.
4. **Reemplazar SQL nativo frágil por persistencia JPA o SQL consistente con el esquema oficial**.
5. **Agregar pruebas de integración** para HU 1–6 y casos de error de negocio.

## Estado general

El proyecto tiene una base sólida y estructura profesional para un sprint académico/prototipo, pero actualmente presenta **inconsistencias críticas entre código y esquema** que conviene resolver antes de considerar despliegue estable.
