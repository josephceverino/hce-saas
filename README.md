# Plataforma HCE (SaaS de historia clínica electrónica)

## Cómo entrar (plan v3: sin cuentas de demostración)
La aplicación ya **no trae cuentas ni pacientes de demostración**. Al arrancar por primera vez crea un único **administrador de plataforma**:

| Usuario | Clave temporal | Qué pasa al entrar |
|---|---|---|
| `admin` | `Temporal2026` | El sistema obliga a cambiar la clave en el primer ingreso (RN-14, CU-18) |

- Se configuran en `src/main/resources/application.properties` con `hce.admin-inicial.usuario` y `hce.admin-inicial.clave` (la clave debe tener al menos 6 caracteres con una letra y un número). En el paquete portable también se pueden pasar al lanzarlo, por ejemplo `--hce.admin-inicial.clave=OtraClave1`.
- Solo se crea si no existe ningún administrador de plataforma: reiniciar la aplicación nunca pisa una clave ya cambiada.
- Para empezar de cero, borra la carpeta `data/` con la aplicación cerrada.
- Las cuentas `admision.a`, `medico.a`, etc. (clave `demo123`) que mencionan las secciones antiguas de este README **solo existen en los tests** (`hce.demo-usuarios=true` en `src/test/resources`).
- Avance del plan v3: `PROGRESO.md`. Decisiones por revisar: `DECISIONES_PENDIENTES.md`.

## Cómo probar el recorrido (plan v3, pasos 1 a 9 de `docs/recorrido.md`)
1. Borra `data/` (con la app cerrada) y arranca: `.\mvnw.cmd spring-boot:run`. Abre `http://localhost:8090`.
2. **Paso 1:** entra con `admin` / `Temporal2026`, cambia la clave y en **Hospitales** da de alta un hospital con su administrador. Anota la clave temporal de la constancia (se muestra una sola vez).
3. **Paso 2:** entra con el administrador del hospital, cambia su clave y en **Usuarios** crea a admisión, enfermería, médico (con colegiatura) y apoyo al diagnóstico.
4. **Paso 3:** entra con admisión y en **Registrar paciente** completa la filiación. Se abre la historia y sale la constancia del portal (usuario = documento).
5. **Paso 4:** en **Pacientes**, botón **Admitir**: consulta externa o emergencia. **Pacientes del día** muestra la cola completa.
6. **Paso 5:** entra con enfermería: en **Cola de pacientes** la emergencia va primero. **Tomar paciente** abre la historia con la sección de enfermería: registra signos vitales (un valor imposible se rechaza), antecedentes y alergias, y confirma "listo para atención". El médico lo ve en su cola.
7. **Paso 6:** entra con el médico: en **Cola de pacientes**, **Iniciar atención** sobre el paciente listo. Completa motivo, enfermedad actual, anamnesis, funciones biológicas, examen físico y por sistemas; agrega un diagnóstico CIE-10 (ej. `J02.9`), un tratamiento y **Solicitar examen auxiliar**.
8. **Paso 7:** entra con apoyo al diagnóstico (cambia su clave): en **Exámenes** ve el pedido pendiente y pulsa **Registrar resultado**.
9. **Paso 8:** el médico abre la atención, ve el resultado y la cierra: la admisión pasa a "atención cerrada" y ya no se edita (solo enmienda).
10. **Paso 9:** la admisión restablece la clave del paciente (Pacientes → Restablecer clave) y le da la temporal. El paciente entra, la cambia y en **Mi historial** ve solo atenciones cerradas (con exámenes y resultado, sin signos vitales); **Ver resumen** lo imprime; en **Consentimiento** otorga y revoca. El administrador del hospital lo ve en **Auditoría**.
- **Restablecer clave (CU-26):** el administrador del hospital lo hace desde **Usuarios**, la plataforma desde **Hospitales** (al administrador) y la admisión desde **Pacientes** (cuenta del portal). La clave temporal se muestra una sola vez.
- Automático: `cd e2e`, `npm install` y `npm run recorrido -- 9` con la app en el puerto 8191 y base nueva (ver `e2e/LEEME.md`). Capturas en `docs/capturas/`.

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

## Cómo probar (pantallas 1: menú por rol, Inicio y Pacientes; A-5 y A-2)
1. `./mvnw spring-boot:run` y abrir `http://localhost:8090` (clave `demo123`).
2. Entrar con `admision.a`: llega a **Inicio** con tarjetas reales. En **Pacientes** busca por DNI, N.° de historia o nombre. En **Registrar paciente** prueba un DNI nuevo (201), el mismo otra vez (200) y uno del otro hospital, por ejemplo `90000005` (409).
3. Entrar con `medico.a`: el menú trae Historia clínica y Registrar atención; las filas de Pacientes ganan "Ver historia". Con `paciente.a` el menú es Mi historial y Consentimiento, y con `plataforma` solo Hospitales.
4. Pantallas aún sin construir (Historia, Atención, Mi historial, Consentimiento, Auditoría, Hospitales y Mi perfil) dan 404 hasta los siguientes bloques.

## Cómo probar (pantallas 2: historia clínica y atención; A-4 y A-3)
1. `./mvnw spring-boot:run`, abrir `http://localhost:8090` y entrar con `medico.a` (clave `demo123`).
2. **Pacientes > Ver historia:** cabecera con iniciales, alergias en rojo, antecedentes y línea de tiempo. Las atenciones cerradas llevan candado y solo ofrecen "Agregar enmienda".
3. **Registrar atención:** elige un paciente, abre la atención, agrega un diagnóstico (busca por código o nombre CIE-10) y un tratamiento, y ciérrala con el modal de confirmación. Al cerrar desaparecen los formularios de edición (RN-03).
4. Entra con `medico.b` y abre la historia 1: se muestra el mensaje de otro establecimiento y ningún dato (RN-06). Con `enfermera.a` se ve la historia sin el botón de registrar atención.

## Cómo probar (bloque 3: consentimiento, CU-11 y P-06)
1. `./mvnw spring-boot:run`, abrir `http://localhost:8090` y entrar con `paciente.a` (clave `demo123`). El primer destino, Mi historial, aún no existe (bloque 4): usa **Consentimiento** del menú.
2. Otorga un consentimiento a "Hospital Demo B" con su alcance: aparece activo con fecha (CP-19).
3. Apaga el interruptor: confirma en el modal y queda "Revocado" con la fecha de revocación, sin posibilidad de reactivarlo (CP-20).
4. Con `medico.a` el endpoint responde 403 y sin sesión 401. `paciente.b` no ve los consentimientos de `paciente.a` (RN-04).

## Cómo probar (bloque 4: Mi historial y Mi perfil; A-6 y A-9)
1. `./mvnw spring-boot:run`, abrir `http://localhost:8090` y entrar con `paciente.a` (clave `demo123`): ahora aterriza en **Mi historial**, solo lectura y solo atenciones cerradas, sin botones de edición.
2. **Ver resumen** muestra el resumen imprimible; "Imprimir o guardar como PDF" abre el diálogo del navegador sin el menú.
3. Entra con `paciente.b`: ve su propia historia (HC-000005) y nada del Hospital A (RN-04). Con `medico.a` el endpoint del portal responde 403.
4. **Mi perfil** está en el menú de todos los roles. `plataforma` muestra "Plataforma" como hospital.
5. Con esto ya no queda ningún enlace del menú de paciente, médico, enfermería o admisión que dé 404; faltan solo Auditoría y Hospitales.

## Cómo probar (bloque 5: auditoría, tablero y hospitales; A-5, A-10 y A-11)
1. `./mvnw spring-boot:run`, abrir `http://localhost:8090` y entrar con `admin.a` (clave `demo123`): **Inicio** trae las tarjetas y el tablero con gráficos (atenciones por día, pacientes nuevos por semana, diagnósticos frecuentes, abiertas frente a cerradas y últimos accesos). Todo sale de la base de datos del hospital.
2. **Auditoría:** filtra por usuario, acción, N.° de historia y fechas; paginación en el servidor. Solo ve su hospital. Es solo lectura.
3. Entra con `plataforma`: **Hospitales** muestra una tarjeta por hospital con usuarios, pacientes y atenciones, y nunca datos clínicos (DEC-11).
4. Con esto todos los enlaces de todos los menús tienen pantalla. Los gráficos son SVG propio, sin librerías (`static/js/graficos.js`).

## Cómo probar (v5: paquete portable)
Requisito en la PC donde se arma: JDK 21 a 25 (no basta el JRE). La PC de la demo no necesita Java.
1. Doble clic en `empaquetar.bat` (o `empaquetar.bat` en una consola). Corre los tests, construye `dist\HCE-Demo\HCE-Demo.exe` con el Java incluido (`jpackage`) y genera `dist\HCE-Demo.zip` (unos 75 MB).
2. Abrir `dist\HCE-Demo\HCE-Demo.exe` con doble clic: se abre una ventana de consola (el servidor) y el navegador. La base queda en `dist\HCE-Demo\data` (junto al `.exe`).
3. Si el puerto 8090 está ocupado, la app usa el siguiente libre y abre el navegador en el correcto.
4. Guía para quien recibe el zip: `GUIA_INSTALACION.md`. Prueba pendiente antes del 5 de diciembre: copiar el zip a **otra PC Windows** y abrirlo con doble clic.
En desarrollo (`./mvnw spring-boot:run` o el IDE) no se abre el navegador solo: eso lo activa únicamente el lanzador (`hce.abrir-navegador=true`).

## Cómo probar (v5: auditoría e indicadores)
1. `./mvnw test`: se suman `AuditoriaConsultaTest` (10) e `IndicadoresServiceTest` (9) y `ArranqueLocalTest` (4).
2. Entrar con `admin.a` / `demo123` (Administrador del hospital A). Por API: `GET /api/auditoria` (con filtros `usuario`, `accion`, `nroHc`, `desde`, `hasta`, `pagina`, `tamano`) y `GET /api/indicadores/tablero`. Entrar con `medico.a`: ambos dan 403.
3. Inicio por rol: `GET /api/indicadores/inicio` con `admision.a`, `medico.a`, `enfermera.a` o `admin.a` (cada uno ve sus tarjetas, con cifras del Hospital Demo A).
4. Entrar con `plataforma`: `GET /api/indicadores/hospitales` devuelve solo totales por hospital.
5. Cada inicio de sesión, intento fallido y cierre de sesión queda en la auditoría. Contrato: `docs/contratos/auditoria_indicadores.md`.

## Cómo probar (v5: BCrypt y bloqueo por intentos)
1. `./mvnw test`: se suman `BloqueoLoginTest` (11) y `ClaveBcryptTest` (4).
2. Entrar con `enfermera.a` y una clave mala 5 veces: cada vez responde 401 con el mismo mensaje. A la sexta, **aunque pongas `demo123`**, dice "La cuenta está bloqueada... Intenta de nuevo en 15 minutos". Otros usuarios (`enfermera.b`) no se afectan. El bloqueo se levanta solo a los 15 minutos.
3. Los usuarios demo están guardados con BCrypt. Una base anterior (con SHA-256) se actualiza sola al arrancar.
4. Los intentos fallidos y bloqueados quedan en la auditoría (`LOGIN_FALLIDO`, `LOGIN_BLOQUEADO`).

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
