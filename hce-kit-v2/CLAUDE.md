# CLAUDE.md: Plataforma SaaS de Historia Clínica Electrónica (HCE)

## Qué es
Proyecto académico (Ingeniería de Software, UCV). Plataforma SaaS de HCE para **hospitales públicos** con portal del paciente. Es un **prototipo con datos ficticios**: nunca datos reales de pacientes.
Se **demuestra en clase (11 de diciembre de 2026) en una PC con Windows**: debe ser portable, funcionar sin internet y abrirse con doble clic.
Metodología: RUP (estamos en Construcción). Se entrega con trazabilidad por IDs (RF, RNF, RN, CU, M, A).

## Fuente de verdad (leer antes de codificar)
- `docs/requisitos.md`: RF, RNF, RN.
- `docs/casos_de_uso.md`: flujos de CU-03, 05, 06, 08, 10, 11.
- `docs/arquitectura.md`: capas, multi-tenant, nombres de clases.
- `docs/modelo_datos.md` y `docs/script_sqlserver.sql`: tablas.
**Si algo no está en estos documentos, no lo inventes: pregúntame.**

## Stack (decisiones cerradas)
- **Java:** código compilado a nivel de lenguaje **21** (`maven.compiler.release=21`) para que compile con cualquier JDK 21 a 25 del equipo. El paquete portable lo arma Pablo con JDK 25.
- **Spring Boot 4.x** (Web, Data JPA, Validation). Plan B si la migración desde 3.3.5 da muchos problemas: Spring Boot 3.5.x. Pablo decide el cambio.
- **Maven con wrapper** (`mvnw.cmd` en Windows).
- **Base de datos: H2 en archivo** (`./data/hce`), sin servidor ni instalación. SQL Server solo como perfil `sqlserver`, opcional y sin probar. Las entidades respetan `docs/modelo_datos.md`.
- **Puerto: 8090** (no 8080, para no chocar con otros proyectos de Spring en la PC). Si está ocupado, la app prueba el siguiente libre y abre el navegador en el correcto.
- **Frontend:** HTML, CSS y JS planos en `src/main/resources/static`, **sin CDN ni internet** (CSS, íconos SVG y fuente incluidos en el proyecto). Sin React ni frameworks.
- **Tests:** JUnit 5 + Spring Boot Test.
- No agregues dependencias sin avisar y justificar.

## Portabilidad (requisito de la demo)
Entregable final: carpeta `HCE-Demo/` con `HCE-Demo.exe` (hecha con `jpackage --type app-image`, runtime de Java incluido). Doble clic abre el servidor y el navegador. Los datos viven junto al programa, nunca en `Archivos de programa`. Detalle en `PROMPTS.md` (prompt 5).

## Arquitectura (obligatoria)
Paquetes: `dominio`, `aplicacion`, `persistencia`, `presentacion`.
- El controlador solo llama a servicios; nunca a repositorios.
- Las reglas RN viven en dominio o servicios, no en controladores ni en el JS.
- **Multi-tenant:** toda consulta a datos clínicos filtra por el `id_establecimiento` del usuario en sesión (RNF-06, RN-06). Un test debe demostrar que un hospital no ve datos de otro.
- Auditoría transversal vía `AuditoriaService` (RN-07).
- Nombres de clases según la tabla de correspondencia de `docs/arquitectura.md`.

## Reglas de trabajo
1. Un incremento a la vez; empieza SIEMPRE proponiendo un plan y esperando mi aprobación.
2. Cada cambio cita los IDs que implementa (ej. "CU-06, RN-06, RN-07") en commits y en comentarios de las clases nuevas.
3. Antes de dar algo por terminado: `./mvnw test` en verde y la app arrancando (`./mvnw spring-boot:run`).
4. Escribe al menos un test por cada regla de negocio que toques (RN-01, 03, 04, 06...).
5. Mensajes de error y textos de UI en español claro.
6. No rompas lo que ya funciona: si un cambio afecta código anterior, dilo antes.
7. Contraseñas: la demo usa SHA-256 (solo demostración). Objetivo v5: BCrypt (RN-12). Nunca contraseñas reales en el repo.
8. Al terminar un incremento: actualiza `README.md` (cómo probarlo) y `docs/pruebas/` si aplica; sugiéreme el mensaje de commit y el tag.

## Interfaz
Seguir `docs/diseno_ui.md` (sistema visual propio, inspirado en paneles de administración modernos, sin copiar código ni imágenes de plantillas de terceros). Las cifras de las tarjetas salen de la base de datos real, nunca inventadas. Solo se muestran opciones del menú que existan en el alcance.

## Equipo y flujo
Ver `docs/equipo_y_flujo.md`. Resumen: rama por tarea, pull request revisado por Pablo, nadie hace push directo a `main`.

## Estado actual
Semilla `semilla-v2/` (código funcional): CU-03 registrar paciente y abrir historia (RN-01), login por sesión (CU-01), dos hospitales demo (admision.a / admision.b, clave demo123).
Pendiente: Fase 0 (base y frontend dividido) · v3 (CU-06, CU-05, CU-08) · v4 (CU-10, CU-11) · v5 (auditoría, roles, BCrypt) · empaquetado portable. Ver `PLAN_CALENDARIO.md`.

## Fuera de alcance (no implementar)
Agenda de citas, facturación, farmacia, telemedicina, IA, app móvil nativa.
