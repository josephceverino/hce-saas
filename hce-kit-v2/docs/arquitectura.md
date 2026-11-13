# Arquitectura y glosario

## Capas (RNF-11)
1. **presentacion**: controladores REST, DTO (records), validación de entrada, manejo de errores. No accede a repositorios.
2. **aplicacion**: servicios que implementan cada caso de uso; centralizan autorización por rol y consentimiento (AutorizacionService) y auditoría (AuditoriaService).
3. **dominio**: entidades y reglas de negocio (RN-01..RN-12). Ej.: una atención cerrada no se modifica (RN-03); un consentimiento se otorga y se revoca.
4. **persistencia**: repositorios Spring Data JPA (interfaces).

## Multi-tenant (RNF-06, RN-06)
Un solo despliegue y una sola BD atienden a varios hospitales. Toda consulta filtra por el `id_establecimiento` del usuario autenticado. Excepciones: intercambio autorizado por el paciente (RN-06) y emergencia justificada (RN-08).

## Correspondencia análisis → diseño (Tabla 4 del informe)
| Análisis (frontera / control) | Diseño (controlador / servicio) |
|---|---|
| FormularioRegistroPaciente / ControlRegistroPaciente | PacienteController / RegistroPacienteService |
| FormularioAtencion / ControlAtencion | AtencionController / AtencionService |
| FormularioHistorial / ControlHistorial | HistorialController / HistorialService |
| FormularioTratamiento / ControlTratamiento | TratamientoController / TratamientoService |
| PortalMiHistorial / ControlAccesoPaciente | PortalPacienteController / AccesoPacienteService |
| FormularioConsentimiento / ControlConsentimiento | ConsentimientoController / ConsentimientoService |

Servicios transversales: `AutorizacionService`, `AuditoriaService`.

## Módulos (M-xx)
M-01 Seguridad y accesos (RF-10,11,12) · M-02 Gestión de pacientes (RF-01,02,03) · M-03 Historia clínica (RF-04..09) · M-04 Portal del paciente (RF-14..17) · M-05 Auditoría (RF-13) · M-06 Interoperabilidad (RF-18, HL7 CDA R2 aislado) · M-07 Reportes (RF-19) · M-08 Administración SaaS (RF-20).

## Actores (A-xx)
A-01 Paciente o representante legal · A-02 Médico · A-03 Enfermería · A-04 Admisión y archivo · A-05 Apoyo al diagnóstico · A-06 Administrador del hospital · A-07 Administrador de plataforma SaaS · A-08 Sistema de interoperabilidad externo (secundario).

## Pantallas (P-xx)
P-01 Inicio de sesión (CU-01) · P-02 Buscar paciente (CU-04) · P-03 Registrar atención (CU-05, CU-08) · P-04 Ver historial (CU-06) · P-05 Portal: mi historial (CU-10) · P-06 Consentimiento y privacidad (CU-11).

## Decisiones de diseño (DD-xx)
DD-01 capas · DD-02 una BD con filtro por establecimiento · DD-03 AutorizacionService central · DD-04 AuditoriaService transversal · DD-05 atención cerrada inmutable, correcciones por enmienda · DD-06 interoperabilidad aislada en M-06 · DD-07 JPA · DD-08 web adaptable.

## Diagramas
En `docs/diagramas/` (PlantUML y PNG): arquitectura, clases de diseño, secuencias de CU-03/05/06/08/10/11, componentes y navegabilidad.
