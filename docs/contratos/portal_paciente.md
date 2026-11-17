# Contrato de la API: portal del paciente (CU-10)
Cubre CU-10 (RF-14, RF-15, RF-16; RN-04, RN-07). **Estado: implementado** (rama `feat/v4`). Para la pantalla P-05 (Alexander, A-6).

Reglas:
- Rol `PACIENTE` solamente (cualquier otro rol: **403**). Sin sesión: **401**.
- **Ningún endpoint recibe un id.** La historia sale de la cuenta en sesión (RN-04). Es solo lectura: solo `GET`; cualquier otro método responde 405.
- El paciente ve **solo atenciones cerradas** (DEC-13). Sin historia abierta: **404** con "Aún no tienes una historia clínica abierta.". Cuenta sin paciente vinculado: **403**.
- Cada consulta se audita (RN-07).
- RN-05 (representante legal) no está implementada (DEC-05).

Cuentas demo (clave `demo123`): `paciente.a` (Hospital A, DNI 90000001) y `paciente.b` (Hospital B, DNI 90000005). Su `establecimiento` en `/api/sesion` es "Portal del paciente" y su menú es `mi-historial`, `consentimiento`, `perfil`.

## 1. Mi historial · `GET /api/mi-historial`
→ **200**, el mismo formato `HistoriaDetalleResponse` de `docs/contratos/atencion_historia.md` (sección 1), pero con `atenciones` solo cerradas. Todas tienen `cerrada: true`, así que la pantalla **no muestra ningún botón de edición**.

## 2. Mi resumen · `GET /api/mi-historial/resumen`
→ **200** `ResumenHistorialResponse`, para mostrar en pantalla o imprimir (botón "Ver resumen"):
```json
{
  "nroHc": "HC-000001", "paciente": "Uno, Paciente de prueba", "tipoDocumento": "DNI", "nroDocumento": "90000001",
  "fechaNacimiento": "1988-04-12", "sexo": "F", "establecimiento": "Hospital Demo A",
  "alergias": [ { "agente": "Penicilina", "reaccion": "Urticaria", "gravedad": "ALTA" } ],
  "antecedentes": [ { "tipo": "Personal", "descripcion": "Asma leve en la infancia", "fechaRegistro": "2026-10-17" } ],
  "totalAtenciones": 2, "ultimaAtencion": "2026-11-14T10:15:00",
  "diagnosticos": [ { "codigoCie10": "J06.9", "descripcion": "Infección aguda de las vías respiratorias superiores, no especificada" } ],
  "generadoEn": "2026-11-16T10:00:00"
}
```
`examenes` (R4.2) trae los exámenes con resultado de las atenciones cerradas: `{ tipoExamen, fechaResultado, resultado }`. `diagnosticos` trae los distintos (por código) de las atenciones cerradas. La edad se calcula en la pantalla a partir de `fechaNacimiento`.
Para "descargar": la pantalla arma una vista imprimible (`window.print()`); no hay endpoint de archivo.
