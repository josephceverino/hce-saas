# Pruebas manuales del plan v3 (recorrido de 9 pasos)

Se ejecutan **a mano, en la PC de desarrollo**, con la aplicación en `http://localhost:8090` (IntelliJ o `mvnw spring-boot:run`) o con `dist\HCE-Demo\HCE-Demo.exe` (puerto 8091 si el 8090 está ocupado).
Todos están en estado **Pendiente**: el Resultado real, el Estado, la Fecha y el Responsable se llenan solo después de ejecutarlos, con evidencia (captura propia). No marcar **Aprobado** sin ella.
Las capturas `docs/capturas/paso*` las generó el recorrido automático (`npm run recorrido -- 9`): sirven como referencia de lo esperado, **no** como evidencia de esta ejecución.

## Preparación
- Base limpia: con la app cerrada, renombra `data/` (por ejemplo a `data_vieja/`). Solo existirá `admin` / `Temporal2026`.
- O bien usa la base ya poblada (usuarios en `GUIA_INSTALACION.md`, sección 2) y salta los pasos 1 a 4.
- Claves del recorrido (tras cambiar la temporal): `admin` `Plataforma2026`, `admin.trujillo` `Hospital2026`, `carlos.rios` `Admision2026`, `ana.flores` `Enfermeria2026`, `jorge.paz` `Medico2026`, `elena.campos` `Apoyo2026`.

## Casos

| ID | Paso | CU / RN | Quién | Pasos a ejecutar | Resultado esperado | Referencia (captura del recorrido) | Resultado real | Estado | Fecha | Responsable |
|---|---|---|---|---|---|---|---|---|---|---|
| MV-01 | 1 | CU-16, RN-16 | `admin` | Entrar con la clave temporal, cambiarla, ir a **Hospitales** → **Nuevo hospital**, completar y guardar | Pide cambiar la clave; el hospital aparece en la lista; la constancia muestra usuario y clave temporal de 10 caracteres, una sola vez | `paso1_P-07_constancia` | | Pendiente | | |
| MV-02 | 1 | RN-14 | Administrador del hospital | Entrar con la temporal | Obliga a cambiarla antes de seguir; con una clave de menos de 6 caracteres o sin número, la rechaza | `paso2_cambiar_clave` | | Pendiente | | |
| MV-03 | 2 | CU-17, RN-16 | `admin.trujillo` | **Usuarios** → crear admisión, enfermería, médico (con colegiatura) y apoyo | Los 4 aparecen con "Clave pendiente"; el médico sin colegiatura se rechaza | `paso2_P-08_usuarios_lista` | | Pendiente | | |
| MV-04 | 2 | RN-16 | `admin.trujillo` | Revisar los roles ofrecidos al crear un usuario | No hay rol administrador en la lista (el servidor también lo rechaza) | — | | Pendiente | | |
| MV-05 | 3 | CU-03, CU-19, RN-01, RN-15 | `carlos.rios` | **Registrar paciente** con filiación completa; repetir el mismo documento | Se abre la historia y sale la constancia del portal (usuario = documento); el documento repetido se rechaza sin crear otra historia | `paso3_P-16_constancia` | | Pendiente | | |
| MV-06 | 3 | RF-01 | `carlos.rios` | Registrar con documento de 7 dígitos, correo mal escrito y fecha de nacimiento futura | Mensajes de validación en español; no se registra | — | | Pendiente | | |
| MV-07 | 4 | CU-20, RN-13 | `carlos.rios` | **Pacientes** → **Admitir** a uno por consulta externa y a otro por emergencia; intentar admitir otra vez al mismo | La segunda admisión del mismo paciente el mismo día se rechaza; la emergencia se distingue | `paso4_P-02_admitidos` | | Pendiente | | |
| MV-08 | 5 | RF-22 | `ana.flores` | Abrir **Cola de pacientes** | La emergencia aparece primero aunque llegó después | `paso5_P-09_cola_enfermeria` | | Pendiente | | |
| MV-09 | 5 | CU-21, RN-17 | `ana.flores` | Tomar al paciente; registrar temperatura 45,5; corregir a 37,8; agregar antecedente y alergia; **Listo para atención** | La temperatura imposible se rechaza con mensaje claro; sin signos vitales no deja marcar "listo" | `paso5_P-04_signos_rechazados` | | Pendiente | | |
| MV-10 | 5 | RF-22, RN-13 | `jorge.paz` | Abrir su **Cola de pacientes** | Ve solo al paciente "listo para atención"; no ve a quien no pasó por enfermería | `paso5_P-09_cola_medico` | | Pendiente | | |
| MV-11 | 6 | CU-05, CU-08, RN-10 | `jorge.paz` | **Iniciar atención** → completar campos ampliados → agregar diagnóstico `J02.9` y un tratamiento; probar un código inexistente (`ZZZ`) | La admisión pasa a "en atención"; el código inexistente se rechaza | `paso6_P-03_atencion_ampliada` | | Pendiente | | |
| MV-12 | 6 | CU-22, RN-03 | `jorge.paz` | **Solicitar examen auxiliar** en la atención abierta | El examen queda "pendiente de resultado" en la atención | `paso6_P-03_atencion_con_examen` | | Pendiente | | |
| MV-13 | 7 | CU-23, RN-18 | `elena.campos` | Entrar (cambia clave), **Exámenes** → **Registrar resultado**; intentar registrarlo otra vez | El examen pasa a "Con resultado"; no se puede registrar dos veces | `paso7_P-12_examenes_resueltos` | | Pendiente | | |
| MV-14 | 8 | CU-05, RN-03 | `jorge.paz` | Abrir la atención, ver el resultado, **Cerrar atención**; intentar editar | El resultado se ve; al cerrar desaparecen los formularios y solo queda la enmienda; el paciente sale de la cola | `paso8_P-03_atencion_cerrada` | | Pendiente | | |
| MV-15 | 8 | RN-03 | `jorge.paz` | Registrar una enmienda sobre la atención cerrada | Queda con autor y fecha; el contenido original no cambia | — | | Pendiente | | |
| MV-16 | 9 | CU-26, RN-14 | `carlos.rios` | **Pacientes** → **Restablecer clave** del paciente atendido | Muestra una clave temporal una sola vez; la clave anterior deja de servir | `paso9_P-02_restablecer_clave_paciente` | | Pendiente | | |
| MV-17 | 9 | CU-10, RN-04 | Paciente | Entrar con la temporal, cambiarla, abrir **Mi historial** | Ve solo atenciones cerradas, con datos ampliados y el examen con resultado; no ve signos vitales | `paso9_P-05_mi_historial` | | Pendiente | | |
| MV-18 | 9 | CU-10, RF-16 | Paciente | **Ver resumen** e imprimir o guardar como PDF | El resumen lista alergias, antecedentes, diagnósticos y exámenes con resultado | `paso9_P-05_resumen` | | Pendiente | | |
| MV-19 | 9 | CU-11, RN-09 | Paciente | **Consentimiento**: otorgar a un tercero y luego apagar el interruptor | Queda registrado con fecha y alcance; al revocar queda "revocado" y no se reactiva (se otorga uno nuevo) | `paso9_P-06_consentimiento_revocado` | | Pendiente | | |
| MV-20 | 9 | CU-25, RN-07 | `admin.trujillo` | **Auditoría**: filtrar por `RESTABLECER_CLAVE` y por `SOLICITAR_EXAMEN` | Aparece quién lo hizo, cuándo y desde qué IP | `paso9_P-14_auditoria_restablecer` | | Pendiente | | |
| MV-21 | Transversal | RNF-06, RN-06 | Dos hospitales | Crear un **segundo hospital** (paso 1) con su administrador y personal; entrar con su admisión y su médico | No ve pacientes, admisiones, exámenes ni auditoría del primer hospital | — | | Pendiente | | |
| MV-22 | Transversal | RN-02 | Distintos roles | Con enfermería, intentar abrir `/usuarios/` y `/hospitales/`; con el médico, `/examenes/` | El menú no las ofrece y el servidor responde con "No tienes permiso" al pedir sus datos | — | | Pendiente | | |
| MV-23 | Transversal | RN-12 | Cualquier usuario | Fallar la clave 5 veces seguidas | La cuenta se bloquea unos minutos con mensaje claro; el administrador del hospital puede restablecer la clave y se levanta el bloqueo | — | | Pendiente | | |
| MV-24 | Portabilidad | RNF | Paquete | Doble clic en `dist\HCE-Demo\HCE-Demo.exe`; cerrar y volver a abrir | Se abre el navegador; sin internet funciona; los datos siguen en `HCE-Demo\data` | — | | Pendiente | | |

## Cobertura automática de las mismas reglas
`./mvnw test` (220 tests). Referencias útiles: `EstadoAdmisionTest` (RN-13, 36 transiciones), `RestablecimientoClaveServiceTest` (CU-26), `ExamenServiceTest` (RN-18), `AtencionDesdeColaTest` (CU-05 ampliado), `PortalHistoriaNuevaTest` (RN-04), `AuditoriaPlanV3Test` (RN-06, RN-07), `ConsentimientoServiceTest` (CU-11).
Estos tests respaldan el comportamiento, pero **no sustituyen** la ejecución manual de arriba.
