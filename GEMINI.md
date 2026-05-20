# Estado Minucioso del Proyecto y Pruebas (GEMINI.md)

Este documento detalla el estado actual de la cobertura de código tras revertir el refactor en el `GlobalExceptionHandler` y corregir los tests de la capa de dominio. 

## 📊 Resumen de Cobertura Global

De acuerdo con el último reporte de JaCoCo, el proyecto cuenta con un total de **28 clases**. La cobertura global en líneas es **excepcionalmente alta (aproximadamente 98%)**, con **88 pruebas pasando**. Hemos cubierto por completo la capa de servicios, todos los DTOs y casi toda la capa de dominio.

El 21% faltante reportado en SonarCloud se debe a ramas condicionales ("branches") que no se ejecutan y unas cuantas instrucciones puntuales.

---

## 🟢 Componentes con Cobertura Total (100%)

Las siguientes clases no tienen ni una sola línea o instrucción faltante:

1. **Servicios**: `CuentaService`, `ClienteService`
2. **Entidades**: `Cuenta`, `Cliente`, `Transaccion`, `Direccion`, `TipoDocumento`, `TipoCuentaEntity`
3. **Enums**: `EstadoCuenta`, `TipoCuenta`, `TipoTransaccion`
4. **DTOs (Requests/Responses)**: Todos (`CrearClienteRequest`, `DepositoRequest`, `CuentaResponse`, etc.)
5. **Controladores**: `TransaccionController`, `CuentaController`
6. **Excepciones**: `BusinessException`, `ResourceNotFoundException`, `ApiError`

---

## 🔴 Brechas Restantes (Lo que falta para el 100%)

A continuación, el detalle exacto de las **4 únicas clases** que impiden llegar al 100% de cobertura.

### 1. `GlobalExceptionHandler` (Faltan 8 Ramas - Impacto Alto en SonarCloud)
* **Situación:** En las líneas 44, 51 y 59 se utiliza un operador ternario: `ex.getMostSpecificCause() != null ? ... : ...`. 
* **El Problema:** Al revertir los cambios anteriores, el código volvió a tener este operador. Como el método `getMostSpecificCause()` en Spring nunca devuelve `null`, la rama `== null` nunca se ejecuta en ninguna prueba, dejando 8 "ramas perdidas" en el reporte de JaCoCo.
* **Solución para 100%:** Eliminar los operadores ternarios y usar directamente `ex.getMostSpecificCause().getMessage()`. Esto resolverá tanto los bugs de SonarCloud como la cobertura de ramas de JaCoCo.

### 2. `TransaccionService` (Falta 1 Línea y 2 Ramas)
* **Situación:** En el método `insertTransaccionRow`, se verifica si la base de datos es H2 mediante el método `isH2()`.
* **El Problema:** Como nuestras pruebas siempre usan H2 (`jdbc:h2:mem:test`), la rama que ejecuta el query de PostgreSQL `INSERT_TRANSACCION_POSTGRES` nunca se prueba. Además, la línea `throw new IllegalStateException("RETURNING id no devolvió un número")` nunca se alcanza porque el mock siempre devuelve un número.
* **Solución para 100%:** Añadir dos pruebas unitarias adicionales en `TransaccionServiceTest`:
  1. Forzar que el `Environment` devuelva una URL de Postgres para cubrir la rama de PostgreSQL.
  2. Forzar que el `EntityManager` retorne un String (ej: "ID") para forzar la `IllegalStateException`.

### 3. `BackendApplication` (Faltan 2 Líneas)
* **Situación:** El método `main` no se ejecuta en las pruebas.
* **El Problema:** Nuestra prueba `BackendApplicationMainTest` solo verifica por Reflection que el método existe, pero no ejecuta `SpringApplication.run`.
* **Solución para 100%:** Cambiar la prueba para que ejecute el método `main()` en un entorno de pruebas apagando el servidor web, o simplemente ignorar esta clase en la configuración de JaCoCo (práctica común en la industria).

### 4. `ClienteController` (Falta 1 Rama)
* **Situación:** El controlador tiene 16 líneas cubiertas (100% en líneas) pero marca 1 rama faltante y 4 instrucciones.
* **El Problema:** El método `toResponse` tiene ternarios como `cliente.getTipoDocumento() != null ? ...`. Una de estas ramas nunca se está evaluando porque no hay una prueba explícita que devuelva un cliente sin TipoDocumento.
* **Solución para 100%:** Añadir una prueba en `ClienteControllerTest` donde el cliente retornado no tenga `TipoDocumento` (null) para cubrir la rama falsa del ternario.

---

## 🚀 Plan de Acción Recomendado

Para recuperar y subir el *coverage* en SonarCloud:

1. **Aplicar la corrección a `GlobalExceptionHandler`**: Es la corrección más fácil que aumentará enormemente la métrica de *branches covered*.
2. **Agregar los Edge-cases en `TransaccionServiceTest`**.
3. **Agregar el Edge-case en `ClienteControllerTest`**.
4. **Ignorar `BackendApplication` en `pom.xml`**: Añadiendo una regla de exclusión en el plugin de JaCoCo.
