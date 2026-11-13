# Equipo y flujo de trabajo

## Personas
| Persona | Disponibilidad | Herramienta de IA | Rol |
|---|---|---|---|
| Pablo | Tardes | Claude Code | Líder técnico. Repo, arquitectura, backend crítico, revisión y merge. Único que arma el paquete portable. |
| Alexander | Noches | ChatGPT | Tareas autocontenidas (pantallas, CSS, tests). Trabaja asíncrono: cada tarea trae su ficha completa. |
| Mattias | Desde el domingo 15/11 | Gemini Flash | Empieza con tareas pequeñas (tests, pantalla simple). Necesita Git. |

## Git (GitHub, repo privado)
1. Pablo crea el repo y sube la base. Invita a Alexander y Mattias como colaboradores.
2. `main` siempre debe compilar y pasar `mvnw test`. **Nadie hace push directo a `main`** (si el plan de GitHub no permite protegerla, es regla del grupo).
3. Una rama por tarea: `feat/p02-buscar-paciente`, `test/rn01`, `fix/login-error`.
4. Al terminar: pull request a `main` con la ficha de la tarea y captura de `mvnw test` en verde. Pablo revisa y hace el merge.
5. Antes de empezar una tarea: `git pull` de `main` y crear la rama desde ahí.
6. Commits pequeños, con IDs: `CU-06: consultar historia (RN-06, RN-07)`.
7. **No subir al repo:** `target/`, `data/` (la base H2), `.idea/`, contraseñas, datos reales.

## Dueños de archivos (para evitar conflictos)
| Zona | Dueño |
|---|---|
| `pom.xml`, `CLAUDE.md`, `docs/`, `DatosIniciales`, configuración | Pablo |
| `dominio/`, `aplicacion/`, `persistencia/` | Pablo (los demás solo con ficha) |
| `static/css/estilo.css` y layout común | Pablo (cambios pequeños con PR) |
| `static/<pantalla>/` (HTML, JS de una pantalla) | Quien tenga la ficha de esa pantalla |
| `src/test/...` | Mattias y Alexander, por ficha |

## Qué hacer cuando usas Gemini o ChatGPT
1. Pega `docs/reglas_para_ia_externa.md` al inicio del chat.
2. Pega la ficha de tu tarea y los archivos que debes tocar.
3. Ejecuta y prueba lo que te dé; si no compila o falla un test, pega el error completo.
4. No aceptes cambios en archivos que no están en tu ficha.
5. Tú eres responsable de entender lo que entregas: Pablo lo preguntará en la revisión.

## Definición de terminado (toda tarea)
- Compila, `mvnw test` en verde, la app arranca.
- Cumple el criterio de aceptación de la ficha.
- Cita IDs (CU, RN, RF).
- Sin archivos fuera de la ficha.
- Captura o explicación de cómo se probó.
