# Guion de la demostración (esqueleto, 6 a 7 minutos)
El sistema arranca **vacío**: un solo administrador de plataforma. Todo se crea en vivo siguiendo el recorrido. Si algo falla, se restaura la copia de `data/` ensayada (ver `PLAN_V3.md`).

| Min | Qué se muestra | Quién habla | Lo que se debe ver |
|---|---|---|---|
| 0:00 | Doble clic en `HCE-Demo.exe`; se abre el navegador; login del administrador de plataforma y cambio de clave | A | Arranque sin instalar nada |
| 0:45 | Alta del hospital y de su administrador (paso 1) | A | Multi-hospital real (SaaS) |
| 1:15 | Administrador del hospital: crea admisión, enfermería, médico y apoyo (paso 2) | B | Roles y permisos |
| 2:00 | Admisión: registra al paciente con filiación completa, se abre su historia y se entrega la constancia de acceso (paso 3); intenta repetir el documento (RN-01) y lo rechaza | C | RN-01 |
| 2:45 | Admisión admite al paciente: aparece en la cola (paso 4) | C | Flujo entre actores |
| 3:00 | Enfermería: toma al paciente, signos vitales y alergias (paso 5) | D | Cola por rol |
| 3:45 | Médico: lee la historia, registra anamnesis, examen, diagnóstico CIE-10, tratamiento y pide un examen (paso 6) | E | Contenido clínico |
| 4:45 | Apoyo al diagnóstico registra el resultado; el médico lo revisa y cierra la atención; intenta editar y el sistema lo impide (pasos 7 y 8) | F | RN-03 |
| 5:30 | Paciente entra al portal (cambia clave), ve su historial, revoca un consentimiento (paso 9) | A | RN-04 y consentimiento |
| 6:15 | Otro hospital intenta ver esa historia y se le deniega; auditoría muestra los accesos | B | RN-06, RN-07 |

Los nombres A a F son marcadores de reparto: asignar según quién participó realmente en el desarrollo.
Respaldo: video grabado del recorrido y copia de `data/` ya poblada.
