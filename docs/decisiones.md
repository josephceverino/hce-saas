# Decisiones del proyecto (DEC)
Decididas por Pablo (líder técnico) el 13/11/2026. Si algo cambia, se agrega una nueva DEC; no se reescribe la historia.

| ID | Decisión | Estado en el código |
|---|---|---|
| DEC-01 | Estructura | Hecho (Fase 0) |
| DEC-02 | RN-01 frente a multi-tenant | Hecho (Fase 0) |
| DEC-03 | Roles y permisos | Hecho (v3-base): `CodigoRol`, `Permiso`, `AutorizacionService`; aplicado a CU-03 |
| DEC-04 | Catálogo CIE-10 de demostración | CSV hecho (v3-base); cargador y validación pendientes (M-4, contrato en `CatalogoCie10Service`) |
| DEC-05 | Representante legal fuera de v4 | Documentado |
| DEC-06 | Auditoría mínima desde v3 | Hecho en CU-03, CU-05 y CU-06; falta CU-08 (M-2) |
| DEC-07 | Versión de Spring Boot y Java | Hecho (Fase 0) |
| DEC-08 | RF-07 solo con datos de demostración | Hecho (semilla `DatosDemoClinicos`); M-3 opcional |
| DEC-09 | Atención cerrada, enmienda en texto libre y quién modifica | Hecho (v3, `AtencionService`) |
| DEC-10 | Pantallas por rol y tablero con gráficos del Administrador del hospital | Documentado; menú por rol hecho (`MenuService`); tablero pendiente (v5, si hay tiempo) |
| DEC-11 | Panel "Hospitales" del Administrador de plataforma | Documentado; pendiente (v5, si hay tiempo) |
| DEC-12 | Usuarios y datos de demostración por rol | Hecho (v3) |
| DEC-15 | BCrypt y bloqueo por intentos fallidos (RN-12) | Hecho (v5): `ClaveUtil`, `AutenticacionService` |
| DEC-14 | Auditoría del admin del hospital, indicadores y límites | Hecho (v5): `AuditoriaService.consultar`, `IndicadoresService` |
| DEC-13 | Portal del paciente: solo atenciones cerradas, cuenta sin hospital y consentimiento ACTIVO/REVOCADO | CU-10 hecho (v4); CU-11 hecho (`feat/consentimiento`) |

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
- **Implementación:** la tabla `rol` no tiene columna `codigo`; el código va en `rol.nombre` (`ADMISION`) y el texto legible en `rol.descripcion` ("Admisión y archivo"). `/api/sesion` devuelve `rol` (texto) y `rolCodigo` (código). Un rol sin código válido no tiene ningún permiso.

## DEC-04: Catálogo CIE-10 de demostración (RN-10)
Un CSV pequeño (`cie10_demo.csv`: código, descripción) con códigos reales y frecuentes, más validación de formato (letra + 2 dígitos, opcional punto y 1 a 2 dígitos). Solo se incluyen códigos y descripciones conocidos con certeza. El archivo se marca como **"subconjunto de demostración, NO el catálogo oficial completo; verificar contra la fuente oficial"**. No se inventan códigos.

## DEC-05: Representante legal (RN-05)
Fuera de v4: v4 implementa solo al paciente titular. Se conserva la tabla `representante_legal` en el modelo de datos. RN-05 queda documentada como **"no implementada en el prototipo"**.

## DEC-06: Auditoría mínima desde v3 (RN-07, RNF-08, RF-13)
`AuditoriaService` mínimo desde v3: usuario, establecimiento, acción, entidad afectada, id y fecha y hora, escribiendo en `registro_auditoria`. Se invoca desde CU-03, CU-05, CU-06 y CU-08. En v5 se completa (pantalla del administrador, filtros, cobertura total). Así los casos de prueba de auditoría (CP-08) se ejecutan desde v3.
- **Implementación:** `registro_auditoria` no tiene columna de establecimiento en el modelo; el establecimiento se obtiene del usuario (`usuario.id_establecimiento`), sin cambiar el modelo. La auditoría corre en la transacción del caso de uso: si la operación falla, no queda registro (por eso el rechazo de DEC-02 no se audita todavía).

## DEC-07: Versión de Spring Boot y Java
Se verificó el `pom.xml` de la semilla: `spring-boot-starter-parent 3.3.5` y `java.version=17`. En la Fase 0 se migró a **Spring Boot 4.1.2** (última estable en Maven Central a la fecha; 4.2.0 sigue en RC) con `maven.compiler.release=21`. La migración compiló sin cambios de código. Plan B (3.5.x) no fue necesario.

## DEC-08: RF-07 (antecedentes y alergias) solo con datos de demostración
El flujo de registro de RF-07 no está especificado en `docs/casos_de_uso.md` (solo se muestran en CU-06). En el prototipo **no se construye pantalla ni servicio de alta**: los antecedentes y alergias se cargan como datos ficticios al arrancar (`DatosDemoClinicos`) y se muestran en la cabecera de la historia. La tarea M-3 queda **opcional**; solo se retoma si sobra tiempo y Pablo define el alcance por escrito. Las entidades `Antecedente` y `Alergia` y sus repositorios ya existen.

## DEC-09: Atención cerrada, enmienda en texto libre y quién puede modificar (RN-02, RN-03, RN-11)
- Una atención nace `ABIERTA` y pasa a `CERRADA`; cerrada no se modifica ni se elimina (RN-03). Se exige **al menos un diagnóstico** para cerrar.
- La corrección es una **enmienda en texto libre** (`motivo` y `contenido_corregido`), con autor y fecha puestos por el servidor. Solo se enmienda una atención **cerrada**.
- Una atención admite **varios diagnósticos** (`PRINCIPAL` o `SECUNDARIO`). La descripción sale del catálogo CIE-10, no del cliente (RN-10).
- Solo el **médico que registró** la atención agrega diagnósticos y tratamientos y la cierra (RN-11, precondición de CU-08). Cualquier **médico del hospital** puede registrar una enmienda; queda su autor (la tabla `enmienda` guarda su propio profesional).
- Una atención de otro hospital se trata como inexistente (404); una historia de otro hospital responde 403 con el mensaje de RN-06.
- Los valores `ABIERTA`/`CERRADA` y `PRINCIPAL`/`SECUNDARIO` **no estaban en los documentos**: se adoptan aquí como decisión. Contrato en `docs/contratos/atencion_historia.md`.

## DEC-10: Pantallas por rol y tablero con gráficos del Administrador del hospital (RF-19)
Se aprueba el mapa de pantallas por rol de `docs/diseno_ui.md`, con el menú decidido por el servidor. Se agrega: **Inicio por rol**, **Mi perfil** (todos), **Registrar paciente** como pantalla aparte y, en v5 si hay tiempo, un **tablero con gráficos** para el Administrador del hospital (atenciones por día, pacientes nuevos por semana, 5 diagnósticos más frecuentes, atenciones abiertas frente a cerradas, últimos accesos). Cubre RF-19 como tablero en lugar de pantalla de reportes. Reglas: cifras reales con filtro por hospital, SVG propio sin librerías, estado vacío claro. Apoyo al diagnóstico (A-05) queda fuera por no tener caso de uso.

## DEC-11: Panel "Hospitales" del Administrador de plataforma (RF-20)
Una tarjeta por hospital con plan, estado, n.° de usuarios y n.° de pacientes. Es la **única vista que cruza hospitales a propósito** y por eso se decide de forma explícita: **muestra solo totales, nunca datos clínicos ni identificadores de pacientes**. El usuario `plataforma` no pertenece a ningún hospital (`id_establecimiento` nulo) y no tiene permisos clínicos. Es la mejor demostración del SaaS multi-hospital. Pendiente en v5, si hay tiempo.

## DEC-12: Usuarios y datos de demostración por rol
Usuarios (clave `demo123`): `admision.a/b`, `medico.a/b`, `enfermera.a/b`, `admin.a` (ADMIN_HOSPITAL, hospital A) y `plataforma` (ADMIN_PLATAFORMA). Datos clínicos **ficticios** ("Paciente de prueba") sembrados por `DatosDemoClinicos`: 4 pacientes y 8 atenciones en el hospital A y 2 pacientes y 2 atenciones en el B, para que las pantallas y tableros tengan contenido. Se desactivan con `hce.demo-datos=false` (los tests lo tienen apagado) y no se duplican al reiniciar. Nunca datos reales.

## DEC-13: Portal del paciente (CU-10, CU-11; RN-04, RN-09)
- La cuenta `PACIENTE` **no pertenece a ningún hospital** (el paciente es global, DEC-02) y se liga a su paciente por `paciente.id_usuario`. El portal **no recibe ningún id**: la historia sale siempre de la cuenta en sesión (RN-04). Es solo lectura: el controlador solo tiene `GET`.
- El paciente ve **solo atenciones cerradas**: las abiertas son borradores del médico. Esto no está en los documentos: se adopta aquí. Cada consulta se audita (RN-07).
- Resumen (RF-16): JSON que la pantalla muestra e imprime (`GET /api/mi-historial/resumen`).
- RN-05 (representante legal) sigue sin implementarse (DEC-05).
- Consentimiento (CU-11): estados `ACTIVO` y `REVOCADO` (no estaban en los documentos). Revocar guarda la fecha; revocar dos veces es un error. Cuentas demo: `paciente.a` (Hospital A, DNI 90000001) y `paciente.b` (Hospital B, DNI 90000005), clave `demo123`.

## DEC-14: Auditoría del administrador del hospital e indicadores (RF-13, RF-19, RF-20; RN-06, RN-07)
- **Quién y qué ve:** solo el `ADMIN_HOSPITAL` consulta `GET /api/auditoria`, y solo lo de su hospital: lo hecho por usuarios de su hospital **o** lo que toque historias de su hospital (así aparece cuando un paciente consulta la suya). La auditoría es de solo lectura.
- **Qué se registra además:** inicio de sesión, intento fallido (solo de usuarios que existen, porque `registro_auditoria.id_usuario` es obligatorio) y cierre de sesión.
- **Límite conocido:** los accesos **denegados** (403) todavía no se registran. Hace falta guardarlos en una transacción aparte y conocer el usuario y la IP en el punto del rechazo; se deja como mejora.
- **Indicadores:** toda cifra sale de la base y se filtra por hospital. El Inicio por rol, el tablero del admin y el panel de plataforma están en `docs/contratos/auditoria_indicadores.md`. El panel de plataforma da **solo totales**; no trae el plan porque `suscripcion` no tiene entidad todavía.

## DEC-15: BCrypt y bloqueo por intentos fallidos (RN-12)
- **Dependencia nueva, justificada:** `spring-security-crypto` (solo el codificador BCrypt). No es Spring Security completo: el login por sesión (`SesionActual`) no cambia.
- **Claves:** se guardan con BCrypt (sal aleatoria). El SHA-256 de la demo anterior se sigue reconociendo solo para migrar: los usuarios demo pasan a BCrypt al arrancar y cualquier otro usuario con hash antiguo migra al iniciar sesión con su clave correcta. Una base creada antes de v5 se actualiza sola.
- **Bloqueo:** 5 intentos fallidos consecutivos dentro de 15 minutos bloquean la cuenta (configurable: `hce.login.max-intentos` y `hce.login.minutos-bloqueo`). Mientras está bloqueada, ni la clave correcta entra. **Se levanta sola** pasado el tiempo: no hay pantalla de administración de usuarios (RF-11) para desbloquear y la demo no puede quedar trabada. El bloqueo se calcula con los registros de auditoría (`LOGIN_FALLIDO` desde el último `INICIO_SESION`), así que **no hay columnas nuevas en `usuario`** y el modelo de datos no cambia. Un intento durante el bloqueo se registra como `LOGIN_BLOQUEADO` y no lo alarga.
- **Mensajes:** clave o usuario incorrecto da el mismo mensaje en los dos casos. El de cuenta bloqueada solo aparece para cuentas que existen (se acepta, es lo habitual).
- **Política de claves:** mínimo 6 caracteres con una letra y un número (`ClaveUtil.validarPolitica`). Está implementada y probada, pero **aún no se aplica en ninguna pantalla**, porque no hay cambio ni alta de claves hasta que exista la gestión de usuarios (RF-11).
