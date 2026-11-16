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
