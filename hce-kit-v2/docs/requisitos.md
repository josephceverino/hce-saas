# Requisitos (fuente de verdad)
Fuente: Informe académico, secciones 3.1.1 a 3.1.3. Si algo no está aquí, **no se implementa** sin preguntar.

## Requisitos funcionales
| ID | Requisito | Actor | Prioridad | Módulo |
|---|---|---|---|---|
| RF-01 | Registrar paciente con sus datos de identificación y contacto | A-04 | Alta | M-02 |
| RF-02 | Buscar y consultar un paciente por documento de identidad o nombre | A-02, A-03, A-04 | Alta | M-02 |
| RF-03 | Actualizar los datos de un paciente | A-04 | Media | M-02 |
| RF-04 | Abrir la historia clínica del paciente (una por paciente) | A-04 | Alta | M-03 |
| RF-05 | Registrar una atención médica (motivo, anamnesis, examen y diagnóstico) | A-02 | Alta | M-03 |
| RF-06 | Consultar el historial clínico de un paciente | A-02, A-03 | Alta | M-03 |
| RF-07 | Registrar antecedentes y alergias | A-02, A-03 | Alta | M-03 |
| RF-08 | Registrar tratamiento e indicaciones médicas | A-02 | Alta | M-03 |
| RF-09 | Registrar resultados de exámenes auxiliares | A-05 | Media | M-03 |
| RF-10 | Autenticar usuarios mediante credenciales | Todos | Alta | M-01 |
| RF-11 | Gestionar usuarios y roles del hospital | A-06 | Alta | M-01 |
| RF-12 | Gestionar permisos de acceso a la historia clínica por rol | A-06 | Alta | M-01 |
| RF-13 | Registrar auditoría de accesos y modificaciones | Sistema | Alta | M-05 |
| RF-14 | Permitir al paciente iniciar sesión en su portal | A-01 | Alta | M-04 |
| RF-15 | Permitir al paciente consultar su historial en modo solo lectura | A-01 | Alta | M-04 |
| RF-16 | Permitir al paciente ver o descargar un resumen de su historia clínica | A-01 | Media | M-04 |
| RF-17 | Permitir al paciente otorgar o revocar acceso a terceros (consentimiento) | A-01 | Alta | M-04 |
| RF-18 | Intercambiar información clínica con otros establecimientos | A-02, A-08 | Media | M-06 |
| RF-19 | Generar reportes básicos de atenciones | A-06 | Media | M-07 |
| RF-20 | Gestionar hospitales suscritos y sus planes | A-07 | Media | M-08 |

## Requisitos no funcionales
| ID | Categoría | Descripción | Meta de diseño |
|---|---|---|---|
| RNF-01 | Seguridad | Cifrado de datos en tránsito y en reposo | TLS en toda comunicación y almacenamiento cifrado |
| RNF-02 | Cumplimiento legal | Tratamiento de datos de salud conforme a la Ley N.° 29733 y al D.S. N.° 016-2024-JUS | Consentimiento y finalidad registrados |
| RNF-03 | Disponibilidad | Servicio accesible durante la atención | 99,5 % mensual |
| RNF-04 | Rendimiento | Consulta de una historia clínica sin demoras perceptibles | 3 segundos o menos en condiciones normales |
| RNF-05 | Usabilidad | Uso por personal de salud con capacitación breve | Tarea principal en 5 clics o menos |
| RNF-06 | Aislamiento entre hospitales | Un hospital no puede ver datos de otro | 0 accesos cruzados en pruebas |
| RNF-07 | Interoperabilidad | Intercambio basado en HL7 (CDA R2), con FHIR contemplado para etapas posteriores | Mensajes conformes con el documento técnico del SIHCE |
| RNF-08 | Trazabilidad | Todo acceso y cambio es auditable | 100 % de accesos registrados |
| RNF-09 | Respaldo y recuperación | Copias de seguridad automáticas | Respaldo diario y restauración documentada |
| RNF-10 | Compatibilidad | Navegadores actuales y diseño adaptable a móvil | Chrome, Edge, Firefox y Safari |
| RNF-11 | Mantenibilidad | Arquitectura por capas | Presentación, aplicación, dominio y persistencia separadas |

## Reglas de negocio
- **RN-01**: Cada paciente tiene una única historia clínica, identificada por su documento de identidad (DNI, carné de extranjería u otro).
- **RN-02**: Solo el personal de salud autorizado puede registrar o modificar atenciones.
- **RN-03**: Una atención cerrada no se elimina ni se sobrescribe; toda corrección se registra como enmienda con autor y fecha.
- **RN-04**: El paciente accede solo a su propia historia y solo en modo lectura.
- **RN-05**: Si el paciente es menor de edad o no puede acceder por sí mismo, accede su representante legal acreditado.
- **RN-06**: Cada hospital solo ve las historias de sus pacientes, salvo intercambio autorizado por el paciente o emergencia justificada.
- **RN-07**: Todo acceso a una historia clínica queda auditado (quién, cuándo y qué hizo).
- **RN-08**: El acceso por emergencia a la historia de otro establecimiento exige justificación escrita y queda auditado.
- **RN-09**: Los datos de salud son datos sensibles: se tratan con consentimiento y para una finalidad declarada (Ley N.° 29733).
- **RN-10**: Todo diagnóstico se registra con un código de un catálogo vigente (CIE-10).
- **RN-11**: Una atención pertenece a un solo paciente, un solo profesional y un solo establecimiento.
- **RN-12**: Las contraseñas cumplen una política mínima y la cuenta se bloquea tras intentos fallidos consecutivos.

## Interesados
| ID | Interesado | Interés | Influencia |
|---|---|---|---|
| S-01 | Pacientes y familias | Acceso a su información y privacidad | Baja |
| S-02 | Personal médico y de enfermería | Menos carga administrativa y menos tiempo buscando registros | Alta |
| S-03 | Dirección del hospital | Menor costo y rapidez de implementación | Alta |
| S-04 | Oficina de TI o Estadística e Informática | Integración, soporte y seguridad | Media |
| S-05 | Los especialistas del MINSA y gobiernos regionales | Cumplir estándares del SIHCE e interoperabilidad | Alta |
| S-06 | Autoridad Nacional de Protección de Datos Personales | Cumplimiento de la Ley N.° 29733 y su reglamento | Alta |
| S-07 | Equipo de desarrollo | Entregar un producto coherente y a tiempo | Alta |
| S-08 | Docente y asesora | Cumplimiento de la estructura y la rúbrica | Alta |
| S-09 | Proveedor de infraestructura en la nube | Uso contractual del servicio | Media |
