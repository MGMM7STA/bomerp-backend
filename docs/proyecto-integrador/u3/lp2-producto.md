# LP2 - Producto final (Unidad 3)

**Proyecto:** Semestre Saludable UPeU
**Equipo 01:** Montalvo Machaca, Maykol Gabriel · Cruz Mendoza, Deysi
**Curso:** Lenguaje de Programación II — UPeU sede Juliaca — Semestre 2026-2
**Repositorios:** [bomerp-backend](https://github.com/MGMM7STA/bomerp-backend) · [bomerp-frontend](https://github.com/MGMM7STA/bomerp-frontend)

## Producto

Sistema web para la campaña **Semestre Saludable**: el estudiante registra evidencias diarias de hábitos saludables con foto, la coordinación de su sede las valida o rechaza, el puntaje validado arma el ranking y, al cierre, se emite un acta de premiación que descuenta el stock de los premios entregados.

Backend Spring Boot 4 como monolito modular (Spring Modulith) sobre Oracle 23ai, con seguridad JWT de Keycloak. Frontend Angular 22 con componentes standalone, signals y carga diferida por ruta. Cada sede (Juliaca, Lima, Tarapoto) trabaja con sus propios datos.

## 1. Componentes finales

| Módulo | Esquema Oracle | Responsabilidad |
|---|---|---|
| `campanias` | `BOM_CAMPANIAS` | Categorías de hábito, hábitos, campañas, sedes y hábitos de cada campaña, cupo diario |
| `participacion` | `BOM_PARTICIPACION` | Estudiantes, registro de participación con sus evidencias, validación, reporte y ranking |
| `premios` | `BOM_PREMIOS` | Categorías de premio, premios con stock y actas de premiación |
| `cuentas` | Keycloak + `BOM_PARTICIPACION.SOLICITUDES_CUENTA` | Cuentas y roles en Keycloak, solicitudes de cuenta |
| `sedes` | `BOM_PARTICIPACION.SEDES_USUARIO` | Sede de cada usuario y alcance de las consultas |
| `auditoria` | `BOM_AUDITORIA` | Registro inmutable de las acciones importantes |

`catalogo` y `ventas` son el ejemplo guiado del docente y no forman parte del dominio del proyecto.

### 1.1 Operaciones transaccionales

**Registro de participación (cabecera + detalle + cupo).** Una sola transacción crea `REGISTROS`, sus `DETALLE_EVIDENCIAS` y suma las evidencias en `CUPOS_DIARIOS`. Si una línea supera el cupo diario de la campaña, se lanza `CupoDiarioExcedidoException` y Oracle deshace todo: no queda cabecera sin detalle ni cupo consumido a medias.

**Acta de premiación (cabecera + detalle + stock).** Emitir el acta crea `ACTAS_PREMIACION`, sus detalles y descuenta el stock de cada premio en la misma transacción. Si un premio no alcanza, se rechaza el acta completa. Anular el acta devuelve el stock.

### 1.2 Relaciones muchos a muchos

`CAMPANIA_HABITOS` (qué hábitos cuentan en una campaña) y `CAMPANIA_SEDES` (en qué sedes corre) se modelan como entidades de unión explícitas, no con `@ManyToMany`. Así cada módulo guarda solo identificadores del otro y Spring Modulith no detecta dependencias cruzadas entre entidades de módulos distintos.

## 2. Rendimiento y preparación para producción

### 2.1 Carga diferida y presupuestos

Las 23 rutas de `app.routes.ts` usan `loadComponent`, así que cada pantalla se descarga solo cuando se visita. `angular.json` define presupuestos en la configuración `production`:

| Tipo | Advertencia | Error |
|---|---|---|
| `initial` | 500 kB | 1 MB |
| `anyComponentStyle` | 4 kB | 8 kB |

### 2.2 Paginación con agregado correcto

El reporte de participación pide cada página a `GET /api/v1/participacion/registros/pagina` (Spring Data `Page` con `countQuery`) y los totales a `GET /api/v1/participacion/registros/totales`. Los totales (registros, evidencias, puntaje validado, promedio y hábito más registrado) se calculan en Oracle con `COUNT`, `SUM` y `GROUP BY` sobre **todos** los registros que cumplen los filtros, no sobre la página visible.

### 2.3 Caché del navegador

`GET /api/v1/categorias-habito` y `GET /api/v1/habitos` responden con `Cache-Control: no-cache, private` y un `ETag` calculado por `EtagCatalogosFilter` (subclase de `ShallowEtagHeaderFilter`). El navegador guarda la respuesta y, en cada visita, pregunta con `If-None-Match`. Si el catálogo no cambió, el backend responde **304** sin volver a enviar el cuerpo. `private` evita que un proxy compartido guarde datos de un usuario autenticado. Nunca se aplica a endpoints que escriben.

### 2.4 Decisión sobre Redis: descartado

Medición del 8 de noviembre de 2026, de 15:09 a 15:14 (5 minutos de uso normal como administrador: crear categorías, hábitos, campaña, premios y revisar pantallas), en Network del navegador:

| Endpoint | Lecturas (GET) | Respuestas 200 | Respuestas 304 | Tiempo por petición |
|---|---|---|---|---|
| `GET /habitos` | 7 | 1 | 6 | 13–19 ms |
| `GET /categorias-habito` | 8 | 1 | 7 | 12–24 ms |

En 5 minutos hubo 15 lecturas de los catálogos candidatos, unos 0.2 segundos de trabajo total del backend, y 13 de ellas se resolvieron con 304. Redis agregaría un servicio más que levantar, asegurar y mantener sincronizado (invalidar al crear o editar) para ahorrar milisegundos que hoy no se notan. **Se descarta.** Se reconsideraría si la medición con usuarios reales mostrara cientos de lecturas por minuto de un mismo dato o una consulta que tarde cientos de milisegundos.

### 2.5 Logging, Actuator y configuración de producción

- `logback-spring.xml`: `pe.edu.upeu.bomerp` en `DEBUG` con el perfil `dev` y en `INFO` con `prod`; Hibernate en `WARN` en `prod`.
- Actuator: `dev` expone todo; `prod` solo `health,info`, y además la seguridad JWT rechaza cualquier petición sin token.
- `application-prod.yml` lee conexión, usuario, contraseña e issuer de Keycloak desde variables de entorno. `.env.example` documenta los nombres sin valores reales.
- `environment.prod.ts` reemplaza a `environment.ts` en el build de producción por `fileReplacements`.

### 2.6 Auditoría

Cada acción importante queda en `BOM_AUDITORIA.EVENTOS` con fecha, usuario, nombre, acción, entidad, detalle, sede y el `traceId` de la petición:

| Grupo | Acciones |
|---|---|
| Seguridad | crear cuenta, cambiar rol, activar o desactivar, restablecer contraseña |
| Puntos | registrar participación, validar o rechazar evidencia, validar, rechazar o anular registro |
| Premios | emitir o anular acta; crear, editar o eliminar premios y categorías de premio (con stock anterior y nuevo) |
| Reglas | crear, editar o eliminar campañas, hábitos (con puntaje anterior y nuevo) y categorías de hábito |

- **Inmutable:** el usuario de la aplicación `BOMERP_APP` solo tiene `SELECT` e `INSERT` sobre la tabla. Ni con un error en el código se puede editar o borrar un evento.
- **No rompe la operación:** se guarda en una transacción propia (`REQUIRES_NEW`) y, si falla, solo se escribe una advertencia en el log.
- **Sin secretos:** nunca guarda contraseñas ni tokens; al restablecer una contraseña solo se registra que ocurrió.
- Solo el rol `ADMIN` puede consultarla (`/api/v1/auditoria`).

## 3. Plan de pruebas

### 3.1 Pruebas automáticas

| Prueba | Tipo | Qué verifica |
|---|---|---|
| `ModularityTests` | Arquitectura | Que ningún módulo use el interior de otro (Spring Modulith) |
| `BomerpBackendApplicationTests` | Arranque | Que el contexto completo levante contra Oracle |
| `ParticipacionEndToEndTest` | De punta a punta, Oracle real | El flujo completo de participación |

`ParticipacionEndToEndTest` crea su propia categoría, hábito de 2.00 puntos, campaña activa con cupo de 3 por día y estudiante, y comprueba en orden:

1. Dos registros válidos suman 4.00 puntos y consumen 2 evidencias del cupo.
2. Un registro con 2 evidencias más supera el cupo: se lanza `CupoDiarioExcedidoException` y el cupo sigue en 2 (rollback completo).
3. Un tercer registro de 1 evidencia sí entra.
4. Validar una evidencia deja el puntaje validado en 4.00.
5. La página de tamaño 1 informa 2 registros y 2 páginas.
6. Los totales dan 2 registros, 3 evidencias, 4.00 puntos y el hábito más registrado.
7. El ranking pone al estudiante en el puesto 1 con 4.00 puntos.

Al terminar borra todo lo que creó. Resultado: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.

```powershell
.\mvnw test -Dtest=ParticipacionEndToEndTest
```

### 3.2 Pruebas manuales con errores forzados

| Caso | Acción | Resultado esperado |
|---|---|---|
| Cupo diario | Registrar más evidencias que el cupo en un día | Mensaje de cupo excedido; no se guarda nada |
| Stock | Emitir un acta con más unidades que el stock | Acta rechazada; stock intacto |
| Anular acta | Anular un acta emitida | El stock vuelve a su valor anterior |
| Fechas | Campaña con fecha fin igual o anterior a la de inicio | El backend responde 400 con el mensaje de fechas |
| Rol | Estudiante entra a `/auditoria` o `/cuentas` | Pantalla de sin permiso; el backend responde 403 |
| Sede | Coordinador de Juliaca pide datos de Lima | Solo recibe datos de su sede |
| Caché | Visitar Hábitos dos veces | La segunda petición responde 304 |

## 4. Bitácora de hallazgos

| Fecha | Hallazgo | Corrección |
|---|---|---|
| 08/11/2026 | El reporte sumaba evidencias y puntaje con `.reduce()` en el navegador sobre los registros cargados. Al paginar, los totales habrían mostrado solo la página visible. | Totales calculados en Oracle con `/totales`, independientes de la página |
| 08/11/2026 | Crear o editar hábitos y premios no quedaba en la auditoría, aunque cambiar el puntaje de un hábito o el stock de un premio afecta puntos y premios. | Auditoría en los cuatro catálogos con valor anterior y nuevo |
| 08/11/2026 | `categoria-habito-form.ts` imprimía el formulario en la consola en cada tecla, resto de una práctica de clase. | Se eliminaron los `console.log` |
| 08/11/2026 | El panel de coordinación muestra un 404 en consola cuando la sede no tiene campaña activa. | Es la respuesta esperada; el frontend la maneja mostrando "sin campaña activa" |
