# Contrato de la API: historia clínica, atención, tratamiento y CIE-10
Cubre CU-06, CU-05 y CU-08 (RF-05, RF-06, RF-08; RN-02, RN-03, RN-06, RN-07, RN-10, RN-11).
**Estado:** historia y atención (secciones 1 a 5) **implementadas** en `feat/v3-atencion`. Tratamiento y CIE-10 (secciones 6 y 7) los implementa Mattias; hasta entonces el frontend usa los datos de ejemplo. Mientras no esté el catálogo CIE-10 (M-4), agregar un diagnóstico responde 500.

Reglas comunes:
- Todo requiere sesión iniciada (si no: **401**). Errores con cuerpo `{"mensaje": "texto en español"}`.
- Sin permiso del rol: **403**. Historia de otro hospital: **403** (RN-06). Atención de otro hospital o inexistente: **404** (no se revela que existe). Historia inexistente: **404**.
- Diagnóstico, cierre y tratamiento: solo el médico que creó la atención (si no: **403**). La enmienda la puede registrar cualquier médico del hospital; queda su autor.
- Datos inválidos: **400**. Atención cerrada: **409** (RN-03).
- Valores asumidos que **Pablo debe confirmar**: estado de atención `ABIERTA`/`CERRADA`; tipo de diagnóstico `PRINCIPAL`/`SECUNDARIO`.

## 1. Consultar historia (CU-06) · MEDICO, ENFERMERIA
`GET /api/historias/{idHistoriaClinica}` → **200** `HistoriaDetalleResponse`
```json
{
  "idHistoriaClinica": 1, "nroHc": "HC-000001", "estado": "ABIERTA",
  "fechaApertura": "2026-11-16", "establecimiento": "Hospital Demo A",
  "paciente": { "tipoDocumento": "DNI", "nroDocumento": "12345678", "nombres": "Paciente de prueba",
                "apellidos": "Ficticio", "fechaNacimiento": "1990-05-01", "sexo": "F" },
  "alergias": [ { "agente": "Penicilina", "reaccion": "Urticaria", "gravedad": "ALTA" } ],
  "antecedentes": [ { "tipo": "Personal", "descripcion": "Asma leve", "fechaRegistro": "2026-11-01" } ],
  "atenciones": [
    { "id": 7, "fechaHora": "2026-11-16T09:30:00", "motivo": "Dolor de garganta", "anamnesis": "…",
      "examenFisico": "…", "estado": "CERRADA", "cerrada": true, "profesional": "Pérez, Ana",
      "diagnosticos": [ { "id": 3, "codigoCie10": "J02.9", "descripcion": "Faringitis aguda, no especificada", "tipo": "PRINCIPAL" } ],
      "tratamientos": [ { "id": 2, "indicacion": "Reposo", "medicamento": "Paracetamol", "dosis": "500 mg c/8 h", "duracion": "5 días" } ],
      "enmiendas": [ { "id": 1, "motivo": "Error de dosis", "contenidoCorregido": "…", "fecha": "2026-11-16T11:00:00", "autor": "Pérez, Ana" } ] }
  ]
}
```
Las atenciones vienen de la más reciente a la más antigua. `cerrada: true` ⇒ mostrar candado y **sin** botón de editar.
Cada lectura queda auditada (RN-07).

## 1b. Ver una atención · MEDICO, ENFERMERIA
`GET /api/atenciones/{idAtencion}` → **200** `AtencionDto` (mismo formato que cada elemento de `atenciones`). Atención de otro hospital: **404**.

## 2. Crear atención (CU-05) · solo MEDICO
`POST /api/atenciones` — cuerpo `AtencionRequest`:
```json
{ "idHistoriaClinica": 1, "motivo": "Dolor de garganta", "anamnesis": "Hace 3 días…", "examenFisico": "Faringe eritematosa" }
```
→ **201** `AtencionDto` (estado `ABIERTA`, sin diagnósticos). Enfermería o admisión: **403**.

## 3. Agregar diagnóstico (CU-05) · solo MEDICO
`POST /api/atenciones/{idAtencion}/diagnosticos` — cuerpo `DiagnosticoRequest`:
```json
{ "codigoCie10": "J02.9", "tipo": "PRINCIPAL" }
```
→ **201** `DiagnosticoDto`. Código fuera del catálogo: **400** (RN-10). Atención cerrada: **409**.
Se pueden agregar varios diagnósticos a una misma atención.

## 4. Cerrar atención (CU-05) · solo MEDICO
`POST /api/atenciones/{idAtencion}/cierre` (sin cuerpo) → **200** `AtencionDto` con `estado: "CERRADA"`.
Sin diagnósticos: **400**. Ya cerrada: **409**. Después de cerrar ya no se edita (RN-03).
La pantalla pide confirmación antes de llamar.

## 5. Enmienda (RN-03) · solo MEDICO
`POST /api/atenciones/{idAtencion}/enmiendas` — cuerpo `EnmiendaRequest` (texto libre):
```json
{ "motivo": "Error de dosis", "contenidoCorregido": "Paracetamol 500 mg cada 8 horas" }
```
→ **201** `EnmiendaDto` (con `autor` y `fecha` puestos por el servidor). Atención aún abierta: **400**.

## 6. Tratamiento e indicaciones (CU-08) · solo MEDICO · *backend: Mattias*
`POST /api/atenciones/{idAtencion}/tratamientos` — cuerpo `TratamientoRequest`:
```json
{ "indicacion": "Reposo e hidratación", "medicamento": "Paracetamol", "dosis": "500 mg cada 8 horas", "duracion": "5 días" }
```
→ **201** `TratamientoDto`. Atención cerrada: **409** (usar enmienda). Enfermería: **403**.

## 7. Buscar diagnóstico CIE-10 (RN-10) · solo MEDICO · *backend: Mattias*
`GET /api/cie10?q=faringitis` → **200** lista de máximo 20 elementos:
```json
[ { "codigo": "J02.9", "descripcion": "Faringitis aguda, no especificada" } ]
```
Texto vacío ⇒ lista vacía. Catálogo de **demostración** (`cie10_demo.csv`).

## Dónde está cada cosa en el código
| Pieza | Clase | Responsable |
|---|---|---|
| Historia (CU-06) | `HistorialService`, `HistoriaDetalleResponse` | Pablo |
| Atención (CU-05) | `AtencionService`, `AtencionRequest`, `DiagnosticoRequest`, `EnmiendaRequest` | Pablo |
| Tratamiento (CU-08) | `TratamientoService`, `TratamientoRequest` | Mattias (M-2) |
| CIE-10 (RN-10) | `CatalogoCie10Service`, `Cie10` | Mattias (M-4) |
| Errores HTTP | `ManejadorErrores` (ya incluye 404, 409 y 400 de estas reglas) | Pablo (nadie más lo edita) |
