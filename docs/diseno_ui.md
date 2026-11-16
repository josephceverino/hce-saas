# Diseño de interfaz (brief)
Objetivo: que se vea como un panel profesional de salud, consistente entre las 6 pantallas (P-01 a P-06) y las de apoyo.
**Referencia visual:** capturas de un panel de administración moderno (sidebar, tarjetas con indicadores, tablas con etiquetas de estado, login a dos columnas, perfil con línea de tiempo). **Se imita el estilo, no se copia código, textos, logos ni fotos de la plantilla.**

## Tokens (CSS variables en `static/css/estilo.css`)
- Color primario: verde azulado (referencia `#0f9d94`), variante oscura para el panel izquierdo del login, variante clara (`#e6f6f4`) para fondos de ítem activo.
- Estados: verde = activo/ok, ámbar = pendiente, rojo = alerta/inactivo, azul = informativo.
- Fondo de página gris muy claro; tarjetas blancas, radio 12 a 14 px, sombra suave.
- Tipografía: una sans-serif limpia **incluida localmente** (no Google Fonts). Títulos de página 28 px, texto 14 a 15 px.
- Espaciado en múltiplos de 4 u 8 px.

## Layout común
Sidebar fijo a la izquierda (logo del sistema, menú según el rol, perfil y cerrar sesión abajo) + barra superior (buscador de pacientes, hospital actual, usuario). Contenido en tarjetas.
El nombre del hospital activo se ve siempre (refuerza el multi-tenant en la demo).

## Menú por rol (solo lo que existe en el alcance)
El **servidor** decide el menú (`MenuService`) y lo entrega en `menu` dentro de `/api/login` y `/api/sesion` (cada opción: `clave`, `titulo`, `ruta`). El JS solo lo dibuja; no decide permisos. Ocultar una opción no es seguridad: cada servicio sigue exigiendo su permiso.

| Rol | Menú |
|---|---|
| Admisión | Inicio, Pacientes, Registrar paciente |
| Médico | Inicio, Pacientes, Historia clínica, Registrar atención |
| Enfermería | Inicio, Pacientes, Historia clínica (**sin** Registrar atención, RN-02) |
| Paciente | Mi historial, Consentimiento |
| Administrador del hospital (v5) | Inicio (tablero con gráficos), Usuarios y roles, Auditoría, Reportes |
| Administrador de plataforma (v5) | Hospitales |
| Apoyo al diagnóstico | Sin casos de uso en el alcance: solo Mi perfil |
| Todos | **Mi perfil** (al final) |

**Prohibido en el menú:** calendario, citas, chat, correo, kanban, facturación, telemedicina (fuera de alcance).

## Componentes reutilizables (un archivo CSS, clases con prefijo `hce-`)
Tarjeta con indicador (título, número, nota) · tabla con paginación y chips de estado · formulario con validación visible · cabecera de paciente (iniciales en círculo, DNI, edad, alergias como chips rojos, antecedentes) · línea de tiempo de atenciones (cerrada = candado + sin botón de editar) · aviso de error/éxito · modal de confirmación.

## Mapa de pantallas
| Pantalla | Basada en | Notas |
|---|---|---|
| P-01 Login | login a dos columnas | Panel izquierdo con nombre del sistema y 3 beneficios reales; sin Google ni GitHub; botones de cuentas demo |
| Inicio | tablero de indicadores | Pacientes registrados, atenciones del día, historias abiertas; actividad reciente (de la auditoría cuando exista) |
| P-02 Buscar paciente | directorio de personas | Buscar por DNI o nombre, chips de estado, paginación |
| P-04 Historia clínica | perfil de usuario | Cabecera del paciente + línea de tiempo de atenciones + antecedentes y alergias |
| P-03 Registrar atención | formulario en tarjetas | Motivo, anamnesis, examen, diagnóstico CIE-10, tratamiento; botón Cerrar atención con confirmación |
| P-05 Mi historial | variante simplificada de P-04 | Solo lectura; botón de resumen |
| P-06 Consentimiento | lista con interruptores | Otorgar o revocar con fecha |
| Registrar paciente | formulario en tarjeta | Pantalla aparte de P-02; confirmación con el N.° de HC; mensajes distintos para 201, 200 y 409 (DEC-02) |
| Mi perfil (todos) | perfil de usuario | Usuario, rol, hospital y último acceso; refuerza el multi-tenant en la demo |
| Tablero del Administrador del hospital (v5, DEC-10) | tablero con gráficos | Atenciones por día (barras), pacientes nuevos por semana, 5 diagnósticos CIE-10 más frecuentes, atenciones abiertas frente a cerradas, últimos accesos (de la auditoría) |
| Hospitales (Administrador de plataforma, v5, DEC-11) | tarjetas por hospital | Plan, estado, n.° de usuarios y n.° de pacientes. **Solo totales, nunca datos clínicos** |
| Auditoría (v5) | tabla filtrable | Quién accedió a qué y cuándo (D-11) |
| Reportes (opcional) | gráficos de barras | Atenciones por día; solo si sobra tiempo. Se recorta primero |

## Inicio por rol (tarjetas)
- **Admisión:** pacientes registrados, historias abiertas, registrados hoy.
- **Médico:** mis atenciones de hoy y **atenciones abiertas pendientes de cerrar** (lo más útil para el médico).
- **Enfermería:** pacientes del hospital e historias abiertas.
- **Administrador del hospital:** el tablero con gráficos (arriba).

## Reglas de los tableros (DEC-10)
- **Cada cifra sale de la base de datos**, con el filtro por hospital. Ninguna se inventa.
- **Gráficos en SVG dibujado con JS propio**, sin librerías ni CDN: bastan barras y una dona.
- **Estado vacío claro:** sin datos se muestra "Aún no hay atenciones", nunca un gráfico de mentira.
- **Ningún gráfico de más:** si no responde una pregunta del usuario, se quita.
- Si el endpoint de una cifra aún no existe, la tarjeta no se muestra.

## Prioridad para la demo
- **Imprescindible:** Login, Pacientes, Registrar paciente, Historia, Atención, Mi historial, Consentimiento.
- **Importante:** Inicio por rol con tarjetas, Mi perfil.
- **Si hay tiempo:** tablero con gráficos, Auditoría, Hospitales.
- **Se recorta primero:** Reportes aparte y Usuarios y roles.

## Reglas
- Íconos: SVG en línea (o un set libre incluido en el repo). Sin fotos de personas: avatares con iniciales.
- Todos los textos en español. Datos de la demo ficticios y marcados ("Paciente de prueba").
- Una atención cerrada se muestra con candado y **sin** botón de editar; se corrige con "Agregar enmienda" (RN-03).
- Responsive básico (RNF-10), pero la prioridad es que se vea bien en pantalla de laptop y proyector.
- Contraste suficiente para proyector (evitar grises muy claros en texto).
