# Semestre Saludable UPeU — Backend

Backend REST del proyecto **Semestre Saludable UPeU**: el estudiante registra evidencias diarias de hábitos saludables con foto, la coordinación de su sede las valida, el puntaje validado arma el ranking y, al cierre, se emite un acta de premiación que descuenta el stock de los premios.

Proyecto Integrador del ciclo 4 (Análisis y Diseño de Software, Administración de Base de Datos II y Lenguaje de Programación II) — Universidad Peruana Unión, sede Juliaca — Semestre 2026-2.

**Equipo 01:** Montalvo Machaca, Maykol Gabriel · Cruz Mendoza, Deysi

## Stack

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4 |
| Spring Modulith | Límites entre módulos verificados con pruebas |
| Oracle Database | 23ai Free (Docker) |
| Keycloak | 26, con PostgreSQL 17 |
| MapStruct | 1.6 |

## Módulos

| Módulo | Esquema Oracle | Responsabilidad |
|---|---|---|
| `campanias` | `BOM_CAMPANIAS` | Categorías de hábito, hábitos, campañas y cupo diario |
| `participacion` | `BOM_PARTICIPACION` | Estudiantes, registros con sus evidencias, validación, reporte y ranking |
| `premios` | `BOM_PREMIOS` | Categorías de premio, premios con stock y actas de premiación |
| `cuentas` | Keycloak y `BOM_PARTICIPACION` | Cuentas, roles y solicitudes de cuenta desde Odoo |
| `sedes` | `BOM_PARTICIPACION` | Sede de cada usuario y alcance de las consultas |
| `auditoria` | `BOM_AUDITORIA` | Registro inmutable de las acciones importantes |

## Cómo levantarlo

```powershell
docker compose -f compose-dev.yml up -d
.\mvnw spring-boot:run
```

Entre un comando y otro hay que crear la base con los scripts de `sql/`. El orden exacto, el frontend y la ejecución con el perfil de producción están en la [guía de ejecución](docs/guia-ejecucion.md).

## Pruebas

```powershell
.\mvnw test
```

Incluye `ModularityTests` (límites entre módulos) y `ParticipacionEndToEndTest` (flujo completo contra Oracle real).

## Documentación

- Sitio publicado: https://mgmm7sta.github.io/bomerp-backend
- [Sustentación final integrada](docs/proyecto-integrador/u3/sustentacion-final.md)
- [Modelo de datos](docs/proyecto-integrador/u3/modelo-datos.md)
- [Producto de Unidad 3](docs/proyecto-integrador/u3/lp2-producto.md)
- Frontend: https://github.com/MGMM7STA/bomerp-frontend
