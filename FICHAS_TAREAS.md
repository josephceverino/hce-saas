# Fichas de tareas
Formato: objetivo, IDs, archivos, criterio de aceptación, cómo probar. Los que usan Gemini o ChatGPT pegan `docs/reglas_para_ia_externa.md` + la ficha. Pablo revisa todo.

## FASE 0 (esta semana)

### F0-1 · Instalación del entorno (Alexander y Mattias)
- Instalar: JDK 21 a 25, IntelliJ IDEA, **Git for Windows**, cuenta GitHub (enviar su usuario a Pablo).
- Clonar el repo, abrir en IntelliJ, ejecutar `.\mvnw.cmd spring-boot:run`, abrir `http://localhost:8090`.
- Aceptación: captura del login funcionando. Si falla, enviar el error completo.

### F0-2 · Leer el contexto (Mattias: hoy y mañana; Alexander: cuando pueda)
- Leer `ONBOARDING_MATTIAS.md`, `docs/requisitos.md` (solo RN-01 a RN-12) y `docs/casos_de_uso.md` (CU-03 y CU-06).
- Aceptación: poder explicar en 3 líneas qué hace RN-01, RN-03 y RN-06.

### F0-3 · Base del proyecto (Pablo)
- Repo, `.gitignore`, semilla migrada a Spring Boot 4.x, Java nivel 21, puerto 8090, H2 en archivo, tests en verde.
- Frontend dividido en `static/css/estilo.css`, `static/js/` común y una carpeta por pantalla; layout con sidebar.

## FASE 1 (v3)

### F1-1 · Backend CU-06, CU-05, CU-08 (Pablo)
Ver prompt 2 en `PROMPTS.md`. IDs: RN-02, 03, 06, 07, 10, 11.

### F1-2 · Pantalla P-01 Login con el nuevo diseño (Alexander, noche)
- Archivos: `static/login/` (HTML, CSS propio de la pantalla, JS). No tocar el backend: usa el endpoint de login existente.
- Diseño: `docs/diseno_ui.md` (dos columnas; botones de cuentas demo; mensaje de error claro).
- Aceptación: iniciar sesión con `admision.a / demo123` y con `admision.b / demo123`; contraseña mala muestra error; se ve bien en pantalla de laptop.

### F1-3 · Tests de RN-01 y validaciones (Mattias)
- Archivos: `src/test/java/.../RegistroPacienteServiceTest.java` (nuevo).
- Casos: registrar paciente válido; DNI duplicado rechazado (RN-01); DNI con menos de 8 dígitos rechazado; el mismo DNI en otro hospital (preguntar a Pablo antes qué debe pasar).
- Aceptación: `.\mvnw.cmd test` en verde, captura incluida.

### F1-4 · Pantallas P-02 y P-04 (Alexander)
- Dependen de los endpoints de F1-1; Pablo avisa cuando estén.
- Archivos: `static/pacientes/` y `static/historia/`.
- Aceptación: buscar por DNI, abrir historia, ver línea de tiempo; una atención cerrada no muestra botón de editar.

## FASE 2 (v4)
- **F2-1** Backend CU-10 y CU-11 (Pablo).
- **F2-2** P-05 Mi historial, solo lectura (Alexander).
- **F2-3** P-06 Consentimiento con interruptores (Mattias).
- **F2-4** Tests de RN-04 y revocación inmediata (Mattias).

## FASE 3 (v5 y cierre)
- **F3-1** Auditoría, roles, BCrypt (Pablo).
- **F3-2** Pantalla de auditoría del administrador (Alexander, si hay tiempo).
- **F3-3** Ejecutar casos de prueba con evidencia (todos; ver `docs/pruebas/`).
- **F3-4** Paquete portable y guía (Pablo).
- **F3-5** Guion y ensayo de la demo (todos).
