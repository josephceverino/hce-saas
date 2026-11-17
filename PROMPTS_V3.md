# Prompts para Claude Code (v3)
Todos con **modo plan**: pide el plan, lo apruebas, y recién entonces escribe código. Antes del primer prompt, copia `plan-v3/docs/*` a `docs/` del repo y las capturas a `docs/referencias_ui/`.

## P-0 · Integrar el nuevo alcance (antes de programar)
```
Lee docs/recorrido.md, docs/requisitos_nuevos.md, docs/casos_de_uso_nuevos.md, docs/historia_clinica_contenido.md, docs/modelo_datos_cambios.md y docs/guion_demo.md. Son PROPUESTAS mías.
1) Compáralos con el código y los docs actuales y dime qué choca o ya existe (p. ej. CU o RN con el mismo ID, pantallas ya hechas, tablas ya creadas). 2) No inventes campos ni reglas médicas: lo marcado "propuesta" lo dejo yo. 3) Propón los cambios a docs/requisitos.md, casos_de_uso.md y modelo_datos.md como un solo diff para que lo apruebe. 4) No programes todavía.
```

## R0 · Base
```
Incremento R0 según PLAN_V3.md: 1) revisa las 7 ramas encadenadas y dime en qué orden fusionarlas y qué riesgos hay; 2) quita las cuentas de demostración y el recuadro del login y la lista numerada del panel izquierdo; 3) el sistema arranca con un único ADMIN_PLATAFORMA con clave temporal que debe cambiar en el primer ingreso (CU-18, RN-14); 4) cada rol tiene su pantalla de inicio propia y su menú (docs/recorrido.md). Abre cada pantalla con Playwright, toma captura y compárala con docs/referencias_ui/ antes de continuar. Plan primero.
```

## R1 · Cuentas
```
Incremento R1: CU-16 (hospitales), CU-17 (usuarios del hospital), CU-19 (cuenta del paciente), y filiación completa en CU-03 (docs/historia_clinica_contenido.md, sección 1). Contrato primero: crea los endpoints, DTO y tests que fallan para CU-17 y para la parte de signos vitales del siguiente incremento, y dime en qué archivos puede trabajar cada persona (Alexander pantallas, Mattias servicios). Una pantalla por vez con captura contra la referencia. Plan primero.
```

## R2 · Recepción
```
Incremento R2: admisión y estados (RN-13), cola por rol (RF-22), signos vitales (CU-21) y antecedentes por tipo. Test por cada transición de estado permitida y prohibida. La cola de emergencia va primero. Referencia visual: 05_directorio_para_lista_y_cola. Plan primero.
```

## R3 · Atención
```
Incremento R3: CU-05 ampliado (anamnesis estructurada, examen físico por sistemas, diagnóstico CIE-10 con descripción completa, sin siglas en la interfaz), CU-08, CU-22 y CU-23. CIE-10 con CSV de demostración solo con códigos que conozcas con certeza, marcado como subconjunto no oficial. La historia usa la referencia 02_perfil_para_historia_clinica. Atención cerrada inmutable con test. Plan primero.
```

## R4 · Portal y control
```
Incremento R4: el portal del paciente muestra la historia ampliada en solo lectura (RN-04), consentimiento (CU-11), auditoría completa del administrador (CU-25) con filtros por usuario, fecha y paciente. Reportes solo si Pablo lo pide. Plan primero.
```

## R5 · Paquete portable
```
Usa el prompt 5 de PROMPTS.md (jpackage app-image con runtime incluido, apertura automática del navegador, puerto 8090 o el siguiente libre, datos en ./data junto al programa). Además: el programa arranca vacío (solo el administrador de plataforma) y existe un script para restaurar data_ensayo/. Plan primero.
```

## Cuando una pantalla no se ve bien
```
Esta pantalla no cumple: [qué no te gusta]. Compárala con docs/referencias_ui/[archivo]: jerarquía, espacios, tarjetas, tablas con etiquetas de estado. Dime las diferencias concretas antes de cambiar nada y luego corrige y vuelve a tomar la captura.
```
