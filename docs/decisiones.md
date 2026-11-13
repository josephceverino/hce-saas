# Decisiones del proyecto (DEC)
Decididas por Pablo (líder técnico) el 13/11/2026. Si algo cambia, se agrega una nueva DEC; no se reescribe la historia.

| ID | Decisión | Estado en el código |
|---|---|---|
| DEC-01 | Estructura | Hecho (Fase 0) |
| DEC-02 | RN-01 frente a multi-tenant | Hecho (Fase 0) |
| DEC-03 | Roles y permisos | Documentado; se implementa en v3 |
| DEC-04 | Catálogo CIE-10 de demostración | Pendiente (v3) |
| DEC-05 | Representante legal fuera de v4 | Documentado |
| DEC-06 | Auditoría mínima desde v3 | Pendiente (v3) |
| DEC-07 | Versión de Spring Boot y Java | Hecho (Fase 0) |

## DEC-01: Ubicación y estructura
La raíz del proyecto es `hce-saas/`. `CLAUDE.md`, `PROMPTS.md`, `PLAN_CALENDARIO.md`, `FICHAS_TAREAS.md`, `ONBOARDING_MATTIAS.md`, `GIT_SETUP.md`, `README.md`, `.gitignore` y `docs/` viven en la raíz, junto con `pom.xml`, `mvnw` y `src/`. El contenido de `hce-kit-v2/` se movió con `git mv` (historial conservado). La semilla original queda como **referencia** en `referencia/semilla-v2/` hasta terminar la migración; no es parte del build.

## DEC-02: Documento ya registrado en otro establecimiento (RN-01, RN-06; extensión de CU-03)
El paciente es global (`tipo_documento` + `nro_documento` únicos) y tiene una sola historia clínica (RN-01).
- **Mismo hospital:** se mantiene el flujo alternativo de CU-03: el sistema muestra la historia existente y no crea otra (HTTP 200 en lugar de 201).
- **Otro hospital:** se **rechaza** el registro (HTTP 409) con el mensaje exacto: *"El paciente ya cuenta con una historia clínica en otro establecimiento. Para acceder a ella se requiere un intercambio autorizado por el paciente (RN-06)."* No se muestra ningún dato personal ni clínico del paciente.
- El intercambio real (RF-18, CU-13) queda **fuera del alcance de v3 a v5**; solo se rechaza con ese mensaje.
- Tests: `RegistroPacienteServiceTest` (`mismoHospitalDevuelveLaHistoriaExistente`, `otroHospitalEsRechazadoSinRevelarDatos`). Caso de prueba CP-23.

## DEC-03: Roles y permisos (RF-11, RF-12; RN-02)
Códigos de rol en mayúsculas: `PACIENTE` (A-01), `MEDICO` (A-02), `ENFERMERIA` (A-03), `ADMISION` (A-04), `APOYO_DIAGNOSTICO` (A-05), `ADMIN_HOSPITAL` (A-06), `ADMIN_PLATAFORMA` (A-07). Los permisos salen de la columna "Actor" de la tabla de RF (`docs/requisitos.md`); matriz en `docs/roles_permisos.md`.
- **v3:** la verificación por rol vive en `AutorizacionService` con constantes de permiso.
- **v5:** se migra a la tabla `rol_permiso`.

## DEC-04: Catálogo CIE-10 de demostración (RN-10)
Un CSV pequeño (`cie10_demo.csv`: código, descripción) con códigos reales y frecuentes, más validación de formato (letra + 2 dígitos, opcional punto y 1 a 2 dígitos). Solo se incluyen códigos y descripciones conocidos con certeza. El archivo se marca como **"subconjunto de demostración, NO el catálogo oficial completo; verificar contra la fuente oficial"**. No se inventan códigos.

## DEC-05: Representante legal (RN-05)
Fuera de v4: v4 implementa solo al paciente titular. Se conserva la tabla `representante_legal` en el modelo de datos. RN-05 queda documentada como **"no implementada en el prototipo"**.

## DEC-06: Auditoría mínima desde v3 (RN-07, RNF-08, RF-13)
`AuditoriaService` mínimo desde v3: usuario, establecimiento, acción, entidad afectada, id y fecha y hora, escribiendo en `registro_auditoria`. Se invoca desde CU-03, CU-05, CU-06 y CU-08. En v5 se completa (pantalla del administrador, filtros, cobertura total). Así los casos de prueba de auditoría (CP-08) se ejecutan desde v3.

## DEC-07: Versión de Spring Boot y Java
Se verificó el `pom.xml` de la semilla: `spring-boot-starter-parent 3.3.5` y `java.version=17`. En la Fase 0 se migró a **Spring Boot 4.1.2** (última estable en Maven Central a la fecha; 4.2.0 sigue en RC) con `maven.compiler.release=21`. La migración compiló sin cambios de código. Plan B (3.5.x) no fue necesario.
