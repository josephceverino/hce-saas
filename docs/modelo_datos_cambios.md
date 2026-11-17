# Cambios propuestos al modelo de datos
Estado: **PROPUESTA**. Claude Code debe revisar contra el código actual (que ya avanzó) antes de aplicar. Los nombres siguen las convenciones del modelo: minúsculas, singular, `id_<entidad>`.

## Tablas nuevas
| Tabla | Campos principales | Para |
|---|---|---|
| `admision` | `id_admision`, `id_historia_clinica`, `id_establecimiento`, `tipo_atencion` (CONSULTA_EXTERNA/EMERGENCIA), `estado` (RN-13), `fecha_hora_admision`, `id_usuario_admite` | CU-20, cola (RF-22) |
| `signos_vitales` | `id_signos_vitales`, `id_admision`, `presion_sistolica`, `presion_diastolica`, `frecuencia_cardiaca`, `frecuencia_respiratoria`, `temperatura`, `saturacion_oxigeno`, `fecha_hora`, `id_usuario` | CU-21 |
| `solicitud_examen` | `id_solicitud_examen`, `id_atencion`, `id_establecimiento`, `tipo_examen`, `indicaciones`, `estado` (SOLICITADO/RESULTADO_REGISTRADO), `fecha_hora`, `id_usuario` | CU-22 (no confundir con `solicitud_intercambio`) |
| `nota_evolucion` (B) | `id_nota_evolucion`, `id_atencion`/`id_historia_clinica`, `texto`, `fecha_hora`, `id_usuario` | CU-24 |
| `consentimiento_informado` (B) | `id_consentimiento_informado`, `id_atencion`, `texto`, `fecha_hora`, `id_usuario` | RF-29 |

## Columnas nuevas en tablas existentes
| Tabla | Columnas |
|---|---|
| `usuario` | `debe_cambiar_clave` (RN-14), `activo`, `intentos_fallidos`, `bloqueado_hasta` (RN-12), `numero_colegiatura` (personal clínico), `clave_restablecida_en` (CU-26: levanta el bloqueo) |
| `paciente` | `lugar_nacimiento`, `procedencia`, `estado_civil`, `grado_instruccion`, `ocupacion`, `religion`, `contacto_emergencia_nombre`, `contacto_emergencia_parentesco`, `contacto_emergencia_telefono` |
| `atencion` | `id_admision`, `enfermedad_actual`, `funciones_biologicas`, `examen_fisico_sistemas` (texto por sistema, puede ser JSON o columnas) |
| `antecedente` | `tipo` (PERSONAL, QUIRURGICO, VACUNA, HABITO, FAMILIAR) |
| `resultado_examen` | `id_solicitud_examen` (RN-18) |
| `diagnostico` | `tipo` (PRESUNTIVO/DEFINITIVO) (propuesta) |
| `establecimiento` | `codigo` único (CU-16) |

## Compatibilidad
- Con `ddl-auto=update` en H2 se agregan solas; **no** actualiza el script `script_sqlserver.sql`. Dejar nota de que el script de SQL Server quedó desfasado o regenerarlo al final.
- La base H2 de ensayo se vuelve a crear cuando cambien las entidades (borrar `data/`).
- El informe pasa de 20 tablas a 20 + 3 (tramo A) + 2 (tramo B): actualizar 3.4.2, el diagrama ER y el modelo lógico.
