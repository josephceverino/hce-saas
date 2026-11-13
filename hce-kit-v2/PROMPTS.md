# Prompts para Claude Code (en orden)

Regla de oro: **un prompt = un incremento**. Antes de cada uno, abre Claude Code en la carpeta del repo y activa el **modo plan**. Aprueba el plan, recién entonces deja que escriba código.

---
## 0. Preparación (una sola vez)
```
Lee CLAUDE.md y todo docs/. Confirma qué entendiste en 10 líneas (arquitectura, reglas clave, estado actual) y dime qué dudas tienes. No escribas código todavía.
```

## 1. Arrancar el proyecto desde la semilla (Fase 0)
```
El código funcional está en semilla-v2/ (hoy en Spring Boot 3.3.5). Crea en la raíz un proyecto Maven con wrapper usando ese código como base. Decisiones cerradas (ver CLAUDE.md): Spring Boot 4.x (última estable; si la migración desde 3.3.5 da problemas serios, dime y usamos 3.5.x), maven.compiler.release=21, puerto 8090 en application.properties, H2 en archivo ./data/hce con ddl-auto=update, perfil sqlserver conservado.
Pasos: 1) propón el plan y la lista de cambios que implica pasar a Spring Boot 4 (paquetes, anotaciones, tests), 2) ejecútalo, 3) ./mvnw test, 4) arranca la app y dime la URL, 5) crea .gitignore (target/, data/, .idea/, *.log, *.mv.db, *.trace.db) y sugiere el primer commit.
Además divide el frontend: static/css/estilo.css (variables de docs/diseno_ui.md), static/js/ común, y una carpeta por pantalla (login, pacientes, historia) en lugar de un solo index.html. Sin CDN. No cambies el comportamiento actual.
```
Verificación tuya: entra con `admision.a / demo123` en `http://localhost:8090`, registra un paciente, repite el DNI (debe rechazar, RN-01). Si funciona: commit y `git tag v2`.

## 2. v3: CU-06, CU-05, CU-08
```
Implementa el incremento v3 según docs/casos_de_uso.md: CU-06 (consultar historia clínica), CU-05 (registrar atención y cerrarla) y CU-08 (tratamiento e indicaciones). Necesito además un usuario médico demo por hospital (medico.a y medico.b).
Reglas: RN-02, RN-03 (atención cerrada inmutable; corrección por enmienda), RN-06 (otro hospital = denegado), RN-10 (diagnóstico con código CIE-10 de una lista corta de prueba), RN-11, RN-07 (auditoría básica).
Primero dame el plan con: entidades JPA nuevas, servicios, endpoints, pantallas y tests. Espera mi aprobación.
Tests mínimos: cerrar atención y fallar al editarla; registrar tratamiento en atención cerrada falla; médico del hospital A no ve la historia del hospital B; diagnóstico sin CIE-10 se rechaza.
```
Verificación: lo anterior, a mano en el navegador. `git tag v3`.

## 3. v4: CU-10 y CU-11
```
Implementa v4: portal del paciente (CU-10) y gestión de consentimiento (CU-11) según docs/casos_de_uso.md.
Reglas: RN-04 (el paciente solo ve su historia y solo lectura), RN-05 (representante legal, versión simple), RN-06 y RN-09 (consentimiento con fecha y alcance), RF-16 (resumen descargable, puede ser HTML imprimible).
Crea usuarios paciente demo. Revocar un consentimiento corta el acceso del tercero de inmediato.
Plan primero. Tests: paciente A no puede pedir la historia de paciente B (403); revocar consentimiento bloquea al tercero; el paciente no puede modificar nada.
```
`git tag v4`.

## 4. v5: auditoría, roles y seguridad
```
Implementa v5: registro_auditoria real (RN-07, RF-13) con quién, cuándo, qué y sobre qué entidad; roles y permisos por rol (RF-11, RF-12) aplicados en AutorizacionService; contraseñas con BCrypt (RN-12) y bloqueo tras 5 intentos fallidos. Migra los usuarios demo al nuevo hash.
Plan primero. Tests: cada acceso a una historia genera un registro; usuario bloqueado no entra; rol sin permiso recibe 403.
Avísame si introducir Spring Security obliga a cambiar el login de sesión actual y propón la forma menos invasiva.
```
`git tag v5`.

## 5. Empaquetado portable (Windows)
Requisito: la PC del aula usa Windows. Se construye en tu PC con JDK 25.
```
Prepara la entrega portable para Windows, sin que la PC destino necesite Java, SQL Server ni nada instalado:
1) Al arrancar, la app abre el navegador en su URL (http://localhost:8090). Si el puerto está ocupado, prueba el siguiente libre y abre el correcto.
2) La base H2 y sus datos demo viven en una carpeta ./data junto al programa (no en Archivos de programa). Si no existe, se crea y se carga la semilla.
3) Un script empaquetar.bat que: ejecuta ./mvnw clean package, y luego jpackage --type app-image con runtime incluido (--name HCE-Demo, --win-console para ver el log y poder cerrar el servidor, ícono si hay), generando la carpeta HCE-Demo/ con HCE-Demo.exe.
4) GUIA_INSTALACION.md de una página: descomprimir, doble clic en HCE-Demo.exe, usuarios demo, qué hacer si Windows advierte (SmartScreen: Más información > Ejecutar de todas formas; o clic derecho al zip > Propiedades > Desbloquear antes de descomprimir), qué hacer si el puerto está ocupado.
5) Verifica con un paquete recién construido en una carpeta limpia y dime cómo comprobar que no usa el Java del sistema.
Primero propón el plan.
```
Prueba obligatoria antes del 5 de diciembre: copiar el zip a **otra PC Windows** (idealmente sin Java instalado), descomprimir y abrir con doble clic. Si no hay una, usar Windows Sandbox (solo Windows Pro/Enterprise) o quitar Java del PATH para la prueba.

## 6. Casos de prueba (Sesión 11)
```
Lee docs/pruebas/plantilla_casos_prueba.md. Para cada caso, indica si ya está cubierto por un test automático (nombre del test) o hay que probarlo a mano. Actualiza la columna "Cobertura" sin tocar el resto ni marcar casos como ejecutados.
```
Los resultados reales (Aprobado/Fallido) los llenas **tú** después de ejecutar.

---
## Cuando algo falla
```
Esto falló: [pega el error completo]. No cambies nada aún: dime la causa más probable y cómo la confirmaste, y propón el arreglo mínimo.
```
## Antes de cada commit
```
Muéstrame el resumen de cambios (archivos y por qué), confirma que ./mvnw test está en verde y sugiere el mensaje de commit con los IDs (CU/RN/RF) implementados.
```
