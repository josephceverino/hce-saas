# Recorrido del paciente por el hospital (eje del sistema)

Este documento es el eje. Todo requisito, pantalla y tarea se justifica por un paso de este recorrido, que además es el guion de la demostración.
Estado: **PROPUESTA por aprobar** (los IDs nuevos son propuestos). Base: actores A-01 a A-08 y RF-01 a RF-20 del informe.

## Los 7 roles con pantalla
| Actor | Rol en código | Inicio propio (primera pantalla tras entrar) |
|---|---|---|
| A-07 Administrador de plataforma | `ADMIN_PLATAFORMA` | Hospitales suscritos (lista y alta) |
| A-06 Administrador del hospital | `ADMIN_HOSPITAL` | Indicadores del hospital + usuarios |
| A-04 Admisión y archivo | `ADMISION` | Pacientes del día (admitidos) + buscar |
| A-03 Enfermería | `ENFERMERIA` | Cola de pacientes admitidos |
| A-02 Médico | `MEDICO` | Cola de pacientes listos para atención |
| A-05 Apoyo al diagnóstico | `APOYO_DIAGNOSTICO` | Exámenes solicitados pendientes |
| A-01 Paciente | `PACIENTE` | Mi historial |
A-08 (sistema externo) no se construye: el intercambio entre hospitales queda en el rechazo con mensaje (DEC-02).

## Los 9 pasos
| # | Quién | Qué ocurre | RF / CU |
|---|---|---|---|
| 1 | A-07 | Da de alta un hospital y crea su administrador. El sistema arranca con un único administrador de plataforma. | RF-20, CU-16 |
| 2 | A-06 | Primer ingreso (cambia su clave temporal). Crea al personal del hospital con su rol. | RF-11, RF-12, RF-28, CU-17, CU-18 |
| 3 | A-04 | Registra al paciente con su filiación completa. Se abre su historia (una por paciente) y se crea su cuenta del portal (usuario = documento, clave temporal). | RF-01, RF-04, RF-27, CU-03, CU-19 |
| 4 | A-04 | **Admite** al paciente del día: consulta externa o emergencia. Aparece en la cola. | RF-21, CU-20 |
| 5 | A-03 | Ve la cola, toma al paciente, registra signos vitales, antecedentes y alergias. Pasa a "listo para atención". | RF-22, RF-23, RF-07, CU-21 |
| 6 | A-02 | Ve la cola, lee el historial, registra anamnesis, examen físico, diagnóstico (CIE-10), tratamiento y pide exámenes. | RF-06, RF-05, RF-08, RF-25, CU-06, CU-05, CU-08, CU-22 |
| 7 | A-05 | Ve los exámenes solicitados y registra el resultado. | RF-09, CU-23 |
| 8 | A-02 | Revisa resultados, cierra la atención (desde ahí solo se enmienda). Opcional: nota de evolución. | RF-05, RN-03, CU-24 |
| 9 | A-01 | Entra al portal (cambia su clave), ve su historial en solo lectura, descarga el resumen y gestiona consentimientos. | RF-14 a RF-17, CU-10, CU-11, CU-18 |
Transversal: A-06 revisa auditoría y reportes (RF-13, RF-19, CU-25). Cada acceso queda registrado (RN-07).

## Estados de una admisión (RN-13)
`ADMITIDO` → `EN_ENFERMERIA` → `LISTO_PARA_ATENCION` → `EN_ATENCION` → `ATENCION_CERRADA` (o `CANCELADO` desde ADMITIDO). Solo avanza en ese orden.
La cola de cada rol muestra únicamente los estados que le corresponden.

## Inventario de pantallas
| ID | Pantalla | Rol | Estado |
|---|---|---|---|
| P-01 | Inicio de sesión | Todos | Existe, rehacer: sin cuentas demo, sin lista numerada |
| P-13 | Cambio de clave (primer ingreso) | Todos | Nueva |
| P-07 | Hospitales (lista y alta) | A-07 | Nueva |
| P-08 | Usuarios del hospital (lista y alta) | A-06 | Nueva |
| P-02 | Buscar paciente | A-04, A-03, A-02 | Existe, ampliar |
| P-16 | Registrar paciente (filiación completa) | A-04 | Existe, ampliar |
| P-09 | Pacientes del día / cola | A-04, A-03, A-02 | Nueva |
| P-04 | Historia clínica (cabecera + 7 secciones) | A-03, A-02 | Existe, rehacer |
| P-03 | Registrar atención | A-02 | Existe, ampliar |
| P-12 | Exámenes solicitados y resultados | A-05, A-02 | Nueva |
| P-05 | Mi historial | A-01 | Existe, mejorar |
| P-06 | Consentimiento y privacidad | A-01 | Existe |
| P-14 | Auditoría | A-06 | Existe, revisar |
| P-15 | Reportes (gráfico) | A-06 | Opcional |
P-10 (admitir) es un botón/diálogo dentro de P-02 y P-11 (signos vitales) es una sección de P-04: no son pantallas aparte.

## Qué no cambia
Capas, filtro por hospital en toda consulta (RN-06), atención cerrada inmutable (RN-03), RN-01 con el rechazo entre hospitales (DEC-02), auditoría (RN-07), consentimiento. Lo ya construido se reutiliza; lo que cambia es **el flujo entre actores** y el **contenido de la historia**.
