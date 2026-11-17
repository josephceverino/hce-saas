# Progreso del plan v3 (rama `integracion`)

Trabajo autónomo sobre `PLAN_V3.md` y `docs/recorrido.md`. Cada ítem: un commit que cita sus IDs, `mvnw test` en verde y la app arrancando.
Las decisiones tomadas sin respaldo en los documentos están en `DECISIONES_PENDIENTES.md`.

## A. Preparación
- [x] A1. Las 7 ramas encadenadas (v5 → M-2 → pantallas-1 → pantallas-2 → consentimiento → pantallas-3 → pantallas-4) ya estaban fusionadas en `main` por los PR #8 a #14. Se creó `integracion` desde `docs/plan-v3` (que contiene a todas) y se fusionó `origin/main` sin conflictos. 119 tests en verde, la app arranca. Commit `a7cd750`.
- [x] A2. Creados `PROGRESO.md` y `DECISIONES_PENDIENTES.md` en la raíz. Commit `a7db149`.

## R0 Base
- [x] 0.1 Sin cuentas ni pacientes demo sembrados en la aplicación (solo en tests); login sin recuadro de cuentas ni lista, rediseñado según la referencia 04; Playwright en `e2e/`. Commit `f18a6c7`.
- [x] 0.2 `AdministradorInicial` crea un único administrador de plataforma (`admin` / `Temporal2026`, configurables) con `debe_cambiar_clave`; documentado en el README. Commit `9a9f532`.
- [x] 0.3 CU-18: `POST /api/sesion/clave`, RN-14 aplicada en el servidor (403 a todo lo demás), pantalla P-13 con guía de requisitos, 5 tests y `e2e/primer-ingreso.mjs`. Commit `7d81067`.
- [x] 0.4 Menú e Inicio propio para los 7 roles (apoyo al diagnóstico incluido, sin cifras inventadas); estructura visual rehecha según la referencia 01; capturas `inicio_*.png`. Commit `1d4b7a7`.

## R1 Cuentas
- [x] 1.1 CU-16: `POST /api/hospitales`, suscripción, administrador con clave temporal mostrada una vez en una constancia imprimible; P-07 rehecha; 5 tests (RN-16, código repetido, plan). Commit `8d27920`.
- [x] 1.2 CU-17: `/api/usuarios` (listar, crear con clave temporal y colegiatura, desactivar, reactivar); P-08 según la referencia 05; 6 tests, incluido que un administrador no crea ni toca usuarios de otro hospital. Commit `4b9b8c7`.
- [x] 1.3 Filiación completa (lugar de nacimiento, procedencia, estado civil, instrucción, ocupación, religión, contacto de emergencia) en entidad, validación y P-16 por secciones; tests de filiación y de contacto de emergencia. Commit `1f1c2f2`.
- [x] 1.4 CU-19: al registrar un paciente nuevo se crea su cuenta (usuario = documento, clave temporal, sin hospital) y P-16 muestra la constancia una vez; 3 tests, incluido que el paciente entra, cambia su clave y ve su historia. Commit `70ccdff`.
- [x] 1.5 `e2e/recorrido.mjs` recorre los pasos 1 a 3 con verificaciones (`ok:`) y capturas `paso1_*` a `paso3_*`; `npm run recorrido -- 3`; instrucciones en `e2e/LEEME.md`. Commit `e358b35`.

## R2 Recepción
- [x] 2.1 CU-20: entidad `Admision` y `EstadoAdmision` (RN-13 en el dominio), `/api/admisiones`, diálogo Admitir en P-02; `EstadoAdmisionTest` (5 transiciones permitidas y 31 prohibidas) y `AdmisionServiceTest` (7). Commit `c603782`.
- [x] 2.2 P-09 `/cola/` por rol (admisión ve todo el día; enfermería, admitidos y en enfermería; médico, listos y en atención), emergencia primero, tiempo de espera y acciones por rol; la cola también en el Inicio de esos roles. Commit `eb1b6d8`.
- [ ] 2.3 Signos vitales (CU-21, RN-17) y antecedentes por tipo dentro de P-04.
- [ ] 2.4 Script Playwright de los pasos 1 a 5.
