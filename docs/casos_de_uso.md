# Casos de uso principales
Fuente: Informe académico, sección 3.2.2. En todos, la precondición incluye sesión iniciada (CU-01).
Los 15 CU existen en el modelo (CU-01..CU-15); solo estos seis están especificados: CU-03, 05, 06, 08, 10, 11.
CU-03 incluye CU-04 (búsqueda previa). CU-05 incluye CU-06 (revisar historia antes de atender).

### CU-03: Registrar paciente y abrir historia clínica
- **Actor principal**: Personal administrativo (A-04).
- **Requisitos**: RF-01, RF-04.
- **Reglas de negocio**: RN-01, RN-07.
- **Precondición**: A-04 inició sesión (CU-01).
- **Flujo principal:**
  1. A-04 ingresa al módulo de gestión de pacientes (M-02).
  1. El sistema ejecuta la búsqueda previa (CU-04) con el tipo y número de documento.
  1. Al no encontrar coincidencias, A-04 selecciona “Registrar paciente”.
  1. A-04 ingresa los datos de identificación y de contacto.
  1. El sistema valida el formato del documento, registra al paciente y abre su historia clínica.
  1. El sistema registra la auditoría y muestra la confirmación con el número de historia clínica.
- **Flujo alternativo**: en el paso 2, si el paciente ya existe, el sistema muestra su historia clínica existente y no crea otra (RN-01).
- **Postcondición**: existe una única historia clínica asociada al paciente.

### CU-05: Registrar atención médica
- **Actor principal**: Médico (A-02).
- **Requisitos**: RF-05.
- **Reglas de negocio**: RN-02, RN-03, RN-10, RN-11.
- **Precondición**: A-02 inició sesión (CU-01) y seleccionó la historia clínica del paciente (CU-06).
- **Flujo principal:**
  1. A-02 crea una nueva atención.
  1. A-02 ingresa el motivo, la anamnesis y el examen físico.
  1. A-02 registra el diagnóstico con su código CIE-10.
  1. A-02 cierra la atención.
  1. El sistema marca la atención como cerrada y no permite eliminarla ni sobrescribirla.
- **Flujo alternativo**: si A-02 no tiene permiso para registrar atenciones, el sistema rechaza la operación (RN-02). Una corrección posterior se registra como enmienda con autor y fecha (RN-03).
- **Postcondición**: la atención queda fijada con fecha, hora y autor, y es auditable.

### CU-06: Consultar historia clínica
- **Actores principales**: Médico (A-02) y personal de enfermería (A-03).
- **Requisitos**: RF-06.
- **Reglas de negocio**: RN-06, RN-07.
- **Precondición**: el usuario inició sesión (CU-01) y su rol permite consultar historias clínicas.
- **Flujo principal:**
  1. El usuario busca la historia clínica ingresando el documento de identidad del paciente.
  1. El sistema verifica los permisos del rol y que la historia pertenezca a su hospital o tenga un intercambio autorizado (RN-06).
  1. El sistema carga la historia clínica (meta de 3 segundos o menos, RNF-04).
  1. El usuario revisa las atenciones, los antecedentes y las alergias.
- **Flujo alternativo**: en el paso 2, si la historia pertenece a otro establecimiento sin intercambio autorizado, el sistema deniega el acceso.
- **Postcondición**: la lectura queda registrada en la auditoría (RN-07).

### CU-08: Registrar tratamiento e indicaciones
- **Actor principal**: Médico (A-02).
- **Requisitos**: RF-08.
- **Reglas de negocio**: RN-02, RN-03, RN-11.
- **Precondición**: A-02 inició sesión (CU-01) y existe una atención abierta que él registró.
- **Flujo principal:**
  1. Durante la atención, A-02 abre la sección “Tratamiento e indicaciones”.
  1. A-02 ingresa la indicación, el medicamento, la dosis y la duración.
  1. El sistema valida los datos y los vincula a la atención.
  1. A-02 confirma el registro.
- **Flujo alternativo**: si la atención ya está cerrada, el sistema no permite el registro y A-02 debe registrar una enmienda (RN-03).
- **Postcondición**: el tratamiento queda vinculado a la atención y auditado.

### CU-10: Consultar mi historial clínico
- **Actor principal**: Paciente (A-01), o su representante legal acreditado.
- **Requisitos**: RF-14, RF-15, RF-16.
- **Reglas de negocio**: RN-04, RN-05, RN-07.
- **Precondición**: A-01 está registrado e inició sesión en el portal del paciente (CU-01).
- **Flujo principal:**
  1. El sistema valida las credenciales de A-01.
  1. El sistema presenta, en modo solo lectura, sus atenciones, recetas y exámenes.
  1. A-01 solicita ver o descargar el resumen de su historia clínica.
  1. El sistema genera el resumen en pantalla o para descarga.
- **Flujo alternativo**: si accede un representante legal, el sistema verifica antes que su representación esté vigente (RN-05).
- **Postcondición**: la consulta queda registrada en la auditoría.

### CU-11: Gestionar consentimiento de acceso
- **Actor principal**: Paciente (A-01).
- **Requisitos**: RF-17.
- **Reglas de negocio**: RN-06, RN-09, RN-07.
- **Precondición**: A-01 inició sesión en el portal del paciente (CU-01).
- **Flujo principal:**
  1. A-01 accede a “Gestión de consentimiento”.
  1. El sistema muestra las autorizaciones activas.
  1. A-01 elige otorgar o revocar un permiso de acceso.
  1. El sistema actualiza el consentimiento y lo registra con fecha y alcance (RNF-02).
- **Flujo alternativo**: si A-01 revoca una autorización, el acceso del tercero termina de inmediato.
- **Postcondición**: los accesos de terceros reflejan la decisión del paciente.
