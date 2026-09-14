# Semestre Saludable UPeU — Backend

Backend REST del proyecto **Semestre Saludable UPeU**, desarrollado para el curso de Lenguaje de Programación II de la Universidad Peruana Unión, sede Juliaca.

**Autor:** Montalvo Machaca, Maykol Gabriel — Sección g2 — Semestre 2026-2

## Qué hace este sistema

La universidad abre una campaña de hábitos saludables. Durante la campaña, cada estudiante registra evidencias diarias de lo que hizo — caminó, comió fruta, durmió bien, tomó agua — y cada evidencia suma puntaje. La campaña limita cuántas evidencias puede registrar un estudiante por día, para que el puntaje refleje constancia y no un solo día de entusiasmo.

## Cómo está construido

Es una sola aplicación Spring Boot organizada como **monolito modular**: un único ejecutable dividido internamente en módulos con fronteras verificadas automáticamente.

| Módulo | Responsabilidad |
|---|---|
| [`campanias`](proyecto-integrador/u1/lp2-demo.md#1-alcance-arquitectonico-del-corte) | El catálogo: qué categorías de hábito existen, qué hábitos hay en cada una, y qué campaña está vigente con su cupo diario. |
| [`participacion`](proyecto-integrador/u1/lp2-demo.md#4-dto-principales) | La operación: el estudiante registra su participación del día con todas sus evidencias, de forma atómica. |

## Por dónde empezar

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
| MapStruct | 1.6.3 |
| springdoc-openapi | 3.0.2 |
