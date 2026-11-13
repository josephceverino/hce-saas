# Plan de calendario (propuesta)
Demo final: **viernes 11 de diciembre de 2026**, PC del aula con Windows.
Las semanas de sesión (13 a 15) las deduzco del ritmo semanal del cronograma (el que tengo llega a la Sesión 12); confírmalas.

| Fechas | Qué se hace | Quién |
|---|---|---|
| Vie 13 a dom 15 nov | **Fase 0 (base).** Repo en GitHub, semilla migrada y compilando, frontend dividido, `estilo.css` y layout. Mattias y Alexander instalan lo necesario y clonan. | Pablo; Mattias y Alexander: instalación |
| Lun 16 a dom 22 nov (Sesión 12: UML de implementación) | **v3.** Backend de CU-06, CU-05, CU-08. Pantallas P-02 y P-04. Tests de reglas. El UML de implementación se hace sobre el código real. | Pablo backend; Alexander pantallas; Mattias tests |
| Lun 23 a dom 29 nov | **v4.** CU-10 y CU-11 (portal y consentimiento). | Pablo backend; Alexander P-05; Mattias P-06 |
| Lun 30 nov a vie 4 dic | **v5 y primer paquete portable.** Auditoría, roles, BCrypt, `HCE-Demo.exe`. **Viernes 4: congelar código** (solo correcciones). | Pablo |
| Sáb 5 a mié 9 dic | Pruebas en otra PC con Windows, casos de prueba ejecutados con evidencia, video de respaldo, ensayo del guion (mínimo 2 veces). | Todos |
| Jue 10 dic | Ensayo final con la copia que se llevará. Pendrive + laptop cargada. Nada de cambios. | Todos |
| Vie 11 dic | Demo. | Todos |

## Si el tiempo se aprieta, se recorta en este orden
1. Reportes (RF-19).
2. BCrypt y bloqueo por intentos (RN-12): la demo queda con hash simple, y se dice.
3. Auditoría completa: se deja la mínima (quién y cuándo leyó una historia).
**No se recorta:** el filtro por hospital, la atención cerrada inmutable, el portal del paciente y el paquete portable.

## Riesgos
- Alexander trabaja de noche: sus dudas se responden al día siguiente. Por eso las fichas deben ser completas.
- Mattias empieza el domingo 15: menos de 4 semanas de trabajo real.
- La migración a Spring Boot 4.x no la he compilado yo: si en la Fase 0 da problemas, se usa 3.5.x (plan B).
- Sorpresas de Windows con el `.exe` (SmartScreen, antivirus): probar el paquete en otra PC antes del 5 de diciembre.
