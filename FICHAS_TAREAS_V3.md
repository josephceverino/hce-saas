# Fichas de tareas v3 (por incremento y por persona)
Reglas de todas las fichas:
- **Contrato primero:** Pablo (con Claude Code) deja creados el endpoint, los DTO, las firmas y los **tests que fallan** antes de entregar la ficha. Quien recibe la ficha implementa hasta que pasen.
- **Plan B:** Pablo mantiene un mínimo funcional de cada tramo para que la demo no dependa de una sola persona.
- **Punto de control a las 48 horas:** rama con al menos un commit y una captura. Si no hay nada, Pablo reasume el tramo.
- **Revisión:** en cada pull request Pablo hace 2 o 3 preguntas sobre el código. Quien no sepa explicarlo lo rehace.
- **Quienes usan ChatGPT o Gemini** pegan `docs/reglas_para_ia_externa.md` y la ficha antes de empezar, y nunca modifican archivos que no estén en su ficha.
- **Cada pantalla** se compara con su referencia de `docs/referencias_ui/` y se adjunta captura.

## Roles
| Persona | Rol | Responsable de |
|---|---|---|
| Pablo | Líder técnico e integrador | Seguridad, multi-tenant, estados de admisión, atención (CU-05), auditoría, revisión, paquete portable |
| Alexander | Frontend y experiencia | Todas las pantallas P-xx: estructura, estilo, estados vacíos y de error |
| Mattias | Backend de apoyo y calidad | Servicios y endpoints acotados (signos vitales, exámenes y resultados, usuarios), CIE-10, tests de cada incremento, casos de prueba |

## R0 Base (16 a 18 nov)
| Quién | Tarea | Archivos | Terminado cuando |
|---|---|---|---|
| Pablo | Revisar y fusionar las 7 ramas; admin inicial; cambio de clave | `aplicacion/`, `dominio/` | `main` compila y pasa los tests |
| Alexander | Login nuevo (sin lista numerada ni recuadro de cuentas) y P-13 cambio de clave | `static/login/`, `static/cambio-clave/` | Capturas comparadas con la referencia 04 |
| Mattias | Tests de autenticación y de primer ingreso (RN-14) | `src/test/...` | `mvnw test` en verde |

## R1 Cuentas (19 a 22 nov)
| Quién | Tarea | Terminado cuando |
|---|---|---|
| Pablo | CU-16 hospitales (backend) y creación de cuenta del paciente (CU-19) | Endpoints y tests listos |
| Mattias | CU-17 usuarios del hospital: servicio, endpoint y tests (RN-16 es el test principal: un administrador no crea usuarios de otro hospital) | Tests de RN-16 y RN-14 pasan |
| Alexander | P-07 hospitales, P-08 usuarios, P-16 registro de paciente con filiación completa | Se puede hacer el paso 1 al 3 desde pantallas |

## R2 Recepción (23 a 26 nov)
| Quién | Tarea | Terminado cuando |
|---|---|---|
| Pablo | Admisión y estados (RN-13), cola (RF-22) | Pasa un test por cada transición permitida y prohibida |
| Mattias | Signos vitales (CU-21): entidad, servicio, endpoint y validaciones; antecedentes por tipo | Valores fuera de rango se rechazan |
| Alexander | P-09 cola y botón "Admitir"; sección de signos vitales en P-04 | Pasos 4 y 5 completos con captura |

## R3 Atención (27 nov a 1 dic)
| Quién | Tarea | Terminado cuando |
|---|---|---|
| Pablo | CU-05 ampliado y cierre; filtro por hospital en todo | Atención cerrada inmutable con test |
| Mattias | CU-22 pedido de exámenes y CU-23 resultados; módulo CIE-10 (CSV de demostración y validación) | RN-18 y RN-10 con tests |
| Alexander | P-03 registro de atención por secciones, P-12 exámenes y resultados | Pasos 6 a 8 completos con captura |

## R4 Portal y control (1 y 2 dic)
| Quién | Tarea |
|---|---|
| Pablo | Auditoría completa (CU-25), conexión del portal con la historia ampliada |
| Alexander | P-05 con la historia completa, P-14 auditoría; P-15 reportes solo si sobra |
| Mattias | Pruebas de acceso cruzado entre hospitales (RNF-06) y de consentimiento |

## R5 Cierre y pruebas (3 a 10 dic)
| Quién | Tarea |
|---|---|
| Pablo | Paquete portable y guía; congelado |
| Mattias | Ejecutar casos de prueba con evidencia (`docs/pruebas/`) |
| Alexander | UML de implementación (Sesión 12) y capturas para el informe |
| Todos | Ensayo del guion de `docs/guion_demo.md`, mínimo dos veces |

## Sesiones del curso
Sesión 11 (hasta el 15 de nov): casos de prueba. Sesión 12 (16 a 22 de nov): UML de implementación. Si la profesora pide entregables de estas sesiones, se hacen sobre el código real de R0 y R1.
