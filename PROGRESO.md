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
- [x] 2.3 CU-21: `SignosVitales` con rangos de demostración (no clínicos), registro solo con la admisión en enfermería, "listo" exige signos vitales; antecedentes por tipo y alergias; P-04 rehecha según la referencia 02; 6 tests. Commit `8888e41`.
- [x] 2.4 `npm run recorrido -- 5` recorre los pasos 1 a 5 con 15 verificaciones, sin errores de consola, y deja sus capturas (paso1_ a paso5_) en `docs/capturas/`. Commit `b572352`.

## R3 (aprobado el 17 nov: restablecer clave sí, tramo B recortado, `docs/` se actualiza)
- [x] R3a. CU-26 restablecer clave: administrador del hospital → su personal, plataforma → administrador de hospital, admisión → pacientes de su hospital; clave temporal una vez, cambio obligatorio, levanta el bloqueo; botones en P-07, P-08 y P-02; 6 tests (202 en total). Decisión D-39.
- [x] R3b.1 El médico inicia la atención desde la cola (LISTO → EN_ATENCION), atención ampliada (enfermedad actual, funciones biológicas, examen por sistemas) y el cierre de la atención cierra la admisión (RN-13); 5 tests (207 en total). Decisión D-40.
- [x] R3b.2 Pedido de exámenes (CU-22, RF-25; RN-18): tabla `solicitud_examen`, formulario en la atención abierta, lista en la atención y la historia.
- [x] R3b.3 Resultados (CU-23, RF-09; RN-18, RN-06): P-12 `/examenes/` para apoyo al diagnóstico, tabla `resultado_examen`, cifras reales en su Inicio; 6 tests (213 en total). Decisión D-41.
- [x] R3b.4 Cierre de la atención: cerrarla pasa la admisión a ATENCION_CERRADA y la atención cerrada no se edita (RN-03); quedó cubierto por los tests de R3b.1 y el paso 8 del recorrido.
- [x] R3b.5 `npm run recorrido -- 8` recorre los pasos 1 a 8 con 28 verificaciones y sin errores de consola; capturas paso6_ a paso8_ en `docs/capturas/`. `docs/script_sqlserver.sql` actualizado (admision, signos_vitales, solicitud_examen y columnas nuevas; sin probar en SQL Server).

## R4 (aprobado el 17 nov: solo atenciones cerradas, sin signos vitales en el portal, sin cerrar sesiones al restablecer)
- [x] R4.1 Portal verificado con la historia nueva: el paciente ve solo atenciones cerradas, con enfermedad actual, funciones biológicas, examen por sistemas y exámenes con su resultado; no ve atenciones abiertas ni signos vitales; no ve lo de otro paciente (RN-04). No hizo falta cambiar código; 4 tests nuevos (217 en total).
- [x] R4.2 El resumen descargable (`/api/mi-historial/resumen`) lista los exámenes con resultado de las atenciones cerradas; `Mi historial` ya los mostraba dentro de cada atención. Contrato `docs/contratos/portal_paciente.md` actualizado; 1 test nuevo (218 en total).
- [x] R4.3 Consentimiento (CU-11): ya estaba construido y cubierto por 7 tests (otorgar, revocar, no revocar dos veces, aislamiento entre pacientes, solo el paciente, campos vacíos, auditoría). Sin cambios de código. Aclaración: el consentimiento es un registro con fecha y alcance (RF-17); no bloquea el acceso del personal del hospital, porque el intercambio entre hospitales (RF-18) no está en el alcance de la demo. La verificación en pantalla queda en el recorrido del paso 9 (R4.5).
- [x] R4.4 Auditoría CU-25: comprobado con datos reales (8090) y con 2 tests nuevos (220 en total) que las acciones de admisión, atención desde la cola, exámenes y restablecer clave se registran, se filtran y solo las ve el administrador de su hospital (RN-06, RN-07). Sin cambios de código. Decisión D-42.
- [x] R4.5 Recorrido paso 9 con 9 verificaciones y sin errores de consola (restablecer clave del paciente, historial cerrado sin signos vitales, resumen, consentimiento otorgado y revocado, auditoría); capturas `paso9_` en `docs/capturas/`. El recorrido acepta un paso inicial: `node recorrido.mjs 9 9`.

## R5 Paquete portable (probado en esta PC)
- [x] R5.1 `empaquetar.bat` (en una copia limpia, con 220 tests dentro del build) genera `dist\HCE-Demo\HCE-Demo.exe` con Java 25 incluido y `dist\HCE-Demo.zip` (78 MB).
- [x] R5.2 Probado el `.exe`: con la app de desarrollo ocupando 8090 abrió sola en 8091; los datos se crean en `dist\HCE-Demo\data` junto al programa; el recorrido completo (pasos 1 a 9, 37 verificaciones) pasó contra el `.exe` y sin errores; tras cerrar y reabrir, los usuarios y datos siguen.
- [x] R5.3 `dist\data_ensayo`: copia de `data` ya poblada por el recorrido para restaurar la demo (usuarios en `GUIA_INSTALACION.md`).
- [x] R5.4 `GUIA_INSTALACION.md` actualizada: sin cuentas demo, administrador inicial, restaurar `data_ensayo`.

## Pruebas
- [x] `docs/pruebas/pruebas_manuales_v3.md`: 24 casos manuales (MV-01 a MV-24) para ejecutar en esta PC, con el resultado esperado y la captura de referencia de cada uno. Todos en Pendiente: se llenan al ejecutarlos.

## Resumen al parar (cierre de R2) (checklist completo, 0 ítems bloqueados)

**Qué quedó hecho.** Los 17 ítems del checklist. La rama `integracion` contiene todo y está subida.
- R0: sin cuentas de demostración; un único administrador de plataforma inicial (`admin` / `Temporal2026`, configurable); cambio de clave obligatorio en el primer ingreso (RN-14, aplicado en el servidor); menú e Inicio propio para los 7 roles; nueva estructura visual (referencias 01 y 04).
- R1: alta de hospital con su administrador (CU-16, RN-16); usuarios del hospital (CU-17, RN-16); filiación completa; cuenta del portal del paciente al registrarlo con constancia imprimible (CU-19, RN-15).
- R2: admisión del día con los estados de RN-13 en el dominio (36 transiciones probadas); cola por rol con la emergencia primero (RF-22); signos vitales con rangos de demostración y antecedentes por tipo dentro de la historia (CU-21, RN-17).
- Tests: de 119 a **196**, todos en verde. La app arranca. Recorrido automático de los pasos 1 a 5 sin errores.

**Qué quedó bloqueado.** Nada.

**Decisiones tomadas.** 38 en `DECISIONES_PENDIENTES.md`. Las que más conviene revisar:
- D-34: los rangos de signos vitales (de demostración, no clínicos).
- D-14 y D-15: datos de la persona en `usuario` y el nombre de usuario del personal escrito por quien lo crea.
- D-21 y D-22: qué datos de filiación son obligatorios y las listas cerradas (estado civil, instrucción, parentesco).
- D-27: qué rol mueve cada estado de la admisión.
- D-2: las cuentas `admision.a`, `medico.a`... siguen existiendo solo en los tests.

**Cómo probarlo.** Sección "Cómo probar el recorrido" del `README.md` (a mano) y `e2e/LEEME.md` (automático).

**Qué sigue (fuera de este checklist).** R3: el médico inicia la atención desde la cola (LISTO → EN_ATENCION), atención ampliada (enfermedad actual, funciones biológicas, examen por sistemas), pedido de exámenes (CU-22), resultados (CU-23) y cierre que pasa la admisión a ATENCION_CERRADA. El script `docs/script_sqlserver.sql` quedó desfasado respecto de las tablas nuevas (`admision`, `signos_vitales`, columnas de `usuario` y `paciente`).
