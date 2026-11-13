# Modelo de datos
Fuente: informe 3.4.2 y `script_sqlserver.sql` (20 tablas, T-SQL, **nunca ejecutado en SQL Server**).

Tablas: establecimiento, suscripcion, rol, permiso, rol_permiso, usuario, profesional_salud, paciente, representante_legal, historia_clinica, atencion, antecedente, alergia, diagnostico, tratamiento, resultado_examen, enmienda, consentimiento_acceso, registro_auditoria, solicitud_intercambio.

Convenciones: nombres en minúsculas y singular; PK `id_<entidad>`; FK con el nombre de la clave referenciada; estados como texto corto; contraseñas como hash (`contrasena_hash`); 3FN.
- `registro_auditoria` guarda `entidad_afectada` e `id_entidad_afectada` **sin FK** a propósito (auditar cualquier tabla y sobrevivir a cambios).
- `usuario.id_establecimiento` y `paciente.id_usuario` admiten NULL (el admin SaaS y los pacientes no pertenecen a un hospital).
- El código CIE-10 se valida en la aplicación (RN-10), no en la BD.

## Estrategia para el software
- Demo: H2 en archivo, esquema generado por JPA (`ddl-auto=update`). Las entidades JPA deben respetar los nombres de este modelo.
- SQL Server: perfil `sqlserver`, opcional y sin probar. Usar este script como referencia.
