# Guía de instalación: HCE-Demo (Windows)
Prototipo académico con **datos ficticios**. No necesita instalar Java, ni SQL Server, ni internet.

## 1. Abrir la demo
1. Copia `HCE-Demo.zip` a la PC. **Antes de descomprimir**: clic derecho en el zip > **Propiedades** > marca **Desbloquear** > Aceptar. (Evita las advertencias de Windows.)
2. Clic derecho > **Extraer todo**. Elige una carpeta normal, por ejemplo `C:\HCE-Demo` o el Escritorio. **No** uses `Archivos de programa`.
3. Entra a la carpeta `HCE-Demo` y haz **doble clic en `HCE-Demo.exe`**.
4. Se abre una ventana negra (es el servidor: **no la cierres mientras uses la demo**) y, a los pocos segundos, el navegador en `http://localhost:8090`.

## 2. Usuarios de demostración (clave `demo123` para todos)
| Usuario | Rol | Hospital |
|---|---|---|
| `admision.a` / `admision.b` | Admisión y archivo | A / B |
| `medico.a` / `medico.b` | Médico | A / B |
| `enfermera.a` / `enfermera.b` | Enfermería | A / B |
| `admin.a` | Administrador del hospital | A |
| `plataforma` | Administrador de la plataforma | (ninguno) |
| `paciente.a` / `paciente.b` | Paciente (portal) | — |

## 3. Cerrar
Cierra la ventana negra (o pulsa `Ctrl + C` en ella). Los datos quedan guardados en la carpeta **`data`**, junto a `HCE-Demo.exe`. Para volver a empezar de cero, cierra el programa y borra la carpeta `data`: al abrir de nuevo se recrea con los datos de prueba.

## 4. Si algo falla
- **Windows muestra "Windows protegió su PC" (SmartScreen):** clic en **Más información** > **Ejecutar de todas formas**.
- **El antivirus bloquea o borra el programa:** agrega la carpeta `HCE-Demo` a las exclusiones o usa otra copia (pendrive de respaldo).
- **El navegador no se abre solo:** mira la ventana negra; dice la dirección ("HCE-Demo lista en http://localhost:XXXX"). Escríbela en el navegador.
- **El puerto 8090 está ocupado** (otro programa lo usa): no hace falta hacer nada. La demo prueba el siguiente puerto libre (8091, 8092…) y abre el navegador en el correcto. La dirección exacta se ve en la ventana negra.
- **La ventana negra se cierra sola al abrir:** abre una consola (`cmd`) en la carpeta `HCE-Demo` y ejecuta `HCE-Demo.exe` para leer el mensaje de error. Revisa que la carpeta no esté en un lugar de solo lectura (por ejemplo dentro del zip sin extraer).
- **Dos copias a la vez** usan la misma carpeta `data` si están en el mismo lugar: abre solo una.

## 5. Cómo comprobar que no usa el Java de la PC (para el ensayo)
Abre `cmd`, ejecuta `java -version`: si dice que no existe, y aun así `HCE-Demo.exe` arranca, está usando su propio Java (carpeta `runtime`, Java 25).
