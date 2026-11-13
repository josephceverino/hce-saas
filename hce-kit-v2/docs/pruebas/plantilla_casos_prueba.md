# Plantilla de casos de prueba

Derivada de las reglas de negocio y los casos de uso. **Todos están en estado 'Pendiente': nadie los ha ejecutado.** Las columnas Resultado real, Estado, Fecha y Responsable se llenan solo después de ejecutarlos. No marcar 'Aprobado' sin evidencia (captura o nombre del test).

| ID | CU | Regla / requisito | Precondición | Pasos | Resultado esperado | Cobertura (auto / manual) | Resultado real | Estado | Fecha | Responsable |
|---|---|---|---|---|---|---|---|---|---|---|
| CP-01 | CU-03 | RN-01 | Admisión inició sesión; el DNI 12345678 no existe | Registrar paciente con DNI 12345678 y datos válidos | Se crea paciente e historia clínica con número HC-xxxxxx | _por definir_ | | Pendiente | | |
| CP-02 | CU-03 | RN-01 | Ya existe un paciente con DNI 12345678 | Intentar registrar otro paciente con el mismo DNI | El sistema rechaza y no crea otra historia (1 por paciente) | _por definir_ | | Pendiente | | |
| CP-03 | CU-03 | RF-01 | Sesión iniciada | Registrar con DNI de 7 dígitos o con letras | Mensaje de validación; no se registra | _por definir_ | | Pendiente | | |
| CP-04 | CU-03 | RF-01 | Sesión iniciada | Registrar con correo mal escrito y fecha de nacimiento futura | Mensajes de validación; no se registra | _por definir_ | | Pendiente | | |
| CP-05 | CU-01 | RN-12 | Usuario existente | Ingresar contraseña incorrecta | Acceso denegado, mensaje genérico | _por definir_ | | Pendiente | | |
| CP-06 | CU-01 | RN-12 | Usuario existente | Fallar N veces seguidas (N según política v5) | Cuenta bloqueada (v5) | _por definir_ | | Pendiente | | |
| CP-07 | CU-06 | RN-06 / RNF-06 | Médico del hospital A; historia del hospital B | Buscar la historia por DNI | Acceso denegado; 0 datos cruzados | _por definir_ | | Pendiente | | |
| CP-08 | CU-06 | RN-07 | Médico del hospital A; historia del hospital A | Consultar la historia | Se muestra y queda registro de auditoría (v5) | _por definir_ | | Pendiente | | |
| CP-09 | CU-06 | RNF-04 | Historia con atenciones | Medir el tiempo de carga | Meta: 3 s o menos (registrar el tiempo real medido) | _por definir_ | | Pendiente | | |
| CP-10 | CU-05 | RN-02 | Usuario sin rol de médico | Intentar registrar una atención | Operación rechazada | _por definir_ | | Pendiente | | |
| CP-11 | CU-05 | RN-03 | Atención abierta | Cerrar la atención | Queda cerrada con fecha, hora y autor | _por definir_ | | Pendiente | | |
| CP-12 | CU-05 | RN-03 | Atención cerrada | Intentar editarla o borrarla | Rechazado; solo se admite enmienda con autor y fecha | _por definir_ | | Pendiente | | |
| CP-13 | CU-05 | RN-10 | Atención abierta | Registrar diagnóstico sin código CIE-10 | Rechazado | _por definir_ | | Pendiente | | |
| CP-14 | CU-08 | RN-03 | Atención cerrada | Intentar registrar tratamiento | Rechazado; se debe registrar enmienda | _por definir_ | | Pendiente | | |
| CP-15 | CU-08 | RF-08 | Atención abierta propia | Registrar indicación, medicamento, dosis y duración | Se vincula a la atención | _por definir_ | | Pendiente | | |
| CP-16 | CU-10 | RN-04 | Paciente con sesión en el portal | Consultar su historial | Ve solo lo suyo, en modo lectura | _por definir_ | | Pendiente | | |
| CP-17 | CU-10 | RN-04 | Paciente A con sesión | Pedir la historia del paciente B por la URL o API | Acceso denegado (403) | _por definir_ | | Pendiente | | |
| CP-18 | CU-10 | RF-16 | Paciente con sesión | Pedir el resumen | Se muestra o descarga el resumen | _por definir_ | | Pendiente | | |
| CP-19 | CU-11 | RN-09 | Paciente con sesión | Otorgar acceso a un tercero | Consentimiento registrado con fecha y alcance | _por definir_ | | Pendiente | | |
| CP-20 | CU-11 | RN-06 | Consentimiento otorgado | Revocarlo y reintentar el acceso del tercero | Acceso cortado de inmediato | _por definir_ | | Pendiente | | |
| CP-21 | Transversal | RNF-06 | Dos hospitales con datos | Recorrer todas las pantallas como usuario del hospital A | 0 accesos a datos del hospital B | _por definir_ | | Pendiente | | |
| CP-22 | Transversal | RNF-10 | App en ejecución | Abrir en Chrome, Edge, Firefox (y móvil si se puede) | Se ve y funciona; anotar el navegador usado | _por definir_ | | Pendiente | | |

Nota: CP-06 y los de auditoría dependen de v5; CP-16 a CP-20 dependen de v4.