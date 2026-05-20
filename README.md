# Sistema de Banco Digital - Backend (Sprint 1 y 2)

Backend en Spring Boot para cubrir HU 1 a HU 6 del proyecto de banco digital, usando PostgreSQL en Neon.

## Arquitectura

Se organizo en modulos por dominio para facilitar evolucion a microservicios:

- `cliente` (HU-1, HU-2)
- `cuenta` (HU-3, HU-4)
- `transaccion` (HU-5, HU-6)
- `shared` (manejo de errores y respuestas comunes)

## Endpoints principales

- `POST /api/clientes` -> HU-1 Registrar cliente
- `GET /api/clientes/{id}` -> HU-2 Consultar cliente
- `POST /api/cuentas` -> HU-3 Crear cuenta bancaria
- `GET /api/cuentas/{numeroCuenta}/saldo` -> HU-4 Consultar saldo
- `POST /api/transacciones/transferencias` -> HU-5 Transferir dinero
- `POST /api/transacciones/depositos` -> HU-6 Depositar dinero

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
