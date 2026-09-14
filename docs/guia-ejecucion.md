# Guía de ejecución

Cómo levantar el backend desde cero en una máquina limpia. Cada paso indica qué debe verse si salió bien.

## 1. Requisitos

| Herramienta | Versión mínima | Cómo verificar |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Docker Desktop | En ejecución | `docker ps` |
| Git | Cualquiera reciente | `git --version` |

Maven no hace falta instalarlo: el proyecto trae el wrapper `mvnw`.

## 2. Clonar el repositorio

```bash
git clone https://github.com/MGMM7STA/bomerp-backend.git
cd bomerp-backend
```

## 3. Levantar Oracle

```bash
docker compose -f compose-dev.yml up -d
```

Oracle 23ai Free tarda entre uno y tres minutos en quedar listo la primera vez. Está listo cuando esto responde:

```bash
docker logs bomerp-oracle | findstr "DATABASE IS READY"
```

## 4. Crear los esquemas y los datos

Los scripts viven en `sql/` y se ejecutan **en orden**. Cada uno se copia primero al contenedor, porque `sqlplus` corre dentro de Oracle y no ve el disco de Windows.

```bash
docker cp sql/S01_01_esquemas.sql bomerp-oracle:/tmp/a.sql
docker exec -i bomerp-oracle sqlplus -S "system/123456@localhost:1521/FREEPDB1" "@/tmp/a.sql"
```

Repetir con cada script, en este orden:

| Orden | Script | Qué crea |
|---:|---|---|
| 1 | `S01_01_esquemas.sql` | Los esquemas y el usuario de aplicación. |
| 2 | `S01_02_tablas.sql` | Tablas base. |
| 3 | `S01_03_datos.sql` | Datos iniciales. |
| 4 | `S02_tablas_ref.sql` | Tablas de referencia. |
| 5 | `S02_fix_id_categoria.sql` | Corrección de la relación categoría–producto. |
| 6 | `S04_campanias.sql` | Esquema `BOM_CAMPANIAS`: campañas, hábitos y cupos. |
| 7 | `S04_ventas.sql` | Esquema del ejemplo guiado. |
| 8 | `S05_datos_participacion.sql` | Registros de participación de muestra. |
| 9 | `S06_categorias_habito.sql` | Tabla `CATEGORIAS_HABITO`, la FK con `HABITOS` y sus datos. |

## 5. Arrancar la aplicación

```bash
./mvnw spring-boot:run
```

Está arriba cuando el log dice `Started BomerpBackendApplication`.

Si en lugar de eso aparece `Schema validation: missing table [...]`, significa que falta ejecutar alguno de los scripts del paso 4. La aplicación corre con `ddl-auto: validate`: se niega a arrancar si una entidad no tiene su tabla real. Es intencional — es preferible que falle al arrancar a que falle a mitad de una operación.

## 6. Verificar

| Qué | Dónde |
|---|---|
| Estado de la aplicación | `http://localhost:8080/actuator/health` |
| Documentación de la API | `http://localhost:8080/swagger-ui.html` |
| Contrato OpenAPI | `http://localhost:8080/v3/api-docs` |

## 7. Ejecutar las pruebas

```bash
./mvnw test
```

Deben pasar 7 pruebas, incluidas las dos de `ModularityTests`, que son las que verifican que ningún módulo accede a los repositorios o entidades de otro.

## Configuración externa

Ninguna credencial está escrita en el código. Se leen de `application-dev.yml` y pueden sobrescribirse por variable de entorno.

| Propiedad | Para qué sirve |
|---|---|
| `spring.datasource.url` | Cadena de conexión a Oracle. |
| `spring.jpa.hibernate.ddl-auto` | Fijado en `validate`: Hibernate nunca modifica el esquema. |
| `bomerp.cors.allowed-origins` | Lista de orígenes autorizados, separados por coma. Cambiarla no requiere recompilar. |
