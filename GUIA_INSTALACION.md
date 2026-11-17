# Guía de instalación: HCE-Demo (Windows)
Prototipo académico con **datos ficticios**. No necesita instalar Java, ni SQL Server, ni internet.

## 1. Abrir la demo
1. Copia `HCE-Demo.zip` a la PC. **Antes de descomprimir**: clic derecho en el zip > **Propiedades** > marca **Desbloquear** > Aceptar. (Evita las advertencias de Windows.)
2. Clic derecho > **Extraer todo**. Elige una carpeta normal, por ejemplo `C:\HCE-Demo` o el Escritorio. **No** uses `Archivos de programa`.
3. Entra a la carpeta `HCE-Demo` y haz **doble clic en `HCE-Demo.exe`**.
4. Se abre una ventana negra (es el servidor: **no la cierres mientras uses la demo**) y, a los pocos segundos, el navegador en `http://localhost:8090`.

## 2. Usuarios
La demo **no trae cuentas de demostración**: arranca con un único administrador de plataforma (`admin` / `Temporal2026`) y el resto se crea desde las pantallas (hospital, personal, pacientes). Al primer ingreso pide cambiar la clave.

Para ver el sistema ya poblado hay una copia con datos de ejemplo, **`data_ensayo`** (junto a `HCE-Demo`):
1. Cierra `HCE-Demo` y borra la carpeta `HCE-Demo\data` (si existe).
2. Copia `data_ensayo` dentro de `HCE-Demo` y renómbrala a `data`.
3. Abre `HCE-Demo.exe`. Usuarios de esa copia:

| Usuario | Rol | Clave |
|---|---|---|
| `admin` | Administrador de la plataforma | `Plataforma2026` |
| `admin.trujillo` | Administrador del hospital | `Hospital2026` |
| `carlos.rios` | Admisión | `Admision2026` |
| `ana.flores` | Enfermería | `Enfermeria2026` |
| `jorge.paz` | Médico | `Medico2026` |
| `elena.campos` | Apoyo al diagnóstico | `Apoyo2026` |
| `73456729` | Paciente (portal) | `Paciente2026` |

El recorrido completo para mostrar la demo está en el `README.md` ("Cómo probar el recorrido").

## 3. Cerrar
Cierra la ventana negra (o pulsa `Ctrl + C` en ella). Los datos quedan guardados en la carpeta **`data`**, junto a `HCE-Demo.exe`. Para volver a empezar de cero, cierra el programa y borra la carpeta `data`: al abrir de nuevo se recrea solo con el administrador inicial. Para volver al estado de ejemplo, restaura `data_ensayo` como en el punto 2.

## 4. Si algo falla
- **Windows muestra "Windows protegió su PC" (SmartScreen):** clic en **Más información** > **Ejecutar de todas formas**.
- **El antivirus bloquea o borra el programa:** agrega la carpeta `HCE-Demo` a las exclusiones o usa otra copia (pendrive de respaldo).
- **El navegador no se abre solo:** mira la ventana negra; dice la dirección ("HCE-Demo lista en http://localhost:XXXX"). Escríbela en el navegador.
- **El puerto 8090 está ocupado** (otro programa lo usa): no hace falta hacer nada. La demo prueba el siguiente puerto libre (8091, 8092…) y abre el navegador en el correcto. La dirección exacta se ve en la ventana negra.
- **La ventana negra se cierra sola al abrir:** abre una consola (`cmd`) en la carpeta `HCE-Demo` y ejecuta `HCE-Demo.exe` para leer el mensaje de error. Revisa que la carpeta no esté en un lugar de solo lectura (por ejemplo dentro del zip sin extraer).
- **"La cuenta está bloqueada":** se equivocó 5 veces seguidas con ese usuario. Espera 15 minutos o entra con otro usuario. Si es urgente: cierra el programa y restaura `data_ensayo`.
- **Dos copias a la vez** usan la misma carpeta `data` si están en el mismo lugar: abre solo una.

## 5. Cómo comprobar que no usa el Java de la PC (para el ensayo)
Abre `cmd`, ejecuta `java -version`: si dice que no existe, y aun así `HCE-Demo.exe` arranca, está usando su propio Java (carpeta `runtime`, Java 25).
