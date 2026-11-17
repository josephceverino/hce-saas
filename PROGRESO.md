# Progreso del plan v3 (rama `integracion`)

Trabajo autónomo sobre `PLAN_V3.md` y `docs/recorrido.md`. Cada ítem: un commit que cita sus IDs, `mvnw test` en verde y la app arrancando.
Las decisiones tomadas sin respaldo en los documentos están en `DECISIONES_PENDIENTES.md`.

## A. Preparación
- [x] A1. Las 7 ramas encadenadas (v5 → M-2 → pantallas-1 → pantallas-2 → consentimiento → pantallas-3 → pantallas-4) ya estaban fusionadas en `main` por los PR #8 a #14. Se creó `integracion` desde `docs/plan-v3` (que contiene a todas) y se fusionó `origin/main` sin conflictos. 119 tests en verde, la app arranca. Commit `a7cd750`.
- [x] A2. Creados `PROGRESO.md` y `DECISIONES_PENDIENTES.md` en la raíz. Commit `a7db149`.

## R0 Base
- [ ] 0.1 Quitar cuentas demo sembradas, recuadro de cuentas del login y lista numerada.
- [ ] 0.2 Arranque con un único ADMIN_PLATAFORMA con clave temporal configurable y `debe_cambiar_clave`.
- [ ] 0.3 CU-18: cambio de clave en el primer ingreso (RN-14, RN-12), P-13 y tests.
- [ ] 0.4 Los 7 roles con menú e inicio propio; indicadores de la base de datos.

## R1 Cuentas
- [ ] 1.1 CU-16: alta de hospital con su administrador (P-07, tests de RN-16).
- [ ] 1.2 CU-17: usuarios del hospital (P-08, test: un administrador no crea usuarios de otro hospital).
- [ ] 1.3 Filiación completa en el registro de paciente (P-16).
- [ ] 1.4 CU-19: cuenta del paciente al registrarlo, con constancia imprimible.
- [ ] 1.5 Script Playwright de los pasos 1 a 3 con capturas.

## R2 Recepción
- [ ] 2.1 Admisión y estados RN-13 (CU-20), un test por transición permitida y prohibida.
- [ ] 2.2 Cola por rol (P-09, RF-22); emergencia primero.
- [ ] 2.3 Signos vitales (CU-21, RN-17) y antecedentes por tipo dentro de P-04.
- [ ] 2.4 Script Playwright de los pasos 1 a 5.
