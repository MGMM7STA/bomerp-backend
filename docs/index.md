# Semestre Saludable UPeU — Backend

Backend REST del proyecto **Semestre Saludable UPeU**, Proyecto Integrador del ciclo 4 de la Universidad Peruana Unión, sede Juliaca: Análisis y Diseño de Software, Administración de Base de Datos II y Lenguaje de Programación II.

**Equipo 01:** Montalvo Machaca, Maykol Gabriel · Cruz Mendoza, Deysi — Sección g2 — Semestre 2026-2

## Qué hace este sistema

La universidad abre una campaña de hábitos saludables. Durante la campaña, cada estudiante registra evidencias diarias de lo que hizo — caminó, comió fruta, durmió bien, tomó agua — con su foto. La campaña limita cuántas evidencias puede registrar un estudiante por día, para que el puntaje refleje constancia y no un solo día de entusiasmo.

La coordinación de cada sede valida o rechaza cada evidencia. Solo el puntaje validado sube al ranking y, al cierre, se emite un acta de premiación que descuenta el stock de los premios entregados. Cada acción importante queda en la auditoría.

## Cómo está construido

Es una sola aplicación Spring Boot organizada como **monolito modular**: un único ejecutable dividido internamente en módulos con fronteras verificadas automáticamente.

| Módulo | Responsabilidad |
|---|---|
| `campanias` | El catálogo: categorías de hábito, hábitos, campañas por sede y el cupo diario. |
| `participacion` | La operación: el estudiante registra su participación del día con todas sus evidencias, de forma atómica; validación, reporte y ranking. |
| `premios` | Premios con stock y actas de premiación. |
| `cuentas` | Cuentas y roles en Keycloak, y solicitudes de cuenta que llegan desde Odoo. |
| `sedes` | La sede de cada usuario y el alcance de sus consultas. |
| `auditoria` | Registro inmutable de las acciones importantes. |

## Por dónde empezar

- **[Sustentación final integrada](proyecto-integrador/u3/sustentacion-final.md)** — el sistema visto desde los tres cursos: diseño, operación, aplicación, demo y trazabilidad.
- **[Producto de Unidad 3](proyecto-integrador/u3/lp2-producto.md)** — componentes finales, rendimiento, caché, auditoría, pruebas y bitácora de hallazgos.
- **[Producto de Unidad 1](proyecto-integrador/u1/lp2-demo.md)** — alcance, contrato REST, DTO, arquitectura, casos de prueba y trazabilidad.
- **[Guía de ejecución](guia-ejecucion.md)** — cómo levantar el proyecto desde cero en otra máquina.

## Stack

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.7 |
| Spring Modulith | Verificación de módulos con ArchUnit |
| Hibernate ORM | 7.2 |
| Oracle Database | 23ai Free (Docker) |
| Keycloak | 26 (Docker), con PostgreSQL 17 |
| MapStruct | 1.6.3 |
| springdoc-openapi | 3.0.2 |
