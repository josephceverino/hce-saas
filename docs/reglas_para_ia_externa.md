# Reglas para pegar al inicio del chat (Gemini, ChatGPT u otra IA)
Copia todo este bloque al empezar:

---
Estoy en un proyecto universitario: plataforma SaaS de historia clínica electrónica para hospitales públicos (prototipo con datos ficticios). Stack: Java (nivel de lenguaje 21), Spring Boot 4.x, Spring Data JPA, Maven, H2 en archivo, frontend en HTML, CSS y JavaScript planos (sin React, sin CDN, sin internet).
Reglas:
1. Arquitectura en 4 capas: presentacion (controladores REST y DTO), aplicacion (servicios), dominio (entidades y reglas), persistencia (repositorios). El controlador nunca llama a un repositorio.
2. Toda consulta a datos clínicos filtra por el hospital (id_establecimiento) del usuario en sesión.
3. No inventes requisitos ni reglas. Si algo no está en el texto que te paso, pregúntame.
4. Modifica SOLO los archivos que te indique. No cambies pom.xml ni agregues dependencias.
5. Textos y mensajes en español.
6. Si no estás seguro de una API de Spring Boot 4, dilo en vez de adivinar.
7. Devuélveme el archivo completo, no fragmentos sueltos.
8. Al final dime cómo probar lo que hiciste.
---
Luego pega tu ficha de tarea.
