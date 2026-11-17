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
