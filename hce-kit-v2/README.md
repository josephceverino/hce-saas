# Kit para iniciar el software (versión 2)
Demo: **11 de diciembre de 2026**, PC con Windows. Enfoque: hospitales públicos.

## Orden de lectura
1. `PLAN_CALENDARIO.md`: fechas, recortes y riesgos.
2. `CLAUDE.md`: reglas del proyecto y decisiones técnicas cerradas (Java nivel 21, Spring Boot 4.x, H2 en archivo, puerto 8090, portable).
3. `docs/`: requisitos, casos de uso, arquitectura, modelo de datos, `diseno_ui.md`, `equipo_y_flujo.md`, `reglas_para_ia_externa.md`, `pruebas/`.
4. `PROMPTS.md`: prompts para Claude Code (Pablo).
5. `FICHAS_TAREAS.md`: tareas por persona.
6. `ONBOARDING_MATTIAS.md` y `GIT_SETUP.md`.

## Cómo empezar
1. Copia el contenido a la carpeta del proyecto, `git init` y sube a un repo privado de GitHub.
2. Pablo hace el prompt 0 y 1 con Claude Code (Fase 0).
3. Alexander y Mattias hacen la ficha F0-1 y F0-2.

## Avisos
- La semilla v2 y el script de SQL Server **nunca se compilaron/ejecutaron en mi entorno** (solo revisión de sintaxis). La migración a Spring Boot 4.x es parte del prompt 1 y puede dar trabajo; el plan B es 3.5.x.
- `docs/` sale del informe de Drive: si el grupo corrige algo allí, actualizar aquí.
- Los casos de prueba están en estado Pendiente: nadie los ha ejecutado.
