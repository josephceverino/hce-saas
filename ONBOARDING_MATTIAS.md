# Onboarding: lo que necesitas saber en 10 minutos

## Qué es
Una plataforma web de **historia clínica electrónica para hospitales públicos**, con acceso del paciente a su historial. Prototipo con datos ficticios, que se demuestra en clase el **11 de diciembre**.

## El problema que resuelve
Muchas historias clínicas siguen en papel y se pierden o quedan incompletas; un mismo paciente tiene historias separadas en distintos hospitales. Nuestro sistema las centraliza, controla quién ve qué y deja al paciente consultar la suya.

## Cómo está hecho
- Java + Spring Boot, base H2 (un archivo, sin instalar nada), pantallas en HTML, CSS y JS planos.
- **4 capas:** presentación (lo que recibe y responde por internet), aplicación (la lógica de cada caso de uso), dominio (las entidades y las reglas), persistencia (guardar y leer la base).
- **Varios hospitales en un mismo sistema:** cada hospital solo ve lo suyo. Toda consulta filtra por el hospital del usuario.

## Las reglas que más importan (versión corta)
- **RN-01:** cada paciente tiene una sola historia clínica (se identifica por DNI).
- **RN-03:** una atención cerrada no se edita ni se borra; se corrige con una enmienda.
- **RN-04:** el paciente ve solo lo suyo y solo en lectura.
- **RN-06:** un hospital no ve los pacientes de otro (salvo permiso del paciente o emergencia).
- **RN-07:** todo acceso a una historia queda registrado.
Todas están en `docs/requisitos.md`.

## Quién es quién
Admisión (registra pacientes), médico (registra atenciones), enfermería, paciente, administrador. Cada uno ve un menú distinto.

## Qué ya funciona
Iniciar sesión, registrar un paciente con validaciones y rechazo de DNI repetido, dos hospitales de demostración (`admision.a` y `admision.b`, clave `demo123`).

## Qué falta (y dónde entras tú)
Consultar historia, registrar atención y tratamiento (v3), portal del paciente y consentimiento (v4), auditoría y roles (v5). Tú empiezas con **tests** y una pantalla sencilla (ver `FICHAS_TAREAS.md`: F1-3 y F2-3).

## Cómo trabajar
1. Instala Git, JDK y IntelliJ; clona el repo (`GIT_SETUP.md`).
2. Crea una rama por tarea; nunca trabajes en `main`.
3. Si usas Gemini, pega primero `docs/reglas_para_ia_externa.md`.
4. Antes de pedir revisión: `.\mvnw.cmd test` en verde.
5. Si no entiendes algo, pregunta: es mejor que entregar algo que no puedes explicar. En la revisión Pablo te preguntará cómo funciona lo que hiciste.

## Qué NO hacer
No subir `target/`, `data/` ni contraseñas. No inventar requisitos. No tocar `pom.xml`. No agregar funciones fuera del alcance (citas, chat, facturación...).
