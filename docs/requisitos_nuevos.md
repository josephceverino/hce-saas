# Requisitos nuevos y modificados
Estado: **PROPUESTA por aprobar**. Los RF existentes (RF-01 a RF-20) mantienen su numeración. Los nuevos empiezan en RF-21.
Los RN existentes son RN-01 a RN-12; los nuevos empiezan en RN-13. Antes de fusionarlo, verificar en el Kit Base que ninguno de estos ID exista.

## Requisitos funcionales nuevos
| ID | Requisito | Actor | Prioridad | Módulo | Tramo |
|---|---|---|---|---|---|
| RF-21 | Admitir al paciente del día indicando el tipo de atención (consulta externa o emergencia) | A-04 | Alta | M-02 | A |
| RF-22 | Ver la lista de pacientes admitidos del día y su estado, según el rol | A-03, A-02, A-04 | Alta | M-03 | A |
| RF-23 | Registrar los signos vitales del paciente admitido | A-03 | Alta | M-03 | A |
| RF-25 | Solicitar exámenes auxiliares durante la atención | A-02 | Alta | M-03 | A |
| RF-27 | Crear la cuenta del portal del paciente al registrarlo (usuario = documento, clave temporal) | A-04 | Alta | M-04 | A |
| RF-28 | Cambiar la contraseña temporal en el primer ingreso | Todos | Alta | M-01 | A |
| RF-26 | Registrar notas de evolución por fecha | A-02 | Media | M-03 | B |
| RF-29 | Registrar el consentimiento informado de la atención | A-02 | Media | M-03 | B (recortado, ver `PLAN_V3.md`) |
| RF-30 | Restablecer la clave de una cuenta con una clave temporal (CU-26; D-39) | A-06, A-07, A-04 | Media | M-01 | A |
| RF-30 | Registrar procedimientos y seguimiento (próximo control) | A-02 | Media | M-03 | B |
(RF-24 queda reservado y sin uso para no renumerar si el grupo agrega otro.)

## Requisitos existentes que se amplían
| ID | Ampliación |
|---|---|
| RF-01, RF-03 | Registro y actualización con la **filiación completa** (ver `historia_clinica_contenido.md`, sección 1) |
| RF-05 | Anamnesis estructurada (enfermedad actual, funciones biológicas) y examen físico por sistemas |
| RF-07 | Antecedentes por tipo: personales, quirúrgicos, vacunas, hábitos, familiares; más alergias |
| RF-09 | El resultado se vincula a una solicitud de examen (RF-25) |
| RF-11 | El administrador del hospital crea al personal con su rol y una clave temporal |
| RF-20 | El administrador de plataforma da de alta el hospital y crea su administrador inicial |

## Reglas de negocio nuevas
| ID | Regla |
|---|---|
| RN-13 | Una admisión recorre los estados `ADMITIDO`, `EN_ENFERMERIA`, `LISTO_PARA_ATENCION`, `EN_ATENCION`, `ATENCION_CERRADA` solo en ese orden; `CANCELADO` solo desde `ADMITIDO` |
| RN-14 | Toda cuenta creada por otra persona tiene clave temporal y el sistema obliga a cambiarla en el primer ingreso |
| RN-15 | El usuario de un paciente es su documento de identidad; existe una sola cuenta por paciente |
| RN-16 | Solo el administrador de plataforma crea hospitales y sus administradores; el administrador de un hospital solo crea usuarios de su propio hospital |
| RN-17 | Los signos vitales los registra enfermería, se vinculan a la admisión del día y se rechazan los valores fuera de los rangos de validación definidos por el equipo |
| RN-18 | Un resultado de examen solo se registra sobre una solicitud del mismo hospital y estado `SOLICITADO` |
| RN-19 | Los códigos CIE-10 se muestran con su descripción completa; el sistema no valida el texto libre del médico |

## Impacto en cifras del informe (cuando se apruebe)
RF: 20 → 29 (hoy 14 Alta y 6 Media; los nuevos aportan 6 Alta y 3 Media, quedando 20 Alta y 9 Media). RN: 12 → 19. CU: 15 → hasta 25. Revisar todas las menciones de "20 requisitos", "12 reglas" y "15 casos de uso" en el informe.
