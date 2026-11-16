# Matriz de roles y permisos (DEC-03)
Fuente: columna "Actor" de la tabla de RF en `docs/requisitos.md`. Si algo no está allí, no se asigna: se pregunta.

Roles: `PACIENTE` (A-01), `MEDICO` (A-02), `ENFERMERIA` (A-03), `ADMISION` (A-04), `APOYO_DIAGNOSTICO` (A-05), `ADMIN_HOSPITAL` (A-06), `ADMIN_PLATAFORMA` (A-07). A-08 (sistema externo) no es un rol de usuario.

| RF | Permiso | PACIENTE | MEDICO | ENFERMERIA | ADMISION | APOYO_DIAGNOSTICO | ADMIN_HOSPITAL | ADMIN_PLATAFORMA |
|---|---|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| RF-01 | Registrar paciente | | | | ✔ | | | |
| RF-02 | Buscar y consultar paciente | | ✔ | ✔ | ✔ | | | |
| RF-03 | Actualizar datos del paciente | | | | ✔ | | | |
| RF-04 | Abrir historia clínica | | | | ✔ | | | |
| RF-05 | Registrar atención | | ✔ | | | | | |
| RF-06 | Consultar historial clínico | | ✔ | ✔ | | | | |
| RF-07 | Registrar antecedentes y alergias | | ✔ | ✔ | | | | |
| RF-08 | Registrar tratamiento e indicaciones | | ✔ | | | | | |
| RF-09 | Registrar resultados de exámenes | | | | | ✔ | | |
| RF-10 | Autenticarse | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| RF-11 | Gestionar usuarios y roles | | | | | | ✔ | |
| RF-12 | Gestionar permisos por rol | | | | | | ✔ | |
| RF-13 | Registrar auditoría | (sistema) | | | | | | |
| RF-14 | Iniciar sesión en el portal | ✔ | | | | | | |
| RF-15 | Consultar mi historial (solo lectura) | ✔ | | | | | | |
| RF-16 | Ver o descargar mi resumen | ✔ | | | | | | |
| RF-17 | Otorgar o revocar consentimiento | ✔ | | | | | | |
| RF-18 | Intercambio con otros establecimientos | | ✔ | | | | | |
| RF-19 | Reportes de atenciones | | | | | | ✔ | |
| RF-20 | Gestionar hospitales y planes | | | | | | | ✔ |

## Notas
- RF-10 aplica a "Todos". RF-13 lo ejecuta el sistema, no un rol.
- RF-18 tiene como actores A-02 y A-08 (sistema externo). Se asigna a `MEDICO`, pero queda **fuera del alcance de v3 a v5** (DEC-02).
- RF-11, RF-12 y RF-19 solo se implementan a partir de v5 (y RF-19 es lo primero que se recorta, ver `PLAN_CALENDARIO.md`).
- Aislamiento por hospital (RNF-06, RN-06) es independiente del rol: todo rol con datos clínicos filtra por el `id_establecimiento` de la sesión. `PACIENTE` solo ve lo propio (RN-04).
- **v3:** constantes de permiso en `AutorizacionService`. **v5:** se migra a `rol_permiso`.
- Usuarios demo (clave `demo123`): `admision.a`/`admision.b` (`ADMISION`), `medico.a`/`medico.b` (`MEDICO`), `enfermera.a`/`enfermera.b` (`ENFERMERIA`), `admin.a` (`ADMIN_HOSPITAL`, hospital A) y `plataforma` (`ADMIN_PLATAFORMA`, sin hospital). Permisos en `aplicacion/Permiso.java`.
- **Menú por rol** (lo decide el servidor, `aplicacion/MenuService.java`, y llega en `/api/login` y `/api/sesion` como `menu`): Admisión = Inicio, Pacientes, Registrar paciente · Médico = Inicio, Pacientes, Historia clínica, Registrar atención · Enfermería = Inicio, Pacientes, Historia clínica · Paciente = Mi historial, Consentimiento · Admin hospital = Inicio (tablero), Usuarios y roles, Auditoría, Reportes · Admin plataforma = Hospitales. Todos: Mi perfil. Apoyo al diagnóstico: solo Mi perfil (sin casos de uso).
