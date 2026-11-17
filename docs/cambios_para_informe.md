# Cambios que debe recibir el informe académico
Hacerlos **después** de aprobar el alcance. La profesora ya revisó el informe y pidió cambios menores (aún sin detalle): combinar ambas cosas en una sola pasada.

| Sección del informe | Qué cambia |
|---|---|
| 2.2 / 2.3 | Sin cambios de fondo; confirmar que los 7 roles con pantalla están descritos |
| 2.5 Alcance funcional | Añadir RF-21 a RF-30; aclarar que la admisión del día no es agenda de citas (sigue fuera de alcance) |
| 2.6 y 3.1.1 | 20 → 29 RF (20 Alta y 9 Media); nueva tabla; mencionar la filiación completa |
| 3.1.3 | 12 → 19 reglas de negocio (RN-13 a RN-19) |
| 3.2.1 y 3.2.2 | 15 → 25 casos de uso (CU-16 a CU-26, sin CU-24: el tramo B se recortó el 17 nov); actualizar el diagrama general; especificar los CU nuevos principales |
| 3.3.1 y 3.4 | Nuevas entidades (`admision`, `signos_vitales`, `solicitud_examen`, ; el tramo B quedó fuera, sin `nota_evolucion` ni `consentimiento_informado`) y columnas (`usuario.clave_restablecida_en`) y columnas; 20 → 23 o más tablas |
| 3.5.6 | Tabla de pantallas con P-07 en adelante y estado real |
| 4.1 Tabla 8 | Versiones reales: Java, Spring Boot, H2 (demo), SQL Server (perfil, sin probar), Maven, jpackage |
| Anexo 2 (Gantt) | Reflejar el calendario real |
| Limitaciones (nueva) | Intercambio entre hospitales solo con rechazo (DEC-02); representante legal no implementado (DEC-05); catálogo CIE-10 reducido; epicrisis y formatos por etapa de vida fuera |

Además: decisiones DEC-01 a DEC-07 de `docs/decisiones.md` (las que registró Claude Code).
