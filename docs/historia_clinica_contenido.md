# Contenido de la historia clínica (7 secciones)

Fuente: la estructura que definió Pablo (las 7 secciones) y la tabla de brechas elaborada por Claude Code. **No tengo el documento original de las 7 secciones**: los campos marcados con (propuesta) son una elaboración mía y deben verificarse contra esa fuente y la norma técnica de historias clínicas antes de ponerlos en el informe.

Tramos: **A** = imprescindible, **B** = importante, **C** = recortado de entrada.
Regla de pantalla: sin siglas ni abreviaturas en la interfaz (escribir "Presión arterial", no "PA"), y descripciones completas en el catálogo CIE-10.

| # | Sección | Contenido | Tramo | Quién lo llena |
|---|---|---|---|---|
| 1 | **Filiación** | Ya existe: tipo y número de documento, nombres y apellidos, fecha de nacimiento, sexo, teléfono, correo, dirección. **Falta:** lugar de nacimiento, procedencia, estado civil, grado de instrucción, ocupación, religión, acompañante o contacto de emergencia (nombre, parentesco, teléfono) | A | Admisión |
| 2 | **Anamnesis** | Ya existe: motivo de consulta, texto libre, alergias, antecedentes. **Falta:** enfermedad actual estructurada (tiempo de enfermedad, forma de inicio, curso, relato) (propuesta); funciones biológicas (apetito, sed, sueño, deposiciones, micciones) (propuesta); antecedentes por tipo: personales, quirúrgicos, vacunas, hábitos, familiares | A | Enfermería (antecedentes, alergias) y médico (motivo, enfermedad actual) |
| 3 | **Examen físico** | **Signos vitales:** presión arterial, frecuencia cardíaca (pulso), frecuencia respiratoria, temperatura, saturación de oxígeno. **Examen por sistemas** en texto: general, piel, cabeza y cuello, tórax y pulmones, cardiovascular, abdomen, neurológico, osteomuscular (propuesta de lista) | A | Enfermería (signos vitales), médico (sistemas) |
| 4 | **Diagnóstico** | Código CIE-10 del catálogo + descripción completa; tipo presuntivo o definitivo (propuesta). Sin siglas | A | Médico |
| 5 | **Plan** | Tratamiento e indicaciones (ya existe). **Pedido de exámenes auxiliares** (sube a tramo A porque sin él apoyo al diagnóstico no tiene función). Procedimientos y seguimiento (próximo control) | A (tratamiento y pedido de exámenes) / B (procedimientos y seguimiento) | Médico |
| 6 | **Evolución** | Notas de progreso por fecha y autor. Las correcciones siguen como enmiendas. Epicrisis: solo hospitalización | B / C | Médico |
| 7 | **Anexos** | Resultados de exámenes (A, vía A-05). Consentimiento informado (B). Formatos por etapa de vida (C) | A / B / C | Apoyo al diagnóstico, médico |

## Valores de validación
Los signos vitales se validan contra rangos de seguridad (rechazar valores imposibles, p. ej. temperatura negativa). **Los rangos concretos los define el equipo y se marcan como "de demostración, no clínicos"**: no los invento.
