# Sustentación final integrada

**Proyecto:** Semestre Saludable UPeU
**Equipo 01:** Montalvo Machaca, Maykol Gabriel · Cruz Mendoza, Deysi
**Cursos:** Análisis y Diseño de Software · Administración de Base de Datos II · Lenguaje de Programación II
**Sede:** UPeU Juliaca — Semestre 2026-2

Esta página reúne lo que se presenta en la sustentación final del Proyecto Integrador: un solo sistema visto desde los tres cursos. En ADS se diseñó, en LP2 se construyó y en BD2 se opera sobre Oracle.

## 1. Pitch

| | |
|---|---|
| **Problema** | La universidad promueve hábitos saludables, pero participan muy pocos estudiantes. Las evidencias llegan por Instagram y WhatsApp, un encargado las revisa a mano unos 30 minutos por jornada y no queda ningún dato para medir si alguien cambió. |
| **Solución** | Un sistema web donde el estudiante registra su hábito con foto en tres pasos, la coordinación de su sede valida cada evidencia y la jefatura premia con el ranking. |
| **Valor** | La jefatura sabe en tiempo real cuántos estudiantes participan, la revisión deja datos y cada premio tiene un respaldo verificable. |

El sistema se validó con la Directora de Universidad Saludable en una reunión por Microsoft Teams el 8 de noviembre de 2026.

## 2. Secuencia de la sustentación

| Momento | Tiempo | Qué se muestra |
|---|---:|---|
| Pitch ejecutivo | 1 a 3 min | Problema, solución y valor, con el video del sistema |
| Diseño ADS | 3 min | Arquitectura C4, UML, patrones y ADR |
| Operación BD2 | 3 min | Modelo de datos, seguridad, optimización, respaldo y recuperación |
| Aplicación LP2 | 5 min | Backend, SPA, Keycloak, optimización, caché, observabilidad, paginación y auditoría |
| Demo final | 5 min | Flujo completo autenticado, de la pantalla a Oracle |
| Preguntas individuales | 5 min por integrante | Aporte y defensa técnica de cada uno |

## 3. Diseño (ADS)

El sistema es un **monolito modular**: una sola aplicación Spring Boot con seis módulos de Spring Modulith. Cada módulo repite las mismas capas (Controller, Service, Repository, DTO y Mapper) y solo se comunica con los demás por sus servicios públicos.

| Vista | Dónde verla |
|---|---|
| C4 nivel 1 y 2 (contexto y contenedores) | [Lucidchart, archivo 1](https://lucid.app/lucidchart/d50d9dce-97e1-4457-8617-6b3bb0b9cbbc/view) |
| C4 nivel 3 (general y por módulo) y despliegue | [Lucidchart, archivo 2](https://lucid.app/lucidchart/ae3b2e35-040a-4b09-a8a0-7bad5a62c8ba/view) |
| Dominio, UML, patrones e integración (6 sesiones) | [Tablero Miro](https://miro.com/app/board/uXjVHpU7nYw=/) |

### Decisiones de arquitectura

| ADR | Decisión | Consecuencia aceptada |
|---|---|---|
| ADR-01 | Monolito modular con Spring Modulith | Un solo despliegue; un módulo no puede escalar por separado |
| ADR-02 | Cuatro esquemas Oracle y referencias por ID | No hay FK entre esquemas; el detalle copia nombre y puntaje del hábito |
| ADR-03 | Keycloak como proveedor de identidad | Estándar OIDC con PKCE, a cambio de un contenedor más |
| ADR-04 | Registro y cupo en una sola transacción | Rollback completo ante cualquier error; sin bloqueo pesimista, dos envíos simultáneos podrían pasar el tope |
| ADR-05 | Fotos en disco y no en la base | El respaldo debe incluir el volumen de evidencias |
| ADR-06 | Caché HTTP con ETag; Redis descartado | Se reconsidera con cientos de lecturas por minuto |
| ADR-07 | Auditoría inmutable en transacción aparte | Si la auditoría falla, la operación principal no se pierde |
| ADR-08 | Cuentas desde solicitudes de Odoo CRM | Depende de que Odoo responda |

## 4. Operación (BD2)

La operación se trabajó en dos lugares, sobre el mismo dominio:

- **Base integrada del sistema:** Oracle 23ai Free en Docker, la que usa el backend. Su modelo completo está en [Modelo de datos](modelo-datos.md).
- **Laboratorio del curso de BD2:** Oracle 21c XE con SQL Developer, en las máquinas de la universidad, con el esquema `SALUDABLE`. Ahí se practicaron particiones, respaldo, recuperación y PL/SQL.

### Base integrada (Oracle 23ai)

| Aspecto | Evidencia |
|---|---|
| Modelo | 16 tablas en cuatro esquemas (`BOM_CAMPANIAS`, `BOM_PARTICIPACION`, `BOM_PREMIOS` y `BOM_AUDITORIA`), llaves primarias `IDENTITY`, llaves foráneas dentro de cada esquema y referencias por ID entre esquemas (ADR-02) |
| Integridad | `CHECK` en cantidades, puntajes, estados y sedes; `UK_CUPO_DIA` única por campaña, estudiante y fecha; `S20` agrega las dos llaves foráneas que faltaban (`FK_REGISTRO_ESTUDIANTE` y `FK_DETALLE_PREMIO`) |
| Seguridad | La aplicación entra como `BOMERP_APP`, que solo tiene `CREATE SESSION` y permisos por tabla; no es dueña de ningún esquema |
| Auditoría inmutable | Sobre `BOM_AUDITORIA.EVENTOS` la aplicación solo tiene `SELECT` e `INSERT`, y el trigger `TRG_EVENTOS_INMUTABLE` rechaza cualquier `UPDATE` o `DELETE` con `ORA-20010` |
| PL/SQL | Funciones `OBTENER_PUNTAJE_ESTUDIANTE` y `HABITO_MAS_REGISTRADO` con manejo de excepciones; rol `ROL_REPORTES` de solo lectura |
| Particionamiento | `REGISTROS` por rango de `FECHA` con `INTERVAL` mensual; Oracle crea sola la partición de cada mes |
| Optimización | Estadísticas con `DBMS_STATS` y plan con `EXPLAIN PLAN`; índices `IX_REGISTRO_SEDE (SEDE, ESTADO, FECHA)`, `IX_REGISTRO_ESTUDIANTE`, `IX_EVIDENCIA_REGISTRO`, `IX_DETALLE_ACTA`, `IX_DETALLE_PREMIO`, `IX_EVENTOS_FECHA`, `IX_EVENTOS_ACCION` e `IX_SOLICITUD_ESTADO`; totales del reporte calculados en Oracle con `COUNT`, `SUM` y `GROUP BY` |
| Monitoreo | Endpoint de salud de Actuator y `TRACE_ID` de cada petición guardado en `EVENTOS` |

### Laboratorio del curso (Oracle 21c XE)

| Aspecto | Evidencia |
|---|---|
| Particiones | Por rango de año (`P2025`, `P2026`, `P_FUTURO`) y por lista de sede (`P_JULIACA`, `P_LIMA`, `P_TARAPOTO`, `P_OTRAS`) |
| Respaldo y recuperación | Respaldo lógico con `exp` e `imp`, comprobado con `MINUS`; validación de la restauración con RMAN |
| PL/SQL | Procedimientos `REGISTRAR_PARTICIPACION` y `ANULAR_REGISTRO`, funciones `OBTENER_PUNTAJE_ESTUDIANTE` y `HABITO_MAS_REGISTRADO`, disparadores sobre el detalle y el registro, y un log con `AUTONOMOUS_TRANSACTION` |

Los scripts de la base integrada están en la carpeta `sql/` del repositorio y se ejecutan en el orden de la [Guía de ejecución](../../guia-ejecucion.md).

## 5. Aplicación (LP2)

El detalle completo está en el [Producto de Unidad 3](lp2-producto.md): componentes finales, rendimiento, caché, decisión sobre Redis, logging, auditoría, plan de pruebas y bitácora de hallazgos.

## 6. Demo final

| Paso | Rol | Qué se muestra |
|---:|---|---|
| 1 | Estudiante | Inicia sesión por Keycloak y ve su cupo del día |
| 2 | Estudiante | Registra dos hábitos con foto |
| 3 | Estudiante | Intenta pasarse del cupo. La pantalla ya no lo deja; con una segunda pestaña abierta antes, el servidor responde 409 y no guarda nada |
| 4 | Coordinación | Valida una evidencia y rechaza otra con motivo |
| 5 | Coordinación | El ranking suma solo los puntos validados |
| 6 | Coordinación | Emite un acta de premiación y el stock del premio baja |
| 7 | Jefatura | El reporte cambia de página y los totales no cambian |
| 8 | Jefatura | La auditoría muestra cada paso anterior con usuario y fecha |
| 9 | Base de datos | Una consulta a `REGISTROS` y a `EVENTOS` confirma que los datos están en Oracle |

## 7. Matriz final de trazabilidad

| Necesidad | Diseño (ADS) | Base de datos (BD2) | Código (LP2) | Pruebas |
|---|---|---|---|---|
| Registrar la participación | Agregado `RegistroParticipacion`, secuencia de CU-04 | `REGISTROS`, `DETALLE_EVIDENCIAS`, `CUPOS_DIARIOS` | `RegistroServiceImpl.crear()` | Cupo dentro y fuera del límite, hábito ajeno a la campaña, `ParticipacionEndToEndTest` |
| Validar evidencias | Diagrama de estados del registro | `ESTADO` y `MOTIVO_RECHAZO` en `DETALLE_EVIDENCIAS` | `validar()` y `rechazar()` | Rechazo con motivo; validar un registro ya revisado |
| Saber cuántos participan | C4 contenedores | `COUNT`, `SUM` y `GROUP BY` | `/registros/pagina` y `/registros/totales` | Totales iguales en cualquier página |
| Premiar con datos | Agregado `ActaPremiacion`, ADR-04 | `ACTAS_PREMIACION`, `DETALLES_PREMIACION`, stock de `PREMIOS` | `ActaPremiacionService.emitir()` | Acta con stock insuficiente; anular devuelve el stock |
| Solo usuarios reales | ADR-03 y ADR-08 | `ESTUDIANTES` enlazados a Keycloak | `SecurityConfig`, módulo `cuentas` | Sin token 401; rol incorrecto 403 |
| Control de lo que pasa | ADR-07 | `BOM_AUDITORIA.EVENTOS` | `AuditoriaService` | Acciones críticas registradas con actor y fecha |

## 8. Evidencia obligatoria

| Evidencia | Dónde está |
|---|---|
| Repositorios | [bomerp-backend](https://github.com/MGMM7STA/bomerp-backend) y [bomerp-frontend](https://github.com/MGMM7STA/bomerp-frontend) |
| Documentación publicada | Este sitio |
| Demo ejecutable | [Guía de ejecución](../../guia-ejecucion.md) |
| Matriz final de trazabilidad | Sección 7 de esta página |
| Pruebas y bitácora de estabilización | [Producto de Unidad 3](lp2-producto.md), secciones 3 y 4 |
| Evidencias Oracle de operación | Sección 4 de esta página y [Modelo de datos](modelo-datos.md) |
| Build optimizado, caché y decisión sobre Redis | [Producto de Unidad 3](lp2-producto.md), sección 2 |
| Logs, endpoint de salud y monitoreo | [Producto de Unidad 3](lp2-producto.md), sección 2.5 |
| Paginación, auditoría y prueba de punta a punta | [Producto de Unidad 3](lp2-producto.md), secciones 2.2, 2.6 y 3.1 |
| Guía reproducible de configuración y despliegue | [Guía de ejecución](../../guia-ejecucion.md) |
