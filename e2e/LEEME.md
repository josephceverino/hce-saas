# Recorridos de interfaz (Playwright, solo desarrollo)

Recorren `docs/recorrido.md` en un navegador real y guardan capturas en `docs/capturas/`. No forman parte del paquete `HCE-Demo`.
Usan el Chrome instalado en la PC (`playwright-core`, sin descargar navegadores).

## Cómo correrlos
1. Una sola vez: `cd e2e` y `npm install`.
2. Arranca la aplicación **con una base nueva** (solo existe `admin` / `Temporal2026`). Por ejemplo, en el puerto 8191 y en memoria:
   `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8191 --spring.datasource.url=jdbc:h2:mem:recorrido"`
3. En otra terminal: `cd e2e` y `npm run recorrido -- 8` (el número es el último paso a recorrer; sin número recorre todos los que existan).
   Otra URL: `set HCE_URL=http://localhost:8090` antes del comando.

## Qué verifica
Cada paso imprime `ok:` por cada comprobación. Si una falla, se detiene con el motivo y termina con código de error.
- Paso 1: el administrador de plataforma cambia su clave, da de alta un hospital y recibe la constancia.
- Paso 2: el administrador del hospital cambia su clave y crea a admisión, enfermería, médico y apoyo al diagnóstico.
- Paso 3: admisión registra a una paciente con filiación completa; se abre su historia y su cuenta del portal.
- Paso 4: admisión registra a un segundo paciente y admite a los dos (consulta externa y emergencia).
- Paso 5: enfermería ve la cola con la emergencia primero, toma al paciente, registra signos vitales (un valor imposible se rechaza), antecedentes y una alergia, y lo deja listo; el médico lo ve en su cola y no ve a quien aún no pasó por enfermería.
- Paso 6: el médico inicia la atención desde la cola (campos ampliados), agrega diagnóstico y tratamiento y pide un examen; la admisión queda "en atención".
- Paso 7: apoyo al diagnóstico ve el examen pendiente de su hospital y registra el resultado.
- Paso 8: el médico ve el resultado, cierra la atención (sin formularios de edición, solo enmienda) y el paciente sale de su cola.
- Paso 9: admisión restablece la clave del paciente; el paciente entra, ve su historial cerrado y su resumen y otorga y revoca un consentimiento; el administrador del hospital ve la auditoría. Corre solo con `node recorrido.mjs 9 9` (usa las claves fijas de los pasos 1 a 8).
- Paso 10 (controles): un segundo hospital no ve pacientes, historias, atenciones ni auditoría del primero (RN-06); enfermería y médico reciben 403 en lo que no les toca (RN-02); 5 claves malas bloquean la cuenta y el administrador la restablece (RN-12). Corre solo y se puede repetir: `node recorrido.mjs 10 10`.

Recorrido completo actual: `npm run recorrido -- 10` (unos 2 minutos). Cada corrida necesita una base nueva.
