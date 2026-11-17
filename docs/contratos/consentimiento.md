# Contrato de la API: consentimiento de acceso (CU-11)
Cubre CU-11 (RF-17; RN-04, RN-06, RN-07, RN-09). **Estado: implementado** (rama `feat/consentimiento`; la hizo Pablo porque Mattias no llegó a M-5), con la pantalla P-06 en `static/consentimiento/`.
Código: `ConsentimientoAcceso`, `ConsentimientoAccesoRepository`, `ConsentimientoService`, `ConsentimientoController`, `ConsentimientoRequest`, `ConsentimientoResponse` y `ConsentimientoServiceTest` (7 tests, CP-19 y CP-20). Al listar se audita con la entidad `paciente` (la columna `id_entidad_afectada` no admite nulo).

Reglas:
- Solo el rol `PACIENTE` (otro rol: **403**). Sin sesión: **401**.
- El paciente trabaja **solo sobre sus consentimientos** (RN-04): el servicio busca al paciente por la cuenta en sesión (`PacienteRepository.findByUsuarioId`); **nunca** acepta un id de paciente del cliente.
- Estados `ACTIVO` y `REVOCADO` (DEC-13). Revocar guarda `fechaRevocado`; revocar uno ya revocado: **400**.
- Consentimiento de otro paciente o inexistente: **404**. Datos vacíos: **400** (mensajes en español en `ConsentimientoRequest`).
- Otorgar y revocar se auditan (RN-07).
- El intercambio real entre hospitales (RF-18) está fuera del alcance (DEC-02): aquí solo se registra y se muestra la decisión del paciente.

## 1. Listar · `GET /api/consentimientos` → **200** lista de `ConsentimientoResponse`
```json
[ { "id": 2, "otorgadoA": "Hospital Demo B", "alcance": "Lectura de mi historial para una interconsulta",
    "fechaOtorgado": "2026-11-16T10:00:00", "fechaRevocado": null, "estado": "ACTIVO", "activo": true } ]
```
El más reciente primero. La pantalla los muestra como lista con interruptores y fecha.

## 2. Otorgar · `POST /api/consentimientos` — cuerpo `ConsentimientoRequest`
```json
{ "otorgadoA": "Hospital Demo B", "alcance": "Lectura de mi historial para una interconsulta" }
```
→ **201** `ConsentimientoResponse` con `estado: "ACTIVO"`.

## 3. Revocar · `POST /api/consentimientos/{id}/revocacion` (sin cuerpo)
→ **200** `ConsentimientoResponse` con `estado: "REVOCADO"` y `fechaRevocado`.

## Archivos que toca Mattias (M-5)
`aplicacion/ConsentimientoService.java` (solo los cuerpos), `presentacion/ConsentimientoController.java` (nuevo), `src/test/.../ConsentimientoServiceTest.java`, `static/consentimiento/index.html` y `static/consentimiento/consentimiento.js`. No toca `ManejadorErrores`, `Permiso`, `DatosIniciales` ni las entidades. Para armar el escenario del test (paciente con cuenta vinculada), copia el de `PortalPacienteServiceTest`.
