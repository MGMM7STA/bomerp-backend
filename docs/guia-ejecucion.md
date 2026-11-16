# Guía de ejecución

Cómo levantar el backend desde cero en una máquina limpia. Cada paso indica qué debe verse si salió bien.

## 1. Requisitos

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Docker Desktop | En ejecución | `docker ps` |
| Node.js | 22 | `node -v` |
| Git | Cualquiera reciente | `git --version` |

Maven no hace falta instalarlo: el proyecto trae el wrapper `mvnw`.

## 2. Clonar los repositorios

```powershell
git clone https://github.com/MGMM7STA/bomerp-backend.git
git clone https://github.com/MGMM7STA/bomerp-frontend.git
cd bomerp-backend
```

## 3. Levantar Oracle, Keycloak y PostgreSQL

```powershell
docker compose -f compose-dev.yml up -d
```

Levanta tres contenedores:

| Contenedor | Qué es | Puerto |
|---|---|---|
| `bomerp-oracle` | Oracle 23ai Free | 1521 |
| `bomerp-keycloak` | Keycloak 26 con el realm `saludable` importado | 38080 |
| `bomerp-keycloak-db` | PostgreSQL 17, la base de Keycloak | 35432 |

Oracle tarda entre uno y tres minutos en quedar listo la primera vez. Está listo cuando esto responde:

```powershell
docker logs bomerp-oracle | findstr "DATABASE IS READY"
```

Keycloak importa solo el realm con tres usuarios de prueba, uno por rol: `estudiante1`, `coordinador1` y `admin1`. Están definidos en `keycloak/import/realm-saludable.json`.

## 4. Crear los esquemas y los datos

Los scripts viven en `sql/` y se ejecutan **en este orden**, como `SYSTEM`. El modelo completo que dejan creado está en [Modelo de datos](proyecto-integrador/u3/modelo-datos.md).

```powershell
Get-Content sql\S12_base_completa.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
Get-Content sql\S14_estudiantes_validacion_campanias.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
Get-Content sql\S15_revision_por_evidencia.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
Get-Content sql\S16_solicitudes_cuenta.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
Get-Content sql\S17_sedes_y_odoo.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
Get-Content sql\S19_auditoria.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
Get-Content sql\S20_integridad_referencial.sql | docker exec -i bomerp-oracle sqlplus -S system/123456@localhost:1521/FREEPDB1
```

| Orden | Script | Qué crea |
|---:|---|---|
| 1 | `S12_base_completa.sql` | La base completa desde cero: reúne los scripts S01 a S12 (esquemas, usuario de aplicación, campañas, hábitos, participación y premios). |
| 2 | `S14_estudiantes_validacion_campanias.sql` | Estudiantes enlazados a Keycloak, revisión de evidencias y hábitos por campaña. |
| 3 | `S15_revision_por_evidencia.sql` | Estado, motivo de rechazo y archivo de imagen en cada evidencia. |
| 4 | `S16_solicitudes_cuenta.sql` | Datos del estudiante y la tabla `SOLICITUDES_CUENTA`. |
| 5 | `S17_sedes_y_odoo.sql` | Sede de cada usuario, sedes por campaña y el enlace con Odoo. |
| 6 | `S19_auditoria.sql` | Esquema `BOM_AUDITORIA` y la tabla `EVENTOS`, solo con `SELECT` e `INSERT` para la aplicación. |
| 7 | `S20_integridad_referencial.sql` | Llaves foráneas que faltaban dentro de un mismo esquema (registro con su estudiante, detalle de acta con su premio) e índices sobre las columnas de esas llaves. |

`S18_reinicio_datos.sql` **no** forma parte de la instalación: borra los datos del proyecto para empezar de cero.

Si `S12_base_completa.sql` responde `ORA-01920`, los esquemas ya existían: la base ya estaba creada.

## 5. Arrancar el backend

```powershell
.\mvnw spring-boot:run
```

Está arriba cuando el log dice `Started BomerpBackendApplication`, en el puerto 8080.

Si en lugar de eso aparece `Schema validation: missing table [...]`, falta ejecutar alguno de los scripts del paso 4. La aplicación corre con `ddl-auto: validate`: se niega a arrancar si una entidad no tiene su tabla real. Es intencional, porque es preferible que falle al arrancar a que falle a mitad de una operación.

## 6. Arrancar el frontend

En otra terminal:

```powershell
cd ..\bomerp-frontend
npm install
npm start
```

La SPA queda en `http://localhost:4200`. En desarrollo ya apunta al backend en el puerto 8080 y a Keycloak en el 38080, así que no hay nada que configurar.

## 7. Verificar

| Qué | Dónde |
|---|---|
| Aplicación | `http://localhost:4200`, entrando con `estudiante1`, `coordinador1` o `admin1` |
| Estado del backend | `http://localhost:8080/actuator/health` |
| Documentación de la API | `http://localhost:8080/swagger-ui.html` |
| Contrato OpenAPI | `http://localhost:8080/v3/api-docs` |

Para probar el flujo completo, entra como `admin1` y crea una campaña **Activa** en tu sede con dos o tres hábitos. Después entra como `estudiante1` y registra una participación.

## 8. Ejecutar las pruebas

```powershell
.\mvnw test
```

| Prueba | Qué verifica |
|---|---|
| `ModularityTests` | Que ningún módulo acceda a los repositorios o entidades de otro. |
| `BomerpBackendApplicationTests` | Que el contexto completo levante contra Oracle. |
| `ParticipacionEndToEndTest` | El flujo de participación de punta a punta contra Oracle real: cupo, rollback, validación, paginación, totales y ranking. |
| `ProductoControllerTest` | El controlador del ejemplo guiado del docente. |

Necesitan Oracle arriba, porque trabajan contra la base real.

## 9. Ejecución con el perfil de producción

`compose.yml` levanta el backend ya empaquetado junto con Oracle, Keycloak y PostgreSQL, con el perfil `prod`.

```powershell
copy .env.example .env
docker compose up -d --build
```

Antes de levantar, completa los valores de `.env`. Ese archivo no se sube al repositorio.

| Servicio | Puerto en la máquina |
|---|---|
| Backend | 48088 |
| Keycloak | 48080 |
| Oracle | 41521 |
| PostgreSQL de Keycloak | 45432 |

El build de producción de la SPA (`npm run build`) usa `environment.prod.ts`, que apunta a esos puertos. El backend autoriza por CORS el origen `http://localhost:4300`.

## Configuración externa

Ninguna credencial está escrita en el código. Se leen de `application-dev.yml` y pueden sobrescribirse por variable de entorno.

| Propiedad | Para qué sirve |
|---|---|
| `spring.datasource.url` | Cadena de conexión a Oracle. |
| `spring.jpa.hibernate.ddl-auto` | Fijado en `validate`: Hibernate nunca modifica el esquema. |
| `bomerp.cors.allowed-origins` | Lista de orígenes autorizados, separados por coma. Cambiarla no requiere recompilar. |
