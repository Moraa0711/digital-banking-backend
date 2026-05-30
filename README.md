# Sistema de Banco Digital - Backend core

Este repositorio contiene el desarrollo del componente backend para una plataforma de Banca Digital, abarcando desde la gestión de clientes y productos financieros hasta la lógica transaccional avanzada consolidada en su fase final (Sprints 1, 2 y 3). 

El sistema está construido sobre **Java y Spring Boot**, utilizando **PostgreSQL** alojado en la infraestructura serverless de **Neon.tech** como motor de persistencia, y se encuentra documentado bajo el estándar de **Swagger UI**.

---

### Enfoque Profesional y Roles de Ingeniería

El proyecto se estructuró bajo una división clara de responsabilidades de ingeniería. Mi participación dentro del ciclo de vida del desarrollo se centró en los siguientes frentes estratégicos:

- **Análisis Funcional y Conceptual de Requerimientos:** Traducción de las necesidades del negocio financiero hacia Historias de Usuario (HU-1 a HU-8), modelando las restricciones técnicas, flujos lógicos y validaciones de saldos/estados en escenarios transaccionales de alta criticidad.
- **Aseguramiento de la Calidad (QA) y Control de Reglas de Negocio:** Diseño y ejecución de matrices de prueba para verificar la integridad de las transacciones (depósitos, transferencias, retiros con validación de fondos e historiales con filtros dinámicos), garantizando que el sistema mitigue comportamientos inesperados o inconsistencias en los saldos.

---

### Arquitectura del Sistema

La solución implementa una **Arquitectura Modular orientada por Dominios**, aplicando principios de desacoplamiento que facilitan una futura migración hacia un ecosistema de Microservicios. Los componentes core se dividen en:

- **`cliente` (HU-1, HU-2):** Gestión del ciclo de vida del usuario del banco, tipos de documentos e información de contacto.
- **`cuenta` (HU-3, HU-4):** Reglas de negocio para la apertura, parametrización y consulta de saldos en tiempo real para cuentas corrientes y de ahorros.
- **`transaccion` (HU-5 a HU-8):** Motor financiero encargado de procesar débitos, créditos, validación de fondos y el procesamiento del historial de movimientos mediante filtros avanzados.
- **`shared`:** Capa transversal para la centralización del manejo de excepciones de negocio (`GlobalExceptionHandler`) y estandarización de respuestas de la API.

---

### Catálogo de Endpoints de la API (Versionados)

Las APIs se exponen bajo un esquema de versionamiento semántico para garantizar la retrocompatibilidad del sistema durante la evolución del software:

| Módulo | Método | Endpoint | Descripción | Requerimiento Funcional |
| :--- | :--- | :--- | :--- | :--- |
| **Cliente** | POST | `/api/v2/clientes` | Registro de nuevos clientes en el sistema | HU-1: Registrar cliente |
| **Cliente** | GET | `/api/v2/clientes/{id}` | Consulta de información de cliente por ID interno | HU-2: Consultar cliente |
| **Cliente** | GET | `/api/v3/clientes?documento={num}` | Búsqueda indexada de cliente por documento de identidad | HU-2: Consultar cliente |
| **Cuenta** | POST | `/api/v1/cuentas` | Apertura y vinculación de cuentas bancarias | HU-3: Crear cuenta |
| **Cuenta** | GET | `/api/v1/cuentas/{numero}/saldo` | Consulta de saldo disponible en tiempo real | HU-4: Consultar saldo |
| **Transacción**| POST | `/api/v1/transacciones/transferencias`| Débito y crédito interbancario automatizado | HU-5: Transferir dinero |
| **Transacción**| POST | `/api/v1/transacciones/depositos` | Incremento de saldo por canales autorizados | HU-6: Depositar dinero |
| **Transacción**| POST | `/api/v1/transacciones/retiros` | Egreso de efectivo con validación previa de fondos | HU-7: Retirar dinero |
| **Transacción**| GET | `/api/v1/transacciones/historial` | Consulta del histórico de movimientos con filtros | HU-8: Historial transaccional|

---

### Configuración del Entorno de Persistencia (Neon.tech)

El sistema utiliza migraciones y persistencia administrada en un clúster de PostgreSQL serverless. Para realizar el despliegue local o en entornos de staging, se deben aprovisionar las siguientes variables de entorno en el sistema operativo:

```bash
export NEON_DB_URL="jdbc:postgresql://[host-de-neon]/[nombre-bd]"
export NEON_DB_USER="[usuario-asignado]"
export NEON_DB_PASSWORD="[credencial-de-acceso]"
