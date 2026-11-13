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
- Admisión: Inicio, Pacientes, Registrar paciente.
- Médico / Enfermería: Inicio, Pacientes, Historia clínica, Registrar atención.
- Paciente: Mi historial, Consentimiento y privacidad.
- Administrador del hospital (v5): Usuarios y roles, Auditoría, Reportes.
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
| Reportes (opcional) | gráficos de barras | Atenciones por día; solo si sobra tiempo |

## Reglas
- Íconos: SVG en línea (o un set libre incluido en el repo). Sin fotos de personas: avatares con iniciales.
- Todos los textos en español. Datos de la demo ficticios y marcados ("Paciente de prueba").
- Responsive básico (RNF-10), pero la prioridad es que se vea bien en pantalla de laptop y proyector.
- Contraste suficiente para proyector (evitar grises muy claros en texto).
