# Contrato de la API: auditoría e indicadores (v5)
Cubre RF-13, RN-07, RNF-08 (auditoría) y RF-19, RF-20 (indicadores). Para las pantallas Auditoría, Inicio por rol, Tablero del admin y Hospitales (Alexander: A-5, A-10, A-11).
Todo requiere sesión iniciada (**401**). Errores con cuerpo `{"mensaje": "..."}`. Sin permiso del rol: **403**.

## 1. Auditoría · solo ADMIN_HOSPITAL · `GET /api/auditoria`
Parámetros opcionales: `usuario` (parte del nombre), `accion` (exacta), `nroHc`, `desde` y `hasta` (`AAAA-MM-DD`, ambos incluidos), `pagina` (desde 0) y `tamano` (por defecto 20, máximo 100).
Solo muestra lo de **su hospital**: lo hecho por usuarios de su hospital y lo que toque historias de su hospital (así aparece también cuando un paciente consulta su historia). Del más reciente al más antiguo.
```json
{ "registros": [
    { "id": 5, "fechaHora": "2026-11-16T10:15:00", "usuario": "medico.a", "rol": "Médico",
      "accion": "CONSULTAR_HISTORIA", "entidadAfectada": "historia_clinica", "idEntidadAfectada": 1,
      "nroHc": "HC-000001", "ip": "0:0:0:0:0:0:0:1" } ],
  "pagina": 0, "tamano": 20, "total": 4, "totalPaginas": 1 }
```
`nroHc` es `null` cuando la acción no toca una historia (por ejemplo `INICIO_SESION`).
Acciones que existen hoy: `INICIO_SESION`, `LOGIN_FALLIDO` (solo de usuarios que existen), `CIERRE_SESION`, `REGISTRAR_PACIENTE_Y_ABRIR_HISTORIA`, `VER_HISTORIA_EXISTENTE`, `CONSULTAR_HISTORIA`, `CREAR_ATENCION`, `AGREGAR_DIAGNOSTICO`, `CERRAR_ATENCION`, `ENMENDAR_ATENCION`, `CONSULTAR_ATENCION`, `CONSULTAR_MI_HISTORIAL`, `VER_MI_RESUMEN` (más las de tratamiento y consentimiento cuando Mattias las termine).
`GET /api/auditoria/acciones` → lista de textos con las acciones que hay en el hospital, para el filtro.
La auditoría es **solo lectura**: no hay forma de modificar ni borrar un registro (POST, PUT o DELETE dan 405).
Limitación conocida: los accesos **denegados** (403) todavía no se registran.

## 2. Inicio por rol · `GET /api/indicadores/inicio` · Admisión, Médico, Enfermería, Administrador del hospital
Devuelve las tarjetas del Inicio **según el rol**, con cifras reales del hospital del usuario. La pantalla solo dibuja la lista (`clave`, `titulo`, `valor`, `nota`).
```json
{ "establecimiento": "Hospital Demo A",
  "tarjetas": [ { "clave": "mis-atenciones-hoy", "titulo": "Mis atenciones de hoy", "valor": 2, "nota": "Registradas por ti hoy" },
                { "clave": "abiertas-por-cerrar", "titulo": "Atenciones abiertas por cerrar", "valor": 2, "nota": "Pendientes de cerrar" },
                { "clave": "pacientes", "titulo": "Pacientes del hospital", "valor": 4, "nota": "En tu hospital" } ] }
```
| Rol | Tarjetas (`clave`) |
|---|---|
| Admisión | `pacientes`, `historias-abiertas`, `registrados-hoy` |
| Médico | `mis-atenciones-hoy`, `abiertas-por-cerrar`, `pacientes` |
| Enfermería | `pacientes`, `historias-abiertas` |
| Admin del hospital | `pacientes`, `atenciones-hoy`, `atenciones-abiertas`, `usuarios` |
Paciente y plataforma: **403** (no tienen Inicio de hospital).

## 3. Tablero del Administrador del hospital · `GET /api/indicadores/tablero` · solo ADMIN_HOSPITAL (DEC-10)
```json
{ "establecimiento": "Hospital Demo A",
  "atencionesPorDia": [ { "fecha": "2026-11-03", "total": 0 }, "... 14 días, el último es hoy ..." ],
  "pacientesNuevosPorSemana": [ { "fecha": "2026-10-26", "total": 1 }, "... 8 semanas; fecha = lunes de la semana ..." ],
  "diagnosticosFrecuentes": [ { "codigoCie10": "J02.9", "descripcion": "Faringitis aguda, no especificada", "total": 3 } ],
  "atencionesAbiertas": 2, "atencionesCerradas": 6,
  "ultimosAccesos": [ { "fechaHora": "2026-11-16T10:15:00", "usuario": "medico.a", "accion": "CONSULTAR_HISTORIA", "nroHc": "HC-000001" } ] }
```
Los días y semanas **sin datos vienen con `total: 0`** (el eje no se rompe); la pantalla muestra "Aún no hay atenciones" si todo es cero. `diagnosticosFrecuentes` trae hasta 5; `ultimosAccesos`, hasta 5 (acciones que tocan una historia). Otro rol: **403**.

## 4. Panel Hospitales · `GET /api/indicadores/hospitales` · solo ADMIN_PLATAFORMA (DEC-11)
```json
[ { "id": 1, "nombre": "Hospital Demo A", "codigoIpress": "0000001", "region": "Lima", "estado": "ACTIVO",
    "usuarios": 4, "pacientes": 4, "atenciones": 8 } ]
```
**Solo totales**: nunca datos clínicos ni identificadores de pacientes. Es la única vista que cruza hospitales. No trae el plan del hospital porque la tabla `suscripcion` aún no tiene entidad en el código. Otro rol (incluido el admin del hospital): **403**.
