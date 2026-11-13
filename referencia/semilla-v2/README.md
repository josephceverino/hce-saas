# Avance del software v2: inicio de sesión + registrar paciente y abrir historia clínica (CU-03)

Pequeño avance de la plataforma SaaS de historia clínica electrónica. Cubre:

- **CU-01 / RF-10** Inicio de sesión (versión de demostración con dos hospitales).
- **RN-06 / RNF-06** Cada hospital ve solo las historias que abrió (base del SaaS multi-hospital).

- **RF-01** Registrar paciente con sus datos de identificación y contacto.
- **RF-04** Abrir la historia clínica del paciente (una por paciente).
- **RN-01** Un paciente no puede tener dos historias clínicas (el documento no se repite).

Sigue la arquitectura por capas del informe (Figura 14):

| Capa | Paquete | Qué contiene |
|---|---|---|
| Presentación | `presentacion` | `PacienteController`, formulario web, manejo de errores |
| Aplicación | `aplicacion` | `RegistroPacienteService` (caso de uso CU-03) |
| Dominio | `dominio` | `Paciente`, `HistoriaClinica`, `Establecimiento` |
| Persistencia | `persistencia` | Repositorios JPA |

Las tablas coinciden con el modelo lógico (Figura 13): `establecimiento`, `paciente` y `historia_clinica`.

## Cómo ejecutarlo

Necesitas **Java 17 o superior** y **Maven**.

```
cd hce-avance
mvn spring-boot:run
```

Abre http://localhost:8080 en el navegador.

Por defecto usa una base de datos **H2 en memoria**, así que no instalas nada más. Los datos se borran al cerrar.
Consola de la base: http://localhost:8080/h2-console (URL JDBC: `jdbc:h2:mem:hce`, usuario `sa`, sin clave).

### Con SQL Server

1. Crea una base vacía llamada `hce`.
2. Edita usuario y clave en `src/main/resources/application-sqlserver.properties`.
3. Ejecuta: `mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver`

## Qué probar en la demo

1. Registra un paciente nuevo: aparece "Historia clínica HC-000001 abierta".
2. Vuelve a registrar el mismo documento: el sistema responde que ya existe (RN-01).
3. Prueba un DNI con menos de 8 dígitos o un correo mal escrito: muestra el error.
4. Mira la tabla de historias abiertas.

## Qué falta (siguiente avance)

Roles y permisos (RF-11, RF-12), búsqueda de pacientes (RF-02), auditoría de accesos (RF-13), registrar atenciones (RF-05) y el portal del paciente (RF-14, RF-15).

## Cuentas de demostración (clave: `demo123`)

| Usuario | Hospital | Rol |
|---|---|---|
| `admision.a` | Hospital Demo A | Personal administrativo |
| `admision.b` | Hospital Demo B | Personal administrativo |

**Para demostrar el SaaS:** entra con `admision.a`, registra un paciente, cierra sesión y entra con `admision.b`. La tabla del Hospital B estará vacía: cada hospital solo ve sus datos.

**Aclaración:** el inicio de sesión es una versión de demostración. La clave se guarda con SHA-256 y no hay bloqueo por intentos fallidos. La versión real usará BCrypt y la política de claves de RN-12. No se implementó Spring Security.

Los hospitales, el rol y los usuarios se crean automáticamente al arrancar.
