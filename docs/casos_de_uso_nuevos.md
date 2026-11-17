# Casos de uso nuevos
Estado: **PROPUESTA por aprobar**. Formato igual al de `casos_de_uso.md`. Los CU-01 a CU-15 existen en el Kit Base; los nuevos empiezan en CU-16. **Verificar en el Kit Base** si alguno de estos (p. ej. gestión de usuarios, hospitales, resultados, auditoría) ya figura entre los CU-02, 07, 09, 12, 14 o 15 para no duplicar; si es así, se amplía el existente en lugar de crear uno nuevo.
Precondición general: sesión iniciada (CU-01), salvo CU-18.

### CU-16: Dar de alta un hospital
- **Actor principal**: Administrador de plataforma (A-07).
- **Requisitos**: RF-20. **Reglas**: RN-16, RN-14, RN-07.
- **Flujo principal:**
  1. A-07 entra a "Hospitales" y elige "Nuevo hospital".
  2. Ingresa nombre, código del establecimiento y plan de suscripción.
  3. Ingresa los datos del administrador del hospital (nombre, documento, correo).
  4. El sistema crea el establecimiento y el administrador con clave temporal, y la muestra una sola vez.
  5. El sistema registra la auditoría.
- **Alternativo**: si el código del establecimiento ya existe, se rechaza.
- **Postcondición**: existe el hospital, su suscripción y su administrador con cambio de clave pendiente.

### CU-17: Gestionar usuarios del hospital
- **Actor principal**: Administrador del hospital (A-06).
- **Requisitos**: RF-11, RF-12. **Reglas**: RN-16, RN-14, RN-12, RN-07.
- **Flujo principal:**
  1. A-06 entra a "Usuarios" y elige "Nuevo usuario".
  2. Ingresa nombre, documento, correo y elige el rol (admisión, enfermería, médico, apoyo al diagnóstico; para el personal clínico, número de colegiatura).
  3. El sistema crea el usuario en el hospital de A-06 con clave temporal.
  4. A-06 puede desactivar o reactivar a un usuario.
- **Alternativo**: A-06 no puede crear usuarios de otro hospital ni un administrador de plataforma (RN-16).
- **Postcondición**: el usuario existe, solo en ese hospital, con cambio de clave pendiente.

### CU-18: Cambiar contraseña en el primer ingreso
- **Actor principal**: cualquier usuario con clave temporal.
- **Requisitos**: RF-28, RF-10. **Reglas**: RN-14, RN-12.
- **Flujo principal:**
  1. El usuario inicia sesión con su clave temporal.
  2. El sistema lo envía a "Cambiar contraseña" y no le deja usar otra pantalla.
  3. El usuario ingresa la nueva contraseña según la política.
  4. El sistema guarda el hash y lo lleva a su pantalla de inicio.
- **Alternativo**: si la nueva contraseña no cumple la política o es igual a la temporal, se rechaza.

### CU-19: Crear la cuenta del portal del paciente (extiende CU-03)
- **Actor principal**: Admisión (A-04).
- **Requisitos**: RF-27. **Reglas**: RN-15, RN-14.
- **Flujo principal:**
  1. Al registrar un paciente nuevo (CU-03), el sistema crea su cuenta: usuario = documento.
  2. El sistema genera una clave temporal y la muestra una sola vez en una constancia imprimible.
  3. A-04 entrega la constancia al paciente.
- **Alternativo**: si el paciente ya tiene cuenta (RN-15), no se crea otra.

### CU-20: Admitir al paciente del día
- **Actor principal**: Admisión (A-04).
- **Requisitos**: RF-21. **Reglas**: RN-13, RN-06, RN-07.
- **Flujo principal:**
  1. A-04 busca al paciente (CU-04) y elige "Admitir".
  2. Elige el tipo de atención: consulta externa o emergencia.
  3. El sistema crea la admisión en estado `ADMITIDO` y la muestra en la cola.
- **Alternativo**: si el paciente ya tiene una admisión activa hoy, se rechaza y se muestra la existente.
- **Nota**: no es agenda de citas: solo registra a quien ya está presente.

### CU-21: Atender la cola y registrar signos vitales
- **Actor principal**: Enfermería (A-03).
- **Requisitos**: RF-22, RF-23, RF-07. **Reglas**: RN-13, RN-17, RN-06, RN-07.
- **Flujo principal:**
  1. A-03 ve la cola de admitidos (los de emergencia primero).
  2. Toma un paciente: su estado pasa a `EN_ENFERMERIA`.
  3. Registra signos vitales (presión arterial, frecuencia cardíaca, frecuencia respiratoria, temperatura, saturación de oxígeno).
  4. Registra o revisa antecedentes y alergias.
  5. Confirma: el estado pasa a `LISTO_PARA_ATENCION`.
- **Alternativo**: valores fuera de rango se rechazan con mensaje claro (RN-17).

### CU-05 (ampliado): Registrar atención médica
- **Cambios**: la precondición pasa a "el paciente está `LISTO_PARA_ATENCION`; al comenzar, el estado pasa a `EN_ATENCION`". El flujo registra, en orden: motivo y enfermedad actual, funciones biológicas, examen físico por sistemas, diagnóstico CIE-10 (con descripción completa), y al cerrar la atención el estado pasa a `ATENCION_CERRADA` (RN-03). Se mantienen RN-02, RN-10, RN-11.

### CU-22: Solicitar exámenes auxiliares
- **Actor principal**: Médico (A-02).
- **Requisitos**: RF-25. **Reglas**: RN-02, RN-18, RN-07.
- **Flujo principal:**
  1. Durante una atención abierta, A-02 elige "Solicitar examen".
  2. Ingresa el tipo de examen y las indicaciones.
  3. El sistema crea la solicitud en estado `SOLICITADO` y la muestra a apoyo al diagnóstico.
- **Alternativo**: no se puede solicitar sobre una atención cerrada (se usa enmienda, RN-03), ni sobre una atención ajena o de otro hospital.
- Implementado como `ExamenService.solicitar` (D-41).

### CU-23: Registrar resultado de un examen
- **Actor principal**: Apoyo al diagnóstico (A-05).
- **Requisitos**: RF-09. **Reglas**: RN-18, RN-06, RN-07.
- **Flujo principal:**
  1. A-05 ve las solicitudes pendientes de su hospital.
  2. Elige una e ingresa el resultado (texto, y fecha).
  3. El sistema lo vincula a la solicitud (estado `RESULTADO_REGISTRADO`) y a la historia.
- **Alternativo**: no se puede registrar un resultado sobre una solicitud de otro hospital.

### CU-24: Registrar nota de evolución (tramo B)
- **Actor principal**: Médico (A-02).
- **Requisitos**: RF-26. **Reglas**: RN-02, RN-03, RN-07.
- **Flujo principal:** A-02 abre la historia, agrega una nota con fecha y autor; las notas no se editan, se enmiendan.

### CU-25: Consultar auditoría y reportes
- **Actor principal**: Administrador del hospital (A-06).
- **Requisitos**: RF-13, RF-19. **Reglas**: RN-07, RN-06.
- **Flujo principal:** A-06 filtra el registro de accesos por usuario, fecha o paciente; y ve el reporte de atenciones por día.
- **Alternativo**: solo ve registros de su hospital.

### CU-26: Restablecer la clave de una cuenta
- **Actores**: Administrador del hospital (A-06), Administrador de plataforma (A-07), Admisión (A-04).
- **Requisitos**: RF-30. **Reglas**: RN-14, RN-16, RN-06, RN-07, RN-12.
- **Flujo principal:**
  1. El actor elige "Restablecer clave" sobre una cuenta que le corresponde: A-06 al personal de su hospital; A-07 al administrador de un hospital; A-04 a un paciente de su hospital.
  2. El sistema pide confirmación, genera una clave temporal y guarda solo su hash.
  3. Muestra la clave **una sola vez**, deja la cuenta con cambio obligatorio en el primer ingreso (RN-14) y levanta el bloqueo por intentos fallidos (RN-12).
  4. Registra la acción `RESTABLECER_CLAVE` en la auditoría (RN-07).
- **Alternativo**: una cuenta de otro hospital se trata como inexistente (RN-06, RN-16); nadie restablece su propia clave por esta vía (la cambia con CU-18); A-06 no restablece claves de administradores.
- Decisión D-39 (no estaba en los documentos originales).

## Trazabilidad CU nuevo → pasos del recorrido
CU-16: paso 1 · CU-17, CU-18: paso 2 · CU-19: paso 3 · CU-20: paso 4 · CU-21: paso 5 · CU-05, CU-22: paso 6 · CU-23: paso 7 · CU-05 (cierre), CU-24: paso 8 · CU-10, CU-11, CU-18: paso 9 · CU-25: transversal · CU-26: transversal (cuentas).
