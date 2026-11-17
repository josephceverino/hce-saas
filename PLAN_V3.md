# Plan v3: construcción por recorrido
Hoy: lunes 16 de noviembre. Congelado de código: **viernes 4 de diciembre**. Demo: **viernes 11 de diciembre**.
Principio: no se programa un caso de uso suelto; se completa un **paso del recorrido** (`docs/recorrido.md`) para todos los roles que intervienen, y se da por terminado cuando ese paso se puede mostrar en la demo, con captura.

## Punto de partida (importante)
Claude Code ya subió 7 ramas encadenadas (v5, M-2, pantallas 1 a 4 y consentimiento). **No se desecha**: reglas de negocio, autenticación, auditoría y consentimiento se reutilizan. Lo que cambia es el flujo entre actores, el contenido de la historia y la interfaz.

## Incrementos
| Incremento | Fechas | Contenido (pasos) | Terminado cuando… |
|---|---|---|---|
| **R0 Base** | 16 a 18 nov | Aprobar docs de este plan. Pablo revisa y fusiona las 7 ramas en orden. Quitar cuentas demo y la lista numerada del login. Administrador de plataforma inicial. Cambio de clave en primer ingreso (CU-18). Menú e inicio por rol. | Se entra como administrador de plataforma, se cambia la clave y cada rol (aunque solo exista el admin) muestra su inicio propio |
| **R1 Cuentas** | 19 a 22 nov | Paso 1 y 2 (CU-16, CU-17); paso 3 con filiación completa y cuenta del paciente (CU-03 ampliado, CU-19) | Se crea un hospital, su personal y un paciente con constancia de acceso, todo desde pantallas |
| **R2 Recepción** | 23 a 26 nov | Pasos 4 y 5: admitir (CU-20), cola (RF-22), signos vitales y antecedentes (CU-21) | Admisión admite, enfermería ve la cola y registra signos vitales; el médico ve al paciente listo |
| **R3 Atención** | 27 nov a 1 dic | Pasos 6, 7 y 8: atención ampliada (CU-05), tratamiento, pedido de exámenes (CU-22), resultados (CU-23), cierre | El recorrido llega hasta cerrar la atención con resultado visible; no se puede editar la atención cerrada |
| **R4 Portal y control** | 1 a 2 dic | Paso 9 y transversal: portal con la historia nueva, consentimiento, auditoría (CU-25). Reportes y tramo B solo si sobra | El paciente entra y ve su historia completa; el administrador ve la auditoría |
| **R5 Cierre** | 3 y 4 dic | Paquete final `HCE-Demo.exe`, guía, **congelado el viernes 4** | Paquete probado en otra PC con Windows |
Semana de pruebas: 5 a 10 dic (casos de prueba con evidencia, ensayo del guion, video de respaldo, copia de `data/` poblada).

## Paquete de prueba semanal
Pablo genera un paquete `jpackage` **cada viernes** (20 y 27 de noviembre) y lo prueba en otra PC con Windows. Un problema de empaquetado el 3 de diciembre no tiene arreglo; el 20 sí.

## Honestidad sobre el tiempo
Es **viable pero sin holgura**. Dos semanas y media para ocho incrementos con tres personas en horarios distintos. Si un incremento se atrasa más de 1 día, se recorta; no se corre.
Orden de recorte: 1) reportes, 2) tramo B completo (evolución, consentimiento informado, procedimientos), 3) pedido de exámenes simplificado (un campo de texto), 4) pantalla de auditoría reducida a una lista. **No se recorta:** el recorrido de los pasos 1 a 8, el portal del paciente, RN-01, RN-03 y RN-06, ni el paquete portable.

## Decisiones del 17 de noviembre
- **Tramo B recortado desde ya** (CU-24 evolución, RF-29 consentimiento informado, procedimientos): no se implementa.
- **Se agrega** "restablecer clave" (CU-26, RF-30, D-39) a R3.
- Se actualiza `docs/` con cada incremento, incluido el script de SQL Server (regenerar al final de R3).

## Copia de seguridad de la demo
Tras el ensayo final, copiar la carpeta `data/` a `data_ensayo/`. Si falla algo en vivo, se cierra el programa, se restaura esa copia y se reabre: el sistema aparece ya poblado.
