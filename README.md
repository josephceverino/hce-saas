# Plataforma HCE (SaaS de historia clínica electrónica)

## Cómo probar (Fase 0)
Requisito: JDK 21 o superior (el código compila a nivel 21). Maven viene incluido (`mvnw`). La primera vez necesita internet para bajar dependencias.
1. `./mvnw test` (en Windows también `mvnw.cmd test`): deben pasar todos los tests (los `@Disabled` de M-1 salen como omitidos).
2. `./mvnw spring-boot:run` y abrir `http://localhost:8090`.
3. Entrar con `admision.a` / `demo123` (Hospital Demo A) y registrar un paciente con un DNI de 8 dígitos.
4. Repetir el mismo DNI en el mismo hospital: se muestra la historia existente, no se crea otra (RN-01).
5. Cerrar sesión, entrar con `admision.b` / `demo123` y registrar ese mismo DNI: se rechaza con el mensaje de DEC-02 y no se ve ningún dato del paciente (RN-06). La lista de B está vacía.
6. Detener y volver a arrancar: los datos siguen ahí (H2 en archivo `./data/hce`, ignorado por Git).

## Cómo probar (v3-base: roles y auditoría)
1. `./mvnw test`: 18 tests en verde y 9 omitidos (`@Disabled("pendiente M-1")`, son de Mattias).
2. Entrar con `medico.a` / `demo123` e intentar registrar un paciente: se rechaza con "No tienes permiso para realizar esta acción." (403). El médico sí ve la lista de su hospital.
3. Entrar con `admision.a`: el rol en el menú dice "Admisión y archivo". Registrar funciona como antes y cada registro deja una fila en `registro_auditoria`.
4. Una base creada en la Fase 0 se actualiza sola al arrancar (roles nuevos y médicos demo); no hace falta borrar `./data`.

## Cómo probar (v3: historia y atención, CU-06 y CU-05)
1. `./mvnw test`: pasan los tests de `HistorialServiceTest` (5) y `AtencionServiceTest` (13). Quedan omitidos los contratos de Mattias (CIE-10 y tratamiento).
2. `./mvnw spring-boot:run`. Con `admision.a` registrar un paciente (queda la historia HC-000001).
3. Entrar con `medico.a`. Contrato de la API: `docs/contratos/atencion_historia.md`. Hoy se prueba con la API (las pantallas son de Alexander): `GET /api/historias/1`, `POST /api/atenciones`, `POST /api/atenciones/1/cierre`.
4. Con `medico.b` (otro hospital): `GET /api/historias/1` da 403 (RN-06) y `GET /api/atenciones/1` da 404.
5. Con `admision.a`: `GET /api/historias/1` da 403 (RN-02).
6. Agregar diagnóstico necesita el catálogo CIE-10 (tarea M-4); hasta entonces responde 500.

## Cómo probar (v3: usuarios demo, menú por rol y datos de prueba)
1. `./mvnw test`: se suman `MenuServiceTest` (7) y `DatosDemoClinicosTest` (2).
2. Al arrancar la app se cargan datos **ficticios** ("Paciente de prueba"): 4 pacientes y 8 atenciones en el Hospital A, 2 pacientes y 2 atenciones en el B. Se desactivan con `hce.demo-datos=false` y no se duplican al reiniciar.
3. Usuarios (clave `demo123`): `admision.a/b`, `medico.a/b`, `enfermera.a/b`, `admin.a` y `plataforma`.
4. `POST /api/login` y `GET /api/sesion` devuelven `menu` (lista de `clave`, `titulo`, `ruta`) según el rol, más `ultimoAcceso`. Entrar con `medico.a`: `GET /api/historias` muestra 4 historias; con `medico.b`, 2 (RN-06).

## Cómo probar (v4: portal del paciente, CU-10)
1. `./mvnw test`: se suman los 8 tests de `PortalPacienteServiceTest`; el contrato de CU-11 (`ConsentimientoServiceTest`) sale omitido (es de Mattias, M-5).
2. Arrancar la app y entrar con `paciente.a` / `demo123`: el menú es Mi historial, Consentimiento y Mi perfil. Por API: `GET /api/mi-historial` (mi historia, solo atenciones cerradas) y `GET /api/mi-historial/resumen`.
3. Con `paciente.a` probar lo que NO debe: `GET /api/historias/1`, `GET /api/historias` y `POST /api/atenciones` responden 403 (RN-04). `paciente.b` ve solo su propia historia (Hospital B).
4. Contratos: `docs/contratos/portal_paciente.md` y `docs/contratos/consentimiento.md`.

Estructura: `src/` es el proyecto real; `referencia/semilla-v2/` es la semilla original (solo consulta). Decisiones en `docs/decisiones.md`; roles en `docs/roles_permisos.md`.

---

# Kit original para iniciar el software (versión 2)
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
