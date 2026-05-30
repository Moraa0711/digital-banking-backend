# Sistema de Banco Digital - Backend (Sprint 1, 2 y 3)

Backend en Spring Boot para cubrir HU 1 a HU 8 del proyecto de banco digital, usando PostgreSQL en Neon.

## Arquitectura

Se organizo en modulos por dominio para facilitar evolucion a microservicios:

- `cliente` (HU-1, HU-2)
- `cuenta` (HU-3, HU-4)
- `transaccion` (HU-5 a HU-8)
- `shared` (manejo de errores y respuestas comunes)

## Endpoints principales (versionados)

- `POST /api/v2/clientes` -> HU-1 Registrar cliente
- `GET /api/v2/clientes/{id}` -> HU-2 Consultar cliente por id
- `GET /api/v3/clientes?documento={numero}` -> HU-2 Consultar cliente por numero de documento
- `POST /api/v1/cuentas` -> HU-3 Crear cuenta bancaria
- `GET /api/v1/cuentas/{numeroCuenta}/saldo` -> HU-4 Consultar saldo
- `POST /api/v1/transacciones/transferencias` -> HU-5 Transferir dinero
- `POST /api/v1/transacciones/depositos` -> HU-6 Depositar dinero
- `POST /api/v1/transacciones/retiros` -> HU-7 Retirar dinero
- `GET /api/v1/transacciones/historial` -> HU-8 Historial de transacciones

## Configuracion de Neon

Definir variables de entorno:

- `NEON_DB_URL`
- `NEON_DB_USER`
- `NEON_DB_PASSWORD`

Luego ejecutar:

```bash
./mvnw spring-boot:run
```

## CI/CD con GitHub Actions

Se agregaron estos workflows:

- `CI` (`.github/workflows/ci.yml`): compila, ejecuta pruebas y publica el JAR como artefacto.
- `CD` (`.github/workflows/cd.yml`): crea release en GitHub y adjunta el JAR al publicar un tag `v*.*.*`.
- `Sonar Static Analysis` (`.github/workflows/sonar.yml`): ejecuta análisis estático con Sonar.

## Configuración de Sonar

Configurar en el repositorio:

- **Secrets**
  - `SONAR_TOKEN`
- **Variables**
  - `SONAR_PROJECT_KEY`
  - `SONAR_ORGANIZATION` (requerido en SonarCloud)
  - `SONAR_HOST_URL` (opcional, por defecto `https://sonarcloud.io`)

El archivo `sonar-project.properties` contiene la configuración base del análisis.
