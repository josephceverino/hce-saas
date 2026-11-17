# Recorridos de interfaz (Playwright, solo desarrollo)

Recorren `docs/recorrido.md` en un navegador real y guardan capturas en `docs/capturas/`. No forman parte del paquete `HCE-Demo`.
Usan el Chrome instalado en la PC (`playwright-core`, sin descargar navegadores).

## Cómo correrlos
1. Una sola vez: `cd e2e` y `npm install`.
2. Arranca la aplicación **con una base nueva** (solo existe `admin` / `Temporal2026`). Por ejemplo, en el puerto 8191 y en memoria:
   `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8191 --spring.datasource.url=jdbc:h2:mem:recorrido"`
3. En otra terminal: `cd e2e` y `npm run recorrido -- 3` (el número es el último paso a recorrer; sin número recorre todos los que existan).
   Otra URL: `set HCE_URL=http://localhost:8090` antes del comando.

## Qué verifica
Cada paso imprime `ok:` por cada comprobación. Si una falla, se detiene con el motivo y termina con código de error.
- Paso 1: el administrador de plataforma cambia su clave, da de alta un hospital y recibe la constancia.
- Paso 2: el administrador del hospital cambia su clave y crea a admisión, enfermería, médico y apoyo al diagnóstico.
- Paso 3: admisión registra a una paciente con filiación completa; se abre su historia y su cuenta del portal.
