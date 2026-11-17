# Revisión de decisiones (hoja para marcar)

Resume `DECISIONES_PENDIENTES.md`. Para cada una hay una **recomendación** mía. Marca en la columna **Tu respuesta**: `OK` si estás de acuerdo o `CAMBIAR` con una nota. Las del bloque A son las que conviene mirar con cuidado; las del bloque B se pueden aprobar en bloque.

## A. Mirar con cuidado
| # | Tema | Qué se decidió | Recomendación | Tu respuesta |
|---|---|---|---|---|
| D-34 | Rangos de signos vitales | Solo descartan valores imposibles (por ejemplo, temperatura 30,0 a 44,0). No son rangos clínicos | Mantener y decirlo en la exposición: "rangos de demostración". Si el docente quiere rangos clínicos, los define el equipo de salud | |
| D-39 | Restablecer clave | Administrador del hospital → su personal; plataforma → administrador de hospital; admisión → pacientes. Nadie restablece la suya. No cierra sesiones ya abiertas | Mantener. La limitación de las sesiones abiertas se dice si preguntan | |
| D-40 | Atención desde la cola | La atención nace de la cola; los campos nuevos son **texto libre y opcionales**. Se conserva crear atención sin admisión | Mantener. Ver "Pendiente heredado" abajo | |
| D-41 | Exámenes | Solicitud y resultado en dos tablas; sin archivo adjunto; el resultado es texto; se puede cerrar la atención con exámenes pendientes | Mantener. Es lo más simple que cumple CU-22 y CU-23 | |
| D-42 | Auditoría de la plataforma | El restablecimiento hecho por la plataforma sobre el administrador de un hospital no sale en la auditoría de ese hospital | Mantener (RN-06). Si el docente lo pregunta, queda en la base | |
| D-27 | Quién mueve cada estado | Admisión admite/cancela; enfermería toma y deja listo; médico inicia y cierra | Mantener: sale de CU-20, CU-21 y CU-05 | |
| D-21 / D-22 | Filiación | Obligatorios: lugar de nacimiento, procedencia, estado civil, instrucción, ocupación y contacto de emergencia (religión opcional). Listas cerradas habituales | Mantener, pero **contrastar con la norma técnica de historia clínica** que use el curso | |
| D-23 | "DNI" en listas | En los selectores dice "Documento nacional de identidad", en tablas sigue "DNI 12345678" | Mantener ("DNI" es de uso común) | |
| D-13 | Planes de suscripción | Básico, Estándar y Completo; vigencia de 12 meses | Mantener: los documentos no los definían | |
| D-2 | Cuentas de prueba | `admision.a`, `medico.a`, etc. existen solo en los tests | Mantener. La demo se arma por pantallas o con `dist\data_ensayo` | |
| D-5 | Administrador inicial | `admin` / `Temporal2026`, y obliga a cambiarla | Mantener; es solo para la demo. La clave visible en el README es aceptable en un prototipo con datos ficticios | |

## B. Aprobar en bloque
| # | Qué se decidió | Recomendación | Tu respuesta |
|---|---|---|---|
| P-1 | Los IDs nuevos (RF-21 a RF-30, RN-13 a RN-19, CU-16 a CU-26, P-07 a P-16) se usan tal cual | OK, **pero** verifica que el informe use esos mismos IDs (RF-30 y CU-26 se agregaron esta semana) | |
| D-1, D-3, D-4 | Archivos de seguimiento en la raíz; Playwright solo de desarrollo; diseño del login | OK | |
| D-6 | Columna `debe_cambiar_clave` con valor por defecto, sin borrar `data/` | OK | |
| D-7, D-8 | Cambio de clave obligatorio impuesto en el servidor; pide la clave actual | OK | |
| D-10, D-17, D-26, D-38 | Interfaz sin siglas, con unidades completas | OK | |
| D-12 | Cada opción del menú entra con su pantalla | OK | |
| D-14, D-18, D-19, D-20 | `usuario` guarda nombre, documento y correo; la colegiatura va en `profesional_salud`; "activo" usa `estado`; el bloqueo se calcula con la auditoría | OK | |
| D-15, D-16 | Usuario escrito por quien crea la cuenta; clave temporal de 10 caracteres, una sola vez | OK | |
| D-24, D-25 | Cuenta del portal siempre al registrar un paciente; su creación no cuenta como acceso a la historia | OK | |
| D-28, D-29, D-30 | Una admisión activa por día; admitir es un diálogo; permisos nuevos por rol | OK | |
| D-31, D-32 | Cola en `/cola/`, con la emergencia primero; cada rol ve sus estados | OK | |
| D-35, D-36, D-37 | Signos vitales obligatorios antes de "listo"; tipos de antecedente y alergia; sección de enfermería solo desde la cola | OK | |

## C. Obsoletas, ya superadas
| # | Por qué |
|---|---|
| D-9 | Apoyo al diagnóstico ya tiene bandeja de Exámenes y cifras reales (R3) |
| D-11 | Las capturas de Inicio por rol de entonces usaron cuentas de prueba; hoy salen del recorrido sin ellas |
| D-33 | El botón "Iniciar atención" desde la cola ya existe (R3b.1) |

## Pendiente heredado
| # | Qué | Estado | Recomendación | Tu respuesta |
|---|---|---|---|---|
| P-2, P-3, P-4 | Enfermedad actual, funciones biológicas y examen por sistemas **estructurados** (campos por sistema) | Se implementaron como **texto libre** (D-40) | Dejar en texto libre para la demo; anotar en el informe que la estructura por sistema es mejora futura | |
| P-5 / D-43 | Diagnóstico **presuntivo o definitivo** | **Aplicado**: campo `certeza` junto a principal o secundario | Mantener | |
