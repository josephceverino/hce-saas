# Decisiones pendientes de revisión

Decisiones tomadas durante el trabajo autónomo del plan v3. Cada una es la opción más simple coherente con los documentos. Revisar y confirmar o corregir.

## Heredadas de los documentos (marcadas "propuesta")
| # | Tema | Qué se usó | Fuente |
|---|---|---|---|
| P-1 | IDs nuevos (RF-21 a RF-30, RN-13 a RN-19, CU-16 a CU-25, P-07 a P-16) | Se usan tal cual; no se verificaron contra el Kit Base | `docs/requisitos_nuevos.md`, `docs/casos_de_uso_nuevos.md` |
| P-2 | Enfermedad actual estructurada | Tiempo de enfermedad, forma de inicio, curso y relato | `docs/historia_clinica_contenido.md`, sección 2 |
| P-3 | Funciones biológicas | Apetito, sed, sueño, deposiciones y micciones | idem |
| P-4 | Lista de sistemas del examen físico | General, piel, cabeza y cuello, tórax y pulmones, cardiovascular, abdomen, neurológico, osteomuscular | idem, sección 3 |
| P-5 | Tipo de diagnóstico presuntivo o definitivo | Pendiente de aplicar en R3 (el código actual usa principal o secundario) | idem, sección 4 |

## Tomadas durante la implementación
| # | Ítem | Decisión | Por qué |
|---|---|---|---|
| D-1 | A2 | `PROGRESO.md` y `DECISIONES_PENDIENTES.md` van en la raíz del repo, junto a `PLAN_V3.md` | Así no se toca `docs/` |
| D-2 | 0.1 | Las cuentas de prueba (`admision.a`, `medico.a`, ... clave `demo123`) siguen existiendo **solo en los tests** (`hce.demo-usuarios=true` en `src/test/resources`). En la aplicación quedan apagadas, igual que los pacientes ficticios (`hce.demo-datos=false`) | Los 119 tests dependen de esas cuentas; reescribirlos no aporta a la demo |
| D-3 | 0.1 | Playwright como `playwright-core` en `e2e/` (dependencia de desarrollo de Node, fuera de Maven y del paquete), usando el Chrome instalado (`channel: chrome`) para no descargar navegadores | Única dependencia nueva; no entra en el `.exe` |
| D-4 | 0.1 | Login: panel izquierdo con marca, título y una frase; tarjeta "Bienvenido" con pie "Prototipo académico con datos ficticios" (referencia 04) | Más cercano a la referencia sin agregar opciones fuera de alcance (sin "olvidé mi clave", sin redes sociales) |
| D-5 | 0.2 | Administrador inicial: usuario `admin`, clave temporal `Temporal2026` (cumple la política de RN-12), configurables por propiedad. Se crea solo si no hay ningún administrador de plataforma | La opción más simple; la clave queda en el README y en la guía |
| D-6 | 0.2 | `debe_cambiar_clave` es una columna booleana con valor por defecto `false`, para que una base `data/` anterior se actualice sola | Evita borrar `data/` solo por este cambio |
| D-7 | 0.3 | RN-14 se aplica en el servidor: `SesionActual.requerir` responde 403 a cualquier función mientras la clave sea temporal; solo ver la sesión, cambiar la clave y salir quedan permitidos. El frontend además redirige a P-13 | Un solo punto de control para todos los controladores |
| D-8 | 0.3 | El cambio de clave pide la clave actual (la temporal) además de la nueva dos veces | Evita que alguien con la sesión abierta cambie la clave sin conocerla |
| D-9 | 0.4 | Apoyo al diagnóstico tiene menú (Inicio y Mi perfil) e Inicio propio, sin tarjetas hasta R3: muestra "Aún no hay exámenes solicitados" en vez de una cifra | No hay tabla de solicitudes todavía; no se inventan cifras |
| D-10 | 0.4 | Estructura común rehecha según la referencia 01: barra lateral clara con íconos en recuadro y opción activa resaltada, usuario y hospital en la barra superior, Inicio con resumen del día y accesos rápidos tomados del menú del servidor | Afecta a todas las pantallas; los textos por rol son solo presentación |
| D-11 | 0.4 | Las capturas de Inicio por rol se tomaron con `--hce.demo-usuarios=true --hce.demo-datos=true` (cuentas y pacientes de prueba), porque sin R1 no hay forma de crear usuarios | Solo para capturas; la aplicación no los trae |
| D-12 | 0.4 | Usuarios del hospital (P-08) entra en el menú del administrador del hospital en el ítem 1.2, junto con su pantalla | Evita un enlace que dé 404 |
| D-13 | 1.1 | Se agregó la entidad `Suscripcion` (la tabla ya estaba en el modelo). Planes: Básico, Estándar y Completo; vigencia de 12 meses desde el alta | El plan es obligatorio en CU-16, pero los documentos no listan los planes |
| D-14 | 1.1, 1.2 | `usuario` gana `nombre_completo`, `documento` y `correo` (nulos en cuentas antiguas). No estaban en `modelo_datos_cambios.md`, pero CU-16 y CU-17 piden esos datos | Sin ellos no hay dónde guardar quién es la persona |
| D-15 | 1.1 | El nombre de usuario del administrador (y del personal) lo escribe quien crea la cuenta; es único. El código del establecimiento usa la columna existente `codigo_ipress` (5 a 10 dígitos, único) | Más simple que generar usuarios; el documento no sirve como usuario del personal porque chocaría con su cuenta de paciente (RN-15) |
| D-16 | 1.1 | Clave temporal aleatoria de 10 caracteres (letras y dos dígitos, sin caracteres confundibles), mostrada una vez en una constancia imprimible; al pulsar "Listo" desaparece de la pantalla | CU-16 paso 4 |
| D-17 | 1.1 | Se quitaron de los textos visibles las referencias a IDs como "(RN-06)" o "CU-03:" | Regla 7: sin siglas en la interfaz |
| D-18 | 1.2 | La colegiatura se guarda en `profesional_salud` (que ya existía) y no en una columna nueva `usuario.numero_colegiatura`. Es obligatoria para médico y enfermería, opcional para apoyo al diagnóstico | Evita duplicar el dato |
| D-19 | 1.2 | "Activo/inactivo" usa la columna existente `usuario.estado` (ACTIVO, INACTIVO) en lugar de una columna `activo` nueva. El administrador no puede desactivar su propia cuenta | El login ya rechaza a quien no está ACTIVO |
| D-20 | 1.2 | El bloqueo por intentos (RN-12) sigue calculándose con la auditoría; no se agregaron `intentos_fallidos` ni `bloqueado_hasta` | Ya funciona y está probado (DEC-15) |
