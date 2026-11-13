# Fichas de tareas (versión 2: roles por tramo vertical)

Cada persona es dueña de un tramo completo, no de tareas sueltas:
- **Alexander: frontend.** Todas las pantallas, el menú por rol y, al final, el UML de implementación.
- **Mattias: backend de apoyo y calidad.** Tests, y los casos de uso que Pablo le delega con contrato cerrado (CU-08, RF-07, módulo CIE-10, CU-11), más los casos de prueba de la Sesión 11.
- **Pablo: backend crítico, arquitectura, revisión y paquete portable.**

Quienes usan Gemini o ChatGPT pegan primero `docs/reglas_para_ia_externa.md` y después su ficha. Pablo revisa todo (`docs/equipo_y_flujo.md`).

## Cómo funciona una ficha
1. **Contrato primero.** Pablo deja en el repo, **antes** de que empieces, la interfaz, los DTO (records) y los tests que fallan. Tu trabajo es hacer que esos tests pasen sin cambiar el contrato. Si crees que el contrato está mal, avisa a Pablo; no lo cambies tú.
2. **Archivos exactos.** Solo puedes tocar los archivos que lista tu ficha. Esa lista es el permiso de la regla de dueños (`docs/equipo_y_flujo.md`). Un archivo que no esté en la lista, no se toca.
3. **Paso de la demo.** Cada ficha dice qué paso del guion de la demo cumple (tabla de abajo). Si tu trabajo no se ve en la demo, avisa.
4. **Plan B mínimo.** Es lo que Pablo hace si no llegas al punto de control. Existe para que la demo no dependa de una sola persona; no es un castigo.
5. **Punto de control a las 48 horas.** A las 48 horas de empezar, enséñale a Pablo lo que tengas (rama subida y captura) o avisa qué te bloquea. Un avance parcial a las 48 horas vale más que uno completo tarde.
6. **Preguntas de revisión.** Pablo las hará en el pull request. Si no puedes responderlas, aún no entiendes lo que entregas (regla de `docs/reglas_para_ia_externa.md`).

Definición de terminado: compila, `.\mvnw.cmd test` en verde con captura, cumple el criterio de la ficha, cita IDs (CU, RN, RF) en commits y comentarios, y no hay archivos fuera de la ficha. Rama por tarea: `feat/...`, `test/...`, `fix/...`.

## Guion de la demo (PROPUESTA: Pablo lo confirma)
Los pasos salen de los casos de uso especificados. El guion oficial es la tarea F3-5; hasta entonces estos números se usan para referenciar.

| Paso | Qué se muestra | IDs |
|---|---|---|
| D-1 | Iniciar sesión con las cuentas demo | CU-01 |
| D-2 | Admisión registra un paciente y se abre su historia | CU-03, RN-01 |
| D-3 | Mismo DNI: en el mismo hospital se muestra la historia existente; en otro hospital se rechaza | RN-01, RN-06, DEC-02 |
| D-4 | El médico busca y consulta una historia | CU-06, RN-06, RN-07 |
| D-5 | El médico registra una atención con diagnóstico CIE-10 y la cierra | CU-05, RN-03, RN-10 |
| D-6 | El médico registra tratamiento e indicaciones | CU-08 |
| D-7 | Una atención cerrada no se puede editar ni borrar | RN-03 |
| D-8 | Un médico del hospital B no ve la historia del hospital A | RN-06, RNF-06 |
| D-9 | El paciente ve su historial en solo lectura y su resumen | CU-10, RN-04 |
| D-10 | El paciente revoca un consentimiento y el acceso se corta | CU-11, RN-06, RN-09 |
| D-11 | (v5) La auditoría muestra quién accedió a qué | RN-07, RF-13 |

## Estado de las fichas anteriores
| Ficha anterior | Ahora | Estado |
|---|---|---|
| F0-1 Instalación del entorno | F0-1 (abajo) | Pendiente de Alexander y Mattias |
| F0-2 Leer el contexto | F0-2 (abajo) | Pendiente |
| F0-3 Base del proyecto (Pablo) | F0-3 | **Hecha**, en la rama `fase0/base`, pendiente de tu confirmación y merge |
| F1-1 Backend CU-06, 05, 08 (Pablo) | F1-1 | Pendiente (v3) |
| F1-2 Login con el nuevo diseño | — | **Hecha por Pablo** en la Fase 0 (`static/login/`, `static/css/estilo.css`). Alexander la pule dentro de A-1 |
| F1-3 Tests de RN-01 y validaciones | — | **Hecha por Pablo** (`RegistroPacienteServiceTest`, 5 tests, incluye DEC-02). Mattias continúa con M-1 |
| F1-4 Pantallas P-02 y P-04 | A-2 y A-4 | Pendiente |
| F2-2 P-05 | A-5 | Pendiente (v4) |
| F2-3 P-06 | M-5 | Pendiente (v4), ahora parte de CU-11 completo |
| F2-4 Tests RN-04 y revocación | M-5 y M-6 | Pendiente |
| F3-2 Pantalla de auditoría | A-7 | Pendiente, solo si hay tiempo |

---

## FASE 0 (esta semana)

### F0-1 · Instalación del entorno (Alexander y Mattias)
- **Antes de clonar:** Pablo debe haber hecho el merge de `fase0/base` a `main`. Hasta entonces `main` tiene solo la base antigua y no arranca.
- Instalar: JDK 21 a 25, IntelliJ IDEA, **Git for Windows**, cuenta de GitHub (enviar el usuario a Pablo para la invitación).
- Clonar el repo, abrir la **raíz** (la carpeta con `pom.xml`) en IntelliJ, ejecutar `.\mvnw.cmd test` y luego `.\mvnw.cmd spring-boot:run`, abrir `http://localhost:8090`.
- IntelliJ puede avisar de `referencia/semilla-v2/pom.xml`: ignorarlo (es la semilla antigua, solo consulta).
- Aceptación: captura del login funcionando y de los 8 tests en verde. Si falla, enviar el error completo.
- Punto de control a las 48 horas: si no corre, avisa; no insistas solo.

### F0-2 · Leer el contexto (Mattias: hoy y mañana; Alexander: cuando pueda)
- Leer `ONBOARDING_MATTIAS.md`, `docs/requisitos.md` (RN-01 a RN-12), `docs/casos_de_uso.md` (CU-03 y CU-06), `docs/decisiones.md` y `docs/roles_permisos.md`. Alexander además lee `docs/diseno_ui.md`.
- Aceptación: explicar en 3 líneas qué hacen RN-01, RN-03 y RN-06, y qué decidió DEC-02.

### F0-3 · Base del proyecto (Pablo) — HECHA
Repo en la raíz, Spring Boot 4.1.2, nivel 21, puerto 8090, H2 en archivo, frontend dividido, 8 tests en verde, DEC-01 a DEC-07. Pendiente: tu confirmación y el merge.

---

## ALEXANDER · Frontend
Zona: `src/main/resources/static/`. Regla general: sin CDN, sin frameworks, clases `hce-` de `estilo.css`, textos en español, nada de `innerHTML` con datos del usuario (usar `textContent`, como en `comun.js`). Cuando una pantalla necesite un componente que no existe, lo pide a Pablo; no edita `estilo.css` por su cuenta salvo que la ficha lo diga.

### A-1 · Pulido de P-01 Login (antes F1-2) — Hecha por Pablo; Alexander la revisa
- **Archivos:** `static/login/index.html`, `static/login/login.js`. Nada más.
- **Qué hace:** revisar el login en laptop y proyector (contraste, tamaños), responder a Pablo con una lista de mejoras; aplicar solo las acordadas.
- **Demo:** D-1. **Plan B:** queda como está (ya funciona).
- **Control 48 h:** lista de mejoras enviada.
- **Preguntas:** ¿qué devuelve `/api/login` si la clave es mala y qué código HTTP? ¿Por qué el mensaje no dice si falló el usuario o la clave?

### A-2 · P-02 Buscar paciente, con el endpoint que ya existe (antes parte de F1-4)
- **Contrato (ya existe, no cambia):** `GET /api/historias` devuelve la lista de `HistoriaResumen` del hospital del usuario: `idHistoriaClinica, nroHc, tipoDocumento, nroDocumento, paciente, fechaApertura, establecimiento, estado` (ver `presentacion/HistoriaResumen.java`). `POST /api/pacientes` responde 201 (se abrió la historia), 200 (ya existía en tu hospital) o 409 (está en otro establecimiento, DEC-02).
- **Qué hace:** en `pacientes/`, la tabla gana un buscador por DNI o nombre (filtro en el navegador sobre la lista recibida), paginación, y chips de estado. El formulario de registro se mueve a su propia pantalla `pacientes/registrar.html` (menú de Admisión: Pacientes, Registrar paciente). Los 3 estados del `POST` se muestran con mensajes distintos.
- **Limitación a respetar:** el filtro es de cliente, sobre las historias de **su propio hospital**. La búsqueda en el servidor (RF-02) la hace Pablo en v3; entonces solo se cambia de dónde sale la lista.
- **Archivos exactos:** `static/pacientes/index.html`, `static/pacientes/pacientes.js`, `static/pacientes/registrar.html` (nuevo), `static/pacientes/registrar.js` (nuevo).
- **Demo:** D-2, D-3 (mensajes de 200 y 409). **Criterio:** buscar por DNI parcial y por apellido; pantalla vacía con mensaje claro; sin datos de otro hospital jamás.
- **Plan B (Pablo):** deja la pantalla actual (tabla y formulario juntos), que ya funciona.
- **Control 48 h:** buscador funcionando con datos de prueba en una rama subida.
- **Preguntas:** ¿por qué el filtro de cliente no rompe RN-06? ¿Qué pasaría si dos hospitales tuvieran el mismo paciente? ¿Qué muestra la pantalla si el servidor responde 409?

### A-3 · P-03 Registrar atención y tratamiento (CU-05, CU-08)
- **Depende de:** F1-1 (Pablo). No empieza hasta que Pablo avise que el contrato está en el repo.
- **Contrato (lo deja Pablo en `docs/contratos/atencion.md` + código):** endpoints de crear atención, cerrar atención y registrar tratamiento; DTO de entrada y salida (motivo, anamnesis, examen físico, diagnóstico con código CIE-10, tratamiento: indicación, medicamento, dosis, duración); códigos de error (403 sin permiso, 404 historia de otro hospital, 409 atención cerrada). Las rutas exactas las fija Pablo al dejar el contrato. Junto al contrato, un JSON de ejemplo para trabajar sin esperar al backend.
- **Archivos exactos:** `static/atencion/index.html`, `static/atencion/atencion.js` (nuevos).
- **Demo:** D-5, D-6. **Criterio:** formulario en tarjetas; el botón "Cerrar atención" pide confirmación (modal); tras cerrar, los campos quedan solo lectura y no hay botón de editar (RN-03); errores del servidor se muestran en español.
- **Plan B (Pablo):** formulario mínimo de una sola tarjeta sin modal, con `confirm()` del navegador.
- **Control 48 h:** pantalla con datos del JSON de ejemplo, aunque no esté conectada.
- **Preguntas:** ¿cómo evitas que el usuario edite una atención cerrada, y por qué eso no basta como seguridad? ¿Dónde se valida el CIE-10, en el JS o en el servidor? ¿Qué pasa si se cae la red al cerrar?

### A-4 · P-04 Historia clínica (CU-06)
- **Depende de:** F1-1. Mismo mecanismo de contrato (`docs/contratos/historia.md`: cabecera del paciente, atenciones con estado, antecedentes y alergias).
- **Archivos exactos:** `static/historia/index.html`, `static/historia/historia.js` (nuevos; ya existe la carpeta con `.gitkeep`).
- **Demo:** D-4, D-7, D-8. **Criterio:** cabecera con iniciales en círculo, DNI, edad, alergias como chips rojos; línea de tiempo de atenciones (cerrada = candado y sin botón de editar); si el servidor niega el acceso (otro hospital), mensaje claro y ningún dato.
- **Plan B (Pablo):** tabla simple de atenciones sin línea de tiempo.
- **Control 48 h:** cabecera y línea de tiempo con JSON de ejemplo.
- **Preguntas:** ¿de dónde sale la edad y por qué no se guarda? ¿Qué debe ver el usuario si pide la historia de otro hospital? ¿Por qué la pantalla no decide qué roles pueden ver qué?

### A-5 · Menú por rol (`diseno_ui.md`)
- **Contrato:** `GET /api/sesion` ya devuelve `usuario, rol, establecimiento`. Pablo le agrega el **código de rol** (`MEDICO`, `ADMISION`, etc., DEC-03) cuando esté la `AutorizacionService`; hasta entonces el nombre actual ("Personal administrativo") se trata como Admisión.
- **Archivos exactos:** `static/js/comun.js` (solo la función del menú, **excepción acordada** a la regla de dueños), y una página de inicio por rol: `static/inicio/index.html`, `static/inicio/inicio.js`.
- **Demo:** D-1 y D-2 en adelante (cada rol ve su menú). **Criterio:** Admisión ve Inicio, Pacientes, Registrar paciente; Médico y Enfermería ven Inicio, Pacientes, Historia clínica, Registrar atención (solo Médico); Paciente ve Mi historial y Consentimiento. **Solo aparece lo que existe**; nada de calendario, citas, chat, facturación. Las cifras del Inicio salen de la base de datos real; si el endpoint aún no existe, la tarjeta no se muestra.
- **Plan B (Pablo):** menú fijo, el actual.
- **Control 48 h:** menú por rol funcionando con un rol simulado.
- **Preguntas:** ¿por qué ocultar un enlace no es seguridad? ¿Qué pasa si un Admisión escribe la URL de Registrar atención?

### A-6 · P-05 Mi historial, solo lectura (CU-10, antes F2-2)
- **Depende de:** F2-1 (Pablo, v4). Contrato en `docs/contratos/portal_paciente.md`.
- **Archivos exactos:** `static/mi-historial/index.html`, `static/mi-historial/mi-historial.js`.
- **Demo:** D-9. **Criterio:** variante simplificada de A-4 sin ningún botón de edición; botón "Ver resumen" (HTML imprimible); si pide una historia ajena por URL, ve un mensaje de acceso denegado.
- **Plan B (Pablo):** página de resumen en texto plano.
- **Control 48 h:** pantalla con el JSON de ejemplo.
- **Preguntas:** ¿cómo garantiza el servidor que el paciente ve solo lo suyo (RN-04)? ¿Qué muestra el portal si el paciente es menor de edad (RN-05 no está implementada)?

### A-7 · Reportes de atenciones (RF-19) — solo si sobra tiempo
- **Es lo primero que se recorta** (`PLAN_CALENDARIO.md`). Si Pablo avisa que no hay tiempo, no se empieza.
- **Archivos exactos:** `static/reportes/index.html`, `static/reportes/reportes.js`.
- **Demo:** ninguno (opcional). **Plan B:** no hay; se recorta. **Control 48 h:** no aplica hasta que Pablo lo active.

### A-8 · UML de implementación (Sesión 12)
- **Cuándo:** semana del 16 al 22 de noviembre, sobre el **código real**, no sobre lo planeado.
- **Archivos exactos:** nuevos `.puml` y `.png` en `docs/diagramas/` con prefijo `impl_` (por ejemplo `impl_clases.puml`); no modificar los diagramas `3.5.x`.
- **Demo:** ninguno (es entrega académica). **Criterio:** clases, paquetes y una secuencia (CU-03) que coincidan con lo que existe en `src/main/java`; cada diferencia con el diseño original se anota en un párrafo.
- **Plan B (Pablo):** diagrama de paquetes y capas únicamente.
- **Control 48 h:** borrador del diagrama de paquetes.
- **Preguntas:** ¿en qué se diferencia el código real del diseño original? ¿Por qué `presentacion` no depende de `persistencia`?

---

## MATTIAS · Backend de apoyo y calidad (empieza el domingo 15/11)
Zona: `src/test/` y los archivos que cada ficha lista en `dominio/`, `aplicacion/`, `persistencia/`, `presentacion/`. No tocar `pom.xml` ni agregar dependencias.

### M-1 · Tests nuevos (empieza aquí)
- **Contexto:** ya existen `AutenticacionServiceTest` (3 tests) y `RegistroPacienteServiceTest` (5 tests). No se reescriben; se agregan.
- **Contrato (lo deja Pablo):** una lista de los casos a cubrir con su ID de prueba, en `docs/pruebas/plantilla_casos_prueba.md` (columna Cobertura). Pablo deja escritos los métodos con `@Test` y `fail("pendiente")` para que tú los completes.
- **Casos:** (a) CP-04: correo mal escrito y fecha de nacimiento futura, usando el validador de `jakarta.validation` sobre `PacienteRequest` (sin MockMvc, que exige una dependencia nueva); (b) usuario con estado distinto de `ACTIVO` no entra; (c) `ClaveUtil` verifica `demo123` y rechaza otra clave; (d) campos obligatorios vacíos de `PacienteRequest`.
- **Archivos exactos:** `src/test/java/pe/edu/ucv/hce/presentacion/PacienteRequestValidacionTest.java` (nuevo), `src/test/java/pe/edu/ucv/hce/aplicacion/AutenticacionServiceTest.java` (agregar), `src/test/java/pe/edu/ucv/hce/aplicacion/ClaveUtilTest.java` (nuevo).
- **Demo:** apoya D-1 y D-2 (calidad). **Criterio:** `.\mvnw.cmd test` en verde con captura; cada test cita su CP y su RN.
- **Plan B (Pablo):** completa solo (b) y (c).
- **Control 48 h:** (a) funcionando.
- **Preguntas:** ¿por qué el mensaje de login es el mismo si falla el usuario o la clave? ¿Qué diferencia hay entre un test que valida `PacienteRequest` y uno que prueba `RegistroPacienteService`? ¿Qué pasaría con el test si el correo es válido pero la fecha es de hoy (`@Past`)?

### M-2 · Backend CU-08 Tratamiento e indicaciones
- **Depende de:** F1-1 (Pablo deja la entidad `Atencion`, `AtencionService` y las reglas de cierre).
- **Contrato (Pablo, antes de empezar):** interfaz `TratamientoService`, DTO `TratamientoRequest` y `TratamientoResponse`, y los tests **que fallan**: CP-14 (atención cerrada rechaza el tratamiento) y CP-15 (se vincula a la atención abierta propia).
- **Archivos exactos:** `dominio/Tratamiento.java`, `persistencia/TratamientoRepository.java`, `aplicacion/TratamientoService.java`, `presentacion/TratamientoController.java`, `presentacion/TratamientoRequest.java`, `presentacion/TratamientoResponse.java`, y el test del contrato. Las tablas siguen `docs/modelo_datos.md` (`tratamiento`: indicación, medicamento, dosis, duración).
- **Reglas:** RN-02, RN-03, RN-11; el controlador solo llama al servicio; auditoría vía `AuditoriaService` (DEC-06).
- **Demo:** D-6. **Plan B (Pablo):** tratamiento como texto libre dentro de la atención.
- **Control 48 h:** CP-15 en verde.
- **Preguntas:** ¿por qué se rechaza el tratamiento en una atención cerrada y quién lo comprueba (¿el JS o el servicio?)? ¿Qué impide que el médico B registre tratamiento en una atención del médico A? ¿Dónde se aplica el filtro por hospital?

### M-3 · RF-07 Antecedentes y alergias
- **Pregunta abierta antes de empezar:** el flujo de registro de RF-07 **no está especificado** en `docs/casos_de_uso.md` (solo se muestran en CU-06). Pablo define el alcance mínimo y lo deja por escrito en el contrato; hasta entonces esta ficha no se inicia. Roles según `docs/roles_permisos.md`: `MEDICO` y `ENFERMERIA`.
- **Contrato (Pablo):** DTO de alta de antecedente y de alergia, interfaz del servicio y tests que fallan (rol sin permiso rechazado; historia de otro hospital rechazada).
- **Archivos exactos:** `dominio/Antecedente.java`, `dominio/Alergia.java`, `persistencia/AntecedenteRepository.java`, `persistencia/AlergiaRepository.java`, `aplicacion/AntecedenteAlergiaService.java`, `presentacion/AntecedenteAlergiaController.java` y sus DTO.
- **Demo:** alimenta D-4 (chips de alergias en la cabecera). **Plan B (Pablo):** datos de ejemplo cargados al arrancar para que la cabecera tenga qué mostrar.
- **Control 48 h:** entidades y repositorios compilando.
- **Preguntas:** ¿por qué `registro_auditoria` no necesita FK pero estas tablas sí? ¿Qué rol puede registrar una alergia y de dónde sale esa regla?

### M-4 · Módulo CIE-10 (RN-10, DEC-04)
- **Contrato (Pablo):** `cie10_demo.csv` (**lo entrega Pablo**, con solo códigos reales y verificados; nadie lo amplía con códigos "de memoria" ni con la IA) y la interfaz `CatalogoCie10Service` (`esValido(String codigo)`, `descripcion(String codigo)`); tests que fallan: formato (letra, 2 dígitos, opcional punto y 1 o 2 dígitos), código inexistente, vacío (CP-13).
- **Qué haces tú:** implementar el cargador del CSV y la validación de formato, y hacer pasar los tests.
- **Archivos exactos:** `aplicacion/CatalogoCie10Service.java`, `src/main/resources/cie10_demo.csv` (solo lectura para ti), `src/test/java/.../Cie10Test.java`.
- **El archivo debe decir en su cabecera:** "subconjunto de demostración, NO el catálogo oficial completo; verificar contra la fuente oficial".
- **Demo:** D-5. **Plan B (Pablo):** validar solo el formato, sin catálogo.
- **Control 48 h:** validación de formato con tests en verde.
- **Preguntas:** ¿por qué el CIE-10 se valida en la aplicación y no en la base de datos? ¿Qué pasa si el catálogo cambia? ¿Qué riesgo hay en que una IA complete la lista?

### M-5 · CU-11 completo: backend, tests y P-06 (antes F2-3 y F2-4)
- **Depende de:** F2-1 (Pablo, v4): usuario `PACIENTE` y portal con sesión.
- **Contrato (Pablo):** interfaz `ConsentimientoService` (otorgar, revocar, listar activos), DTO, la tabla `consentimiento_acceso` del modelo, y los tests que fallan: CP-19 (otorgar registra fecha y alcance) y CP-20 (revocar corta el acceso de inmediato).
- **Archivos exactos:** `dominio/ConsentimientoAcceso.java`, `persistencia/ConsentimientoAccesoRepository.java`, `aplicacion/ConsentimientoService.java`, `presentacion/ConsentimientoController.java`, DTO, tests, `static/consentimiento/index.html`, `static/consentimiento/consentimiento.js`. La pantalla lista autorizaciones con interruptores y fecha.
- **Reglas:** RN-06, RN-09, RN-07; el paciente solo ve y cambia **sus** consentimientos (RN-04).
- **Demo:** D-10. **Plan B (Pablo):** botones Otorgar y Revocar en una tabla, sin interruptores.
- **Control 48 h:** CP-19 en verde y la pantalla con datos de ejemplo.
- **Preguntas:** ¿cómo se asegura que revocar corta el acceso "de inmediato" (qué consulta el servicio en cada acceso)? ¿Qué impide que el paciente A cambie el consentimiento del paciente B? ¿Qué se guarda para poder demostrar la finalidad (RN-09)?

### M-6 · Casos de prueba de la Sesión 11
- **Cuándo:** desde la congelación del código (viernes 4 de diciembre) hasta el 9 de diciembre, con la app ya empaquetada.
- **Qué hace:** ejecutar cada caso de `docs/pruebas/plantilla_casos_prueba.md`, llenar resultado real, estado, fecha y responsable, y adjuntar evidencia (captura o nombre del test). **Nunca marcar "Aprobado" sin evidencia**; un caso que no se pudo ejecutar queda "No ejecutado" con el motivo. Medir y registrar el tiempo real de CP-09.
- **Archivos exactos:** `docs/pruebas/plantilla_casos_prueba.md` (solo las columnas de resultados), `docs/pruebas/evidencias/` (capturas).
- **Demo:** respalda todos los pasos. **Plan B (Pablo):** ejecuta él los casos críticos (CP-07, CP-12, CP-17, CP-20, CP-21).
- **Control 48 h:** la mitad de los casos ejecutados o los bloqueos escritos.
- **Preguntas:** ¿qué caso falló y qué hiciste con el fallo? ¿Cuál es la diferencia entre un caso con test automático y uno manual? ¿Por qué CP-06 sigue pendiente?

---

## PABLO · Backend crítico y cierre

### F1-1 · Backend CU-06, CU-05 (v3)
- Ver prompt 2 de `PROMPTS.md`. IDs: RN-02, 03, 06, 07, 10, 11; DEC-03, 04, 06.
- **Antes de que Alexander y Mattias empiecen A-3, A-4, M-2, M-3, M-4:** dejar en el repo los contratos (`docs/contratos/`), los DTO, las interfaces y los tests que fallan. Esa es la condición para que trabajen en paralelo.
- Incluye `AutorizacionService` con constantes de permiso (DEC-03), `AuditoriaService` mínimo (DEC-06) y los usuarios demo `medico.a` y `medico.b`.

### F2-1 · Backend CU-10 (v4)
- Portal del paciente y usuarios `PACIENTE` demo. RN-04; RN-05 queda documentada como no implementada (DEC-05).

### F3-1 · Auditoría completa, roles y BCrypt (v5)
- Completa `AuditoriaService` y migra los permisos a `rol_permiso`. BCrypt y bloqueo por intentos se recortan primero si el tiempo se aprieta.

### F3-3 · Casos de prueba con evidencia
- Ejecuta Mattias (M-6); Pablo y Alexander cubren los suyos y revisan.

### F3-4 · Paquete portable y guía
- Prompt 5 de `PROMPTS.md`. Prueba en otra PC con Windows antes del 5 de diciembre.

### F3-5 · Guion y ensayo de la demo
- Todos. Se confirma la tabla D-1 a D-11 y se ensaya al menos 2 veces.
