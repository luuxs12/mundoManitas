# Manual del desarrollador

**Mundo Manitas — plataforma educativa inclusiva**
Última revisión: octubre de 2026
Equipo: Proyecto Integrador I, Ingeniería de Software

---

## Antes de empezar: qué es este documento

El `README.md` de la raíz es la carta de presentación del proyecto: sirve para que alguien
entienda qué hace la plataforma, con qué está construida y cómo levantarla en cinco minutos.
Este manual es otra cosa. Es el documento de trabajo del equipo: explica cómo está organizado
el código, por qué se tomaron ciertas decisiones, dónde se toca cada cosa y qué errores son
esperables cuando alguien nuevo se sienta a programar.

Si es tu primer día, lee primero el README y vuelve aquí cuando tengas la aplicación corriendo.
Si ya conoces el proyecto y solo necesitas un dato (una ruta, una regla, un comando), ve
directo a la sección correspondiente. El documento está pensado para consultarse, no para
leerse de corrido.

> Nota sobre el nombre: en el repositorio no existe un archivo llamado "manual" además de este.
> El README es la puerta de entrada; este archivo es la referencia técnica.

---

## 1. El sistema en una página

Mundo Manitas nace de un problema concreto: las personas sordas de la comunidad suelen quedar
fuera de las plataformas de aprendizaje de programación porque el contenido depende del audio,
los textos son largos y el ritmo lo impone el calendario. La plataforma invierte esas tres
cosas: el video es el canal principal (con subtítulos y apoyo visual), las lecciones son cortas,
y el avance se mide por lo que la persona completa, no por fechas.

Eso explica varias decisiones que, vistas desde fuera, pueden parecer arbitrarias:

- El progreso se calcula con videos vistos y ejercicios correctos, no con tiempo de conexión.
- El estudiante puede repetir un ejercicio sin perder lo avanzado.
- El certificado solo se emite al llegar al 100 %, y se genera en Word para que pueda imprimirse.
- El registro de docentes no crea cuentas: primero se revisa evidencia profesional.

### Roles

| Rol | Para qué existe | Qué puede hacer |
| --- | --- | --- |
| `ESTUDIANTE` | Persona que aprende | Ver sus cursos y el catálogo, inscribirse, comprar cursos premium, marcar videos, responder ejercicios, descargar certificados |
| `PADRE` | Acompañamiento familiar | Ver su perfil y su panel; no accede al catálogo ni al contenido |
| `DOCENTE` | Autor de contenido | Ver y editar solo los módulos que tiene asignados, revisar el avance de sus alumnos |
| `ADMINISTRADOR` | Gestión académica | Usuarios, contenido, postulaciones, reportes y panel general |
| `SISTEMAS` | Operación técnica | Los mismos accesos administrativos, orientados a soporte |

### Recorridos principales

**Estudiante.** Se registra en `/registro/estudiante`, entra al catálogo, se inscribe en un
módulo, ve los videos, marca cada uno como completado y responde los ejercicios. El porcentaje
sube a medida que completa contenido. Al llegar a 100 % puede descargar el certificado. Si el
módulo es premium, primero lo compra desde el detalle; la compra queda registrada y desbloquea
el contenido al instante.

**Docente.** No se registra solo. Envía una postulación en `/registro/docente` con su nivel de
Lengua de Señas Peruana, certificación, experiencia en TI y portafolio. Un administrador la
revisa en `/admin/postulaciones`. Si la aprueba, el sistema crea la cuenta `DOCENTE` con una
contraseña temporal y le asigna un módulo. Desde ese momento el docente entra a
`/docente/cursos` y solo ve ese módulo (y los que le asignen después).

**Administrador.** Crea cuentas, cambia roles y estados, edita todo el contenido, resuelve
postulaciones y descarga el reporte de progreso en Excel.

---

## 2. Puesta en marcha

### 2.1 Requisitos

| Herramienta | Versión usada por el equipo | Notas |
| --- | --- | --- |
| JDK | 21 (Temurin 21.0.12) | El `pom.xml` fija `java.version=21` |
| Maven | Wrapper incluido (`mvnw` / `mvnw.cmd`) | No hace falta instalar Maven |
| PostgreSQL | 18 en local; 16 en Docker | El SQL es compatible con ambas |
| Docker | Opcional | Solo para la base o para el despliegue completo |
| Navegador | Chrome o Edge recientes | La interfaz no soporta IE |

En Windows conviene usar PowerShell 7 o, si se usa PowerShell 5.1, recordar que no acepta
`&&` como separador de comandos. Usa `;`.

### 2.2 Base de datos

Con PostgreSQL instalado y el servicio corriendo:

```sql
CREATE DATABASE mundo_manitas;
```

No hace falta crear tablas. Flyway aplica `V1` a `V4` en el primer arranque y Hibernate solo
valida el esquema (`ddl-auto=validate`). Si la base ya existe pero está vacía, el mismo arranque
la deja lista.

Alternativa con Docker, levantando solo la base:

```bash
docker compose up -d postgres
```

Eso publica PostgreSQL en `localhost:5432` con la base `mundo_manitas`.

### 2.3 Configuración local

El perfil `dev` lee credenciales de `src/main/resources/application-dev.properties`, que está
en `.gitignore`. La plantilla versionada es `application-dev.example.properties`:

```bash
cp src/main/resources/application-dev.example.properties src/main/resources/application-dev.properties
```

Edita la contraseña de PostgreSQL en el archivo copiado. Si prefieres no crear el archivo,
exporta las variables antes de arrancar:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/mundo_manitas"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "tu_contrasena"
```

Las propiedades importantes del perfil `dev` son pocas: URL, usuario, contraseña,
`ddl-auto=validate` y nivel de log `DEBUG` para `com.mundomanitas`.

### 2.4 Arrancar

```powershell
.\mvnw.cmd spring-boot:run
```

o, si prefieres el JAR:

```powershell
.\mvnw.cmd -DskipTests package
java -jar target\mundomanitas-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

La aplicación queda en <http://localhost:8080>. El arranque tarda entre 40 y 60 segundos en una
máquina modesta: Flyway, Hibernate y la siembra de datos iniciales pesan. No es un cuelgue.

### 2.5 Verificación rápida

Cinco minutos después de arrancar deberías poder hacer esto:

1. Abrir `http://localhost:8080` sin sesión y ver la página pública de inicio.
2. Entrar con `admin@mundomanitas.com` / `123456` y abrir `/admin/usuarios`.
3. Entrar con `docente@mundomanitas.com` / `123456` y abrir `/docente/cursos`.
4. Entrar con `alumno@mundomanitas.com` / `123456`, abrir `/cursos/3` y ver videos y ejercicios.
5. Consultar `http://localhost:8080/actuator/health` y obtener `{"status":"UP"}`.

### 2.6 Usuarios demo

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Estudiante | `alumno@mundomanitas.com` | `123456` |
| Estudiante | `maria@mundomanitas.com` | `123456` |
| Estudiante | `diego@mundomanitas.com` | `123456` |
| Estudiante | `lucia@mundomanitas.com` | `123456` |
| Padre | `padre@mundomanitas.com` | `123456` |
| Docente | `docente@mundomanitas.com` | `123456` |
| Administrador | `admin@mundomanitas.com` | `123456` |
| Sistemas | `sistemas@mundomanitas.com` | `123456` |

Las cuentas demo conservan `123456` a propósito para facilitar la evaluación. La política de
contraseñas se exige al crear cuentas, al cambiarlas y al restablecerlas, pero no al iniciar
sesión; de lo contrario las cuentas históricas dejarían de funcionar.

La siembra también crea una postulación pendiente de ejemplo (`ana.quispe@ejemplo.com`) para
probar el flujo de aprobación.

---

## 3. Arquitectura

### 3.1 Capas

```
Navegador
   │
   ├── MVC (Thymeleaf)                 ├── API REST (JSON)
   │      controller                        api
   │          │                              │
   │          └──────────┬───────────────────┘
   │                     ▼
   │                  service  ──►  dao  ──►  repository  ──►  PostgreSQL
   │                     │            │
   │                 dto.view     entity / dto.api
   │
   └── static (CSS, JS, imágenes)
```

Los paquetes de `com.mundomanitas`:

| Paquete | Responsabilidad |
| --- | --- |
| `controller` | Controladores MVC; reciben la petición, arman el modelo y devuelven el nombre de la vista |
| `api` | Controladores REST y manejo de errores en JSON |
| `service` | Reglas de negocio y transacciones |
| `dao` | Contratos de acceso a datos (`UsuarioDao`, `CursoDao`, etc.) e implementaciones |
| `repository` | Interfaces Spring Data JPA; solo las usa la capa DAO |
| `entity` | Entidades JPA |
| `dto.api` | Records de entrada/salida de la API |
| `dto.view` | Records que consume Thymeleaf |
| `dto.form` | Formularios MVC |
| `security` | Filtro JWT, `UserDetailsService`, manejadores 401/403, auditoría de autenticación |
| `config` | Seguridad y siembra de datos |
| `web` | `ControllerAdvice` global, navegación del sidebar y errores de vistas |
| `exception` | Excepciones de negocio |

**Reparto del código.** El backend es Java de extremo a extremo. De los 204 archivos fuente,
149 son Java (73 %) y suman 10.811 líneas; el resto es la capa de presentación (HTML, CSS) y
los scripts de apoyo (SQL, PowerShell, configuración). Si se mide solo la lógica de aplicación
—Java, SQL, configuración y scripts, sin plantillas ni hojas de estilo—, Java representa cerca
del 90 %. El único JavaScript de la aplicación es el de la landing.

### 3.2 Por qué DAO si ya existe Spring Data

Es una decisión heredada del alcance del curso: la rúbrica pide una capa DAO explícita. En la
práctica, `AbstractDao` y las implementaciones añaden una capa fina sobre los repositorios de
Spring Data. La ventaja es que los servicios no conocen `JpaRepository` ni las consultas
derivadas; la desventaja es más código para operaciones simples.

No la elimines a mitad de proyecto. Si algún día se simplifica, conviene hacerlo con pruebas
en verde y en un commit aislado, porque toca todos los servicios.

### 3.3 Recorrido de una petición

Tomemos `GET /cursos/3`:

1. `SecurityConfig` comprueba que el usuario tenga la autoridad `MODULO_CURSOS`.
2. `CursosController.detalleModulo` obtiene el usuario autenticado con `UsuarioActualService`.
3. `CursoService.detalle` carga el módulo, sus videos, ejercicios e inscripción, y construye un
   `CursoDetalleView`. Todo el armado ocurre dentro de la transacción.
4. El controlador agrega el DTO al `Model` y devuelve `"curso-detalle"`.
5. Thymeleaf resuelve `templates/curso-detalle.html`, que incluye el sidebar y los fragmentos
   de panel.
6. El navegador descarga `base.css`, `components.css`, `layout.css` y `curso-detalle.css`.

La consecuencia práctica de `spring.jpa.open-in-view=false` es que **no puedes tocar relaciones
perezosas desde la plantilla**. Si un DTO necesita un dato, se resuelve en el servicio. Un
`LazyInitializationException` en producción casi siempre significa que alguien agregó una
llamada a una entidad en el HTML.

### 3.4 Dónde va cada cosa

| Si vas a... | Va en... |
| --- | --- |
| Agregar una ruta web | `controller` + plantilla en `templates` |
| Agregar una regla de negocio | `service` |
| Agregar una consulta | `dao` (contrato) e `impl` (implementación) |
| Cambiar el JSON de la API | `dto.api` y el controlador de `api` |
| Cambiar lo que ve una página | `dto.view` y la plantilla |
| Cambiar estilos globales | `static/css/base.css` |
| Cambiar un componente reutilizable | `static/css/components.css` |
| Cambiar el layout interno | `static/css/layout.css` |
| Cambiar la página pública | `landing.html`, `landing.css`, `landing.js` |

### 3.5 Errores

Las excepciones de negocio son dos y conviene usarlas con intención:

- `RecursoNoEncontradoException` → 404.
- `ReglaNegocioException` → 400, con el mensaje que verá la persona.

`GlobalExceptionHandler` renderiza `error/403`, `error/404` o `error/error` para MVC.
`ApiExceptionHandler` devuelve JSON con la forma:

```json
{
  "error": "Solicitud inválida",
  "mensaje": "El módulo indicado no existe.",
  "detalles": {}
}
```

No captures excepciones en el controlador solo para devolver un `ModelAndView`; para eso está el
handler global. La excepción debe llevar un mensaje útil, en español, sin jerga técnica.

---

## 4. Base de datos

### 4.1 Tablas

| Tabla | Contenido |
| --- | --- |
| `rol` | Roles del sistema |
| `modulo_sistema` | Secciones de navegación (`DASHBOARD`, `CURSOS`, `ADMIN_USUARIOS`, etc.) |
| `rol_modulo` | Qué secciones ve cada rol en el sidebar |
| `usuario` | Cuenta, correo, contraseña BCrypt, rol, estado, intentos fallidos y token de recuperación |
| `estudiante` | Perfil académico: grado, edad, nivel de estudio |
| `modulo` | Curso o módulo; incluye `idDocente` (responsable), `premium` y `precio` |
| `video` | Videos de un módulo, con URL, duración y orden |
| `ejercicio` | Ejercicios, con tipo, contenido JSON y respuesta esperada |
| `inscripcion` | Matrícula de un estudiante en un módulo |
| `progreso` | Porcentaje, calificación y fechas por estudiante y módulo |
| `video_progreso` | Marca de video completado |
| `intento_ejercicio` | Cada intento, con respuesta, resultado y puntaje |
| `compra` | Compra simulada de un curso premium: monto, referencia, método y fecha |
| `postulacion_docente` | Solicitud de alta docente y su resolución |

Los identificadores se guardan en minúsculas (`idmodulo`, `idestudiante`) porque PostgreSQL
pliega los identificadores no citados. Las entidades usan `@Column(name = "idModulo")` y
Hibernate los resuelve sin problema; si escribes SQL a mano, usa minúsculas para no llevarte
sorpresas.

### 4.2 Reglas que viven en la base

- El correo del usuario es único.
- `progreso` tiene un índice único por `(idestudiante, idmodulo)`: no puede haber dos registros
  de avance para la misma pareja.
- Las claves foráneas obligan a borrar el contenido antes que el módulo; `CursoService.eliminarModulo`
  elimina primero videos, ejercicios y progreso, y después el módulo.
- `inscripcion` también es única por estudiante y módulo; la lógica de servicio devuelve la
  inscripción existente en lugar de duplicarla.
- `compra` es única por `(idestudiante, idmodulo)`: la restricción `uk_compra_estudiante_modulo`
  impide cobrar dos veces el mismo curso. La compra guarda `monto`, `referencia`, `metodoPago`
  (`SIMULADO`) y `fechaCompra` para que quede auditable.

### 4.3 Flyway

Las migraciones están en `src/main/resources/db/migration`:

| Archivo | Qué introduce |
| --- | --- |
| `V1__baseline_schema.sql` | Tablas base, claves foráneas e índices |
| `V2__security_progress_and_indexes.sql` | Bloqueo y recuperación de cuentas, inscripción, progreso de videos, intentos e índices |
| `V3__course_owner.sql` | `modulo.idDocente` para el panel docente |
| `V4__teacher_applications.sql` | `postulacion_docente` |
| `V5__premium_courses.sql` | `modulo.premium`, `modulo.precio`, tabla `compra` y el curso demo premium (módulo 3, `S/ 49.90`) |

Reglas de oro:

1. **No edites una migración ya aplicada.** Flyway guarda un checksum; si cambia, la aplicación
   no arranca. Para corregir algo, crea `V6__descripcion.sql`.
2. Usa `IF NOT EXISTS` cuando tenga sentido; la siembra y las migraciones se ejecutan sobre bases
   que pueden tener datos.
3. Prueba la migración contra una base limpia y contra una base con datos. No basta con que
   funcione en tu máquina.
4. Si cambias una entidad, actualiza también la migración y ejecuta la aplicación con
   `ddl-auto=validate`. Hibernate es estricto y fallará si la columna no coincide.

Ejemplo de migración nueva:

```sql
-- V5__add_module_cover.sql
ALTER TABLE modulo ADD COLUMN IF NOT EXISTS portada varchar(200);
```

### 4.4 Datos iniciales

`DatosInicialesConfig` es un `CommandLineRunner` idempotente. En cada arranque:

- Crea roles y secciones de navegación si no existen.
- Crea los usuarios demo si el correo no está registrado.
- Crea los cuatro módulos y el contenido de Java solo si `modulo` está vacía.
- Asigna los módulos sin docente al docente demo.
- Crea perfiles de estudiante, progreso y matrículas solo si no hay registros previos.

Eso significa que **reiniciar la aplicación no borra el avance real**: los métodos de siembra
salen temprano si ya hay datos. Para dejar la base como recién instalada hay que borrarla:

```sql
DROP DATABASE mundo_manitas;
CREATE DATABASE mundo_manitas;
```

o, con Docker, `docker compose down -v` (destructivo: elimina también el volumen).

---

## 5. Seguridad

### 5.1 Dos mecanismos que conviven

La interfaz web usa autenticación por formulario con sesión. La API usa JWT. No son
alternativas: el mismo `SecurityFilterChain` acepta ambas.

- **Web:** `POST /login` con `username` y `password`; Spring crea la sesión y redirige a
  `/dashboard`. CSRF está activo.
- **API:** `POST /api/auth/login` devuelve un token; el cliente lo envía como
  `Authorization: Bearer <token>`. El filtro `JwtAuthenticationFilter` se ejecuta antes de
  `UsernamePasswordAuthenticationFilter` y autentica la petición si el token es válido.

CSRF se ignora solo para `/api/**`, porque esos clientes no tienen sesión ni formulario.

### 5.2 Roles y autoridades

Hay dos conceptos que se confunden con facilidad:

- `ROLE_*` viene del rol del usuario (`ROLE_ESTUDIANTE`, `ROLE_DOCENTE`, ...).
- `MODULO_*` viene de la tabla `rol_modulo` y controla el acceso a secciones.

El sidebar se arma desde la base (`NavegacionService`), no desde el HTML. Si agregas una
sección nueva, no basta con crear la plantilla: hay que registrar el `ModuloSistema`, asignarlo
a los roles y agregar la regla en `SecurityConfig`.

### 5.3 Matriz de rutas

| Ruta | Acceso |
| --- | --- |
| `/`, `/login`, `/registro`, `/registro/**`, `/recuperar`, `/restablecer`, `/error/**`, `/css/**`, `/js/**`, `/img/**` | Público |
| `/dashboard` | `MODULO_DASHBOARD` |
| `/perfil` | `MODULO_PERFIL` |
| `/cursos/**` | `MODULO_CURSOS` |
| `/docente/**` | `MODULO_DOCENTE_CURSOS` |
| `/admin` | `ADMINISTRADOR`, `SISTEMAS` |
| `/admin/contenido`, `/admin/modulos`, `/admin/videos`, `/admin/ejercicios` | `DOCENTE`, `ADMINISTRADOR`, `SISTEMAS` |
| `/admin/usuarios`, `/admin/postulaciones` | `ADMINISTRADOR`, `SISTEMAS` |
| `/reportes/progreso.xlsx` | `DOCENTE`, `ADMINISTRADOR`, `SISTEMAS` |
| `/reportes/certificado/**` | Autenticado (el servicio exige 100 %) |
| `/actuator/health`, `/actuator/info` | Público |

La landing `/` es pública, pero si la petición trae una sesión válida el controlador redirige a
`/dashboard`. Así el enlace del logo funciona igual para visitantes y para usuarios.

### 5.4 Contraseñas, bloqueo y recuperación

- BCrypt para almacenar contraseñas.
- Política: mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial.
- La política **no** se aplica al login, solo al alta y al cambio.
- Tras 5 intentos fallidos la cuenta se bloquea 15 minutos. Los valores se configuran con
  `SECURITY_MAX_INTENTOS` y `SECURITY_BLOQUEO_MINUTOS`.
- La recuperación genera un token de un solo uso con vigencia de 30 minutos. La respuesta no
  revela si el correo existe.
- El cambio de contraseña desde el perfil exige la contraseña actual.

### 5.5 JWT

El secreto debe tener al menos 32 caracteres; si es más corto, JJWT falla al firmar. En local
hay un valor por defecto, pero en producción es obligatorio definirlo:

```bash
JWT_SECRET="una-cadena-larga-y-aleatoria"
JWT_EXPIRATION_MINUTES=120
```

El token incluye el correo y las autoridades. Si cambias el rol de un usuario, el token viejo
conserva los permisos anteriores hasta que expire; para pruebas, vuelve a iniciar sesión.

### 5.6 Auditoría

`AuditoriaService` escribe en el logger `AUDITORIA`, que Logback envía a
`logs/auditoria.log`. Se registran accesos, bloqueos, aprobaciones y rechazos de postulaciones
y cambios administrativos. Es el primer sitio donde mirar cuando alguien pregunta "¿quién hizo
esto?".

---

## 6. Vistas, CSS y JavaScript

### 6.1 Thymeleaf

Las plantillas están en `src/main/resources/templates`:

- `landing.html`, `login.html`, `registro*.html`, `recuperar.html`, `restablecer.html`.
- `dashboard.html`, `cursos.html`, `curso-detalle.html`, `perfil.html`.
- `admin/panel.html`, `admin/usuarios.html`, `admin/contenido.html`,
  `admin/postulaciones.html`.
- `docente/cursos.html`, `docente/curso.html`.
- `fragments/sidebar.html`, `fragments/panel/kpi.html`, `fragments/panel/distribucion.html`,
  `fragments/dashboard/*.html`.
- `error/403.html`, `error/404.html`, `error/error.html`.

El sidebar y los fragmentos de panel se insertan con `th:replace`. Si una página nueva usa las
clases de panel (`.panel-card`, `.kpi-grid`, `.course-row`, `.create-panel`), **debe cargar
`panel.css`**. Nos pasó al agregar `/admin/usuarios` y `/admin/postulaciones`: el HTML era
correcto, pero la página se veía "rota" porque faltaba esa hoja. No es un error de Thymeleaf;
es una hoja que no se pidió.

Orden recomendado de hojas:

```html
<link rel="stylesheet" th:href="@{/css/base.css}">
<link rel="stylesheet" th:href="@{/css/components.css}">
<link rel="stylesheet" th:href="@{/css/layout.css}">
<link rel="stylesheet" th:href="@{/css/panel.css}">      <!-- si usa panel -->
<link rel="stylesheet" th:href="@{/css/dashboard.css}">  <!-- si usa KPIs -->
<link rel="stylesheet" th:href="@{/css/cursos.css}">     <!-- si usa tarjetas de curso -->
<link rel="stylesheet" th:href="@{/css/<pagina>.css}">   <!-- al final -->
```

### 6.2 Un solo lenguaje visual

Toda la plataforma comparte el lenguaje de la página pública: fondo crema (`#fff9ef`), tinta
verde profunda, acentos **teal** (`#0f766e`) y **clay** (`#c2410c`), tipografías redondeadas
(**Baloo 2** para títulos y **Nunito** para texto) y tarjetas suaves con bordes cálidos.

Los tokens viven en `base.css` (`--primary`, `--accent`, `--paper`, `--surface`, `--radius-*`,
`--shadow-*`) y los reutilizan todas las hojas. `components.css` define botones, badges,
formularios, tablas y estados; `layout.css` define el sidebar claro y la topbar; cada pantalla
aporta su hoja específica (`dashboard.css`, `cursos.css`, `curso-detalle.css`, `perfil.css`,
`panel.css`, `login.css`, `auth.css`).

La landing conserva su aislamiento técnico para no cargar el CSS de la aplicación:

- `landing.html` no incluye `base.css` ni `layout.css`.
- `landing.css` define sus propios tokens (`--lp-*`), pero replica la misma paleta y tipografías.
- `landing.js` es el único JavaScript de la página.

Si cambias un color o una tipografía, actualiza ambos lados: los tokens de `base.css` y las
variables `--lp-*` de `landing.css`. La coherencia visual es parte de la rúbrica de UI/UX.

### 6.3 La landing

La página `/` se arma con datos reales:

- `LandingController` pide `CursoService.listarVitrina(4)`.
- `VitrinaView` trae los cursos y los totales de módulos, videos y ejercicios.
- Si hay sesión, redirige a `/dashboard`.

La maqueta tiene seis bloques: portada, cifras, catálogo, demostración de progreso,
testimonios y llamado final. Los testimonios son texto fijo en la plantilla; no están en la
base. Es una decisión consciente para no inventar una tabla de testimonios que el producto
todavía no administra.

Detalles que conviene conservar:

- El enlace "Saltar al contenido" y los `:focus-visible`.
- Los `aria-hidden="true"` en las tarjetas decorativas (progreso y demo).
- `prefers-reduced-motion`: la animación de entrada y los flotantes se desactivan.
- El menú móvil usa `aria-expanded` y se cierra con `Escape` o al hacer clic fuera.
- Los conteos de videos y ejercicios se muestran solo si son mayores que cero; si el módulo
  aún no tiene contenido, la tarjeta dice "A tu ritmo" y "Acceso inmediato". Un "0 videos"
  en una landing de venta no ayuda a nadie.

### 6.4 JavaScript

No hay bundler ni framework. `landing.js` es un IIFE que agrega la clase `lp-js` al `body` para
que la animación solo se aplique si el JS cargó. Si el archivo falla, la página se ve completa.

Si agregas interacción en el interior, sigue el mismo criterio: mejora progresiva, sin depender
del JS para que el contenido sea legible.

### 6.5 Accesibilidad

El proyecto nace para la comunidad sorda, así que la accesibilidad no es un extra:

- No hay contenido que dependa solo del audio.
- Los videos se acompañan de texto y de una acción explícita ("Marcar completado").
- Los mensajes de estado se muestran como flash visibles, no como alertas del navegador.
- Los formularios tienen `label` asociado y mensajes de error en texto.
- Los botones iconográficos llevan `aria-label` o texto visible.
- El foco es visible en todos los controles.

Cuando revises una pantalla nueva, prueba con teclado: Tab, Enter, Escape. Si algo no se puede
usar sin mouse, está mal.

### 6.6 Responsive

Los cortes principales del interior son 900 px (sidebar colapsa) y 760 px (grids a una columna).
La landing usa 1024, 860 y 640 px. En 860 el menú se vuelve hamburguesa; en 640 las tarjetas de
curso pasan a una columna y se ocultan los flotantes decorativos que taparían el contenido.

---

## 7. Flujos funcionales

### 7.1 Catálogo

`GET /cursos` acepta `q`, `page` y `vista`. Para un estudiante, `vista=mis` muestra sus
inscripciones con acceso y `vista=explorar` los módulos que aún no cursa. Un curso premium
bloqueado aparece en el catálogo aunque exista una inscripción histórica: así el estudiante
siempre encuentra el botón de compra. Los contadores de las pestañas usan la misma regla, por
eso "Mis cursos" y "Explorar catálogo" suman el total de módulos. La paginación es de 6 elementos.
Para docentes y administradores, el catálogo es global y sin pestañas.

La búsqueda se hace en el servicio, sobre título y descripción, sin distinguir mayúsculas.
No hay búsqueda por nivel ni por docente; si se necesita, el lugar es `CursoService.listar`.

### 7.2 Inscripción y progreso

`InscripcionService.inscribir` verifica que el módulo exista, devuelve la inscripción si ya
existe y crea el registro de progreso en 0 %. Cancelar la inscripción elimina la matrícula,
pero no borra el progreso histórico.

`ProgresoService.recalcular` cuenta videos completados y ejercicios correctos sobre el total de
contenidos. El resultado se redondea y **nunca baja**: se conserva el mayor valor alcanzado.
Eso permite repetir un ejercicio sin perder avance, que es una regla de producto, no un detalle
técnico.

### 7.3 Detalle del módulo

`/cursos/{id}` muestra videos, ejercicios y el estado de avance. Las acciones son POST con
formulario y flash:

- `POST /cursos/{id}/inscribirse`
- `POST /cursos/{id}/inscripcion/cancelar`
- `POST /cursos/{id}/videos/{idVideo}/completar`
- `POST /cursos/{id}/ejercicios/{idEjercicio}/responder`
- `POST /cursos/{id}/comprar` (solo cursos premium)

Los ejercicios tienen tres tipos (`cuestionario`, `completar`, `abierta`). La respuesta correcta
se guarda en JSON y se compara en `EjercicioService`; el intento se registra siempre, con
puntaje y resultado.

### 7.3.1 Cursos premium y compra simulada

Un módulo puede marcarse como premium desde `/admin/contenido` (checkbox y precio). Las reglas
viven en `CursoService` y `CompraService`:

1. `CursoService.detalle` calcula `comprado` y `bloqueado`. Si el curso es premium y no hay
   compra, devuelve listas vacías de videos y ejercicios: el contenido no viaja al HTML.
2. `POST /cursos/{id}/comprar` llama a `CompraService.comprar`, que valida que el módulo exista,
   que sea premium y que tenga precio mayor que cero.
3. La compra se guarda en `compra` con estado `PAGADA`, método `SIMULADO` y una referencia
   `MM-<estudiante>-<modulo>-<fecha>`. No se contacta ninguna pasarela: el pago es una simulación
   académica.
4. Comprar también crea la inscripción y el registro de progreso en 0 %. Si el estudiante vuelve
   a comprar, el servicio devuelve la compra existente y no duplica el cobro.
5. `CursoService.inscribir`, `marcarVideoCompletado` y `responder` llaman a `verificarAcceso`:
   sin compra no se puede consumir el contenido premium aunque se conozca la URL.

En la vista, el curso bloqueado muestra el paywall (`premium-paywall`), el precio y el botón de
compra. Tras comprar, la tarjeta cambia a "Comprado", el módulo aparece en "Mis cursos" y el
contenido queda disponible.

### 7.4 Panel docente

`/docente/cursos` muestra solo los módulos cuyo `idDocente` coincide con el usuario. Los KPIs
salen de `PanelService` y el detalle de alumnos de `CursoService`. La verificación de propiedad
está en `CursoService.verificarPropiedad`; si un docente intenta abrir un curso ajeno, recibe
un 400 con mensaje, no un 404 silencioso.

### 7.5 Administración

- `/admin` es el panel global: usuarios, cursos, postulaciones y distribución por rol.
- `/admin/usuarios` crea cuentas, cambia rol/estado, edita datos y elimina.
- `/admin/contenido` es el editor de módulos, videos y ejercicios.
- `/admin/postulaciones` lista las solicitudes; aprobar exige módulo y contraseña temporal;
  rechazar exige un motivo de al menos 10 caracteres.

### 7.6 Reportes y certificados

- `GET /reportes/progreso.xlsx` genera un Excel con el avance de todos los estudiantes.
- `GET /reportes/certificado/{idModulo}.docx` genera el certificado del estudiante autenticado.
  Si el módulo no está al 100 %, el servicio lanza una regla de negocio y no se genera nada.

Ambos usan Apache POI. Las plantillas y estilos están en `ReporteService` y
`CertificadoService`; si cambias el formato, revisa las pruebas de `ServiciosNegocioTests`,
que validan que el archivo se pueda abrir y tenga el contenido esperado.

---

## 8. API REST

La API existe para integraciones y para demostrar la separación MVC/REST. No la usa la interfaz
web; la web trabaja con sesión.

### 8.1 Autenticación

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alumno@mundomanitas.com","password":"123456"}'
```

Respuesta:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

Guarda el token y úsalo en las demás llamadas:

```bash
curl http://localhost:8080/api/cursos \
  -H "Authorization: Bearer $TOKEN"
```

### 8.2 Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Devuelve el JWT |
| `POST` | `/api/auth/recuperar` | Solicita recuperación |
| `POST` | `/api/auth/restablecer` | Cambia la contraseña con token |
| `POST` | `/api/auth/postulaciones` | Registra una postulación docente (público) |
| `GET` | `/api/cursos?q=&page=&size=` | Catálogo paginado |
| `GET` | `/api/cursos/{id}` | Detalle con videos, ejercicios y progreso |
| `POST` | `/api/cursos/{id}/inscripcion` | Inscribirse |
| `DELETE` | `/api/cursos/{id}/inscripcion` | Cancelar inscripción |
| `POST` | `/api/videos/{id}/completar` | Marcar video como visto |
| `POST` | `/api/ejercicios/{id}/intento` | Enviar respuesta |
| `GET` | `/api/progreso` | Progreso por módulo |
| `GET` | `/api/progreso/general` | Porcentaje general |
| `GET` | `/api/inscripciones` | Inscripciones del estudiante |
| `GET/POST` | `/api/admin/usuarios` | Listar o crear usuarios |
| `GET/PUT/DELETE` | `/api/admin/usuarios/{id}` | Detalle, edición y borrado |
| `PATCH` | `/api/admin/usuarios/{id}/estado?activo=` | Activar o desactivar |
| `GET/POST` | `/api/admin/modulos` | Listar o crear módulos |
| `PUT/DELETE` | `/api/admin/modulos/{id}` | Editar o borrar |
| `GET/POST` | `/api/admin/videos`, `/api/admin/ejercicios` | Contenido |
| `GET` | `/api/admin/postulaciones?estado=` | Listar postulaciones |
| `POST` | `/api/admin/postulaciones/{id}/aprobar` | Aprobar |
| `POST` | `/api/admin/postulaciones/{id}/rechazar` | Rechazar |

### 8.3 Convenciones

- Sin token: 401 con JSON. Con token pero sin permiso: 403 con JSON. No redirige al login.
- Los errores de validación devuelven 400 con `mensaje` legible.
- Los DTO son `record`; no se exponen entidades.
- La paginación usa `page` (base 0) y `size`.

Si agregas un endpoint, actualiza esta tabla y `ApiRestTests`. La API no tiene versión; un cambio
incompatible afecta a cualquiera que la esté consumiendo.

---

## 9. Pruebas

### 9.1 Cómo están organizadas

Las pruebas usan JUnit 5, MockMvc y H2 en memoria con el perfil `test`. Flyway se desactiva y
Hibernate crea el esquema (`ddl-auto=create-drop`), así que las pruebas no dependen de
PostgreSQL.

| Archivo | Qué cubre |
| --- | --- |
| `MundomanitasApplicationTests` | El contexto de Spring carga |
| `AplicacionWebTests` | Login, redirecciones, permisos por rol, páginas 403/404, edición de contenido |
| `ApiRestTests` | Login JWT, 401/403, catálogo, recuperación |
| `ServiciosNegocioTests` | Política de contraseñas, ejercicios, progreso, Excel y certificado |
| `ContenidoCrudTests` | CRUD de módulos, videos y ejercicios; propiedad del docente |
| `PanelServiceTests` | KPIs globales y del docente, distribución, alumnos |
| `PostulacionServiceTests` | Validación, alta pendiente, aprobación, rechazo |
| `PostulacionApiTests` | Postulación por API y permisos |
| `CompraServiceTests` | Compra premium, idempotencia, precio inválido, paywall y acceso al contenido |
| `DatosInicialesTests` | Siembra idempotente, roles, módulos y postulación demo |

### 9.2 Ejecutar

```powershell
.\mvnw.cmd test
```

Una clase o un método:

```powershell
.\mvnw.cmd test -Dtest=PostulacionServiceTests
.\mvnw.cmd test -Dtest=PostulacionServiceTests#aprobarCreaLaCuentaDocenteYAsignaElModulo
```

Si una prueba falla por datos, revisa primero si depende de la siembra. Las pruebas deben crear
sus propios datos o usar los demo de forma explícita; no asumas que un módulo tiene videos.

### 9.3 Verificación de extremo a extremo

Con PostgreSQL arriba y el JAR construido:

```powershell
.\mvnw.cmd -DskipTests package
powershell -ExecutionPolicy Bypass -File scripts/verificar-local.ps1
```

El script arranca la aplicación con perfil `dev`, espera a que responda y comprueba: formulario
de login, health, redirección de anónimos, login JWT, catálogo por API, 403 por rol, sesión web
con catálogo, reporte Excel del docente y certificado Word del estudiante. Es la prueba que más
se acerca a lo que verá el evaluador.

### 9.4 Qué no cubren las pruebas

- El esquema real de PostgreSQL (H2 imita, no es igual).
- El diseño visual y el responsive.
- El envío real de correos de recuperación.
- La descarga de videos externos.

Por eso existen la verificación local y la revisión visual con capturas. No reemplazan a las
pruebas, pero atrapan otra clase de errores.

---

## 10. Convenciones y Git

### 10.1 Código

- Java 21, constructor injection, sin `@Autowired` en campos.
- Nombres de dominio en español (`CursoService`, `InscripcionDao`); términos técnicos en inglés
  cuando son estándar (`repository`, `controller`).
- DTO como `record`; las entidades no salen de la capa de servicio.
- Métodos de servicio con `@Transactional` o `@Transactional(readOnly = true)` según corresponda.
- Mensajes de error para la persona, en español, sin códigos internos.
- CSS con variables (`--primary`, `--surface`, etc.) y prefijos por página (`lp-` en la landing).
- JavaScript sin dependencias externas; si hace falta una librería, se discute antes.

### 10.2 Ramas y commits

El repositorio trabaja con `main` y una rama de entrega (`feature/mi-cambio`). Las entregas se
etiquetan (`entrega-40`, etc.). Para cambios pequeños, un commit por intención:

```
fix(admin): carga panel.css en usuarios y postulaciones
feat(landing): agrega página pública con vitrina real
test(progreso): cubre respuestas incorrectas
```

Evita commits que mezclen refactor, formato y funcionalidad. Si un cambio toca muchas líneas
por formato, sepáralo.

### 10.3 Secretos

- `application-dev.properties` no se versiona.
- `.env` no se versiona; la plantilla es `.env.example`.
- Nunca subas `JWT_SECRET`, `REMEMBER_ME_KEY` ni contraseñas reales.
- Si un secreto se filtró, cámbialo; no basta con borrar el commit.

### 10.4 Antes de abrir un pull request

1. `.\mvnw.cmd test` en verde.
2. `.\mvnw.cmd -DskipTests package` sin errores.
3. Revisión visual de las pantallas tocadas (escritorio y móvil).
4. Si cambió la base, migración nueva y prueba en base limpia.
5. README o este manual actualizados si cambió una ruta, un rol o un comando.

---

## 11. Operación y despliegue

### 11.1 Variables de entorno

| Variable | Uso | Obligatoria en prod |
| --- | --- | --- |
| `DB_URL` | JDBC de PostgreSQL | Sí |
| `DB_USERNAME` | Usuario de base | Sí |
| `DB_PASSWORD` | Contraseña de base | Sí |
| `JWT_SECRET` | Firma de tokens (32+ caracteres) | Sí |
| `REMEMBER_ME_KEY` | Firma de "recordarme" | Sí |
| `JWT_EXPIRATION_MINUTES` | Vigencia del token (120 por defecto) | No |
| `SECURITY_MAX_INTENTOS` | Intentos antes de bloquear (5) | No |
| `SECURITY_BLOQUEO_MINUTOS` | Duración del bloqueo (15) | No |
| `SSL_ENABLED` | Activa HTTPS en la app | No |
| `SSL_KEY_STORE`, `SSL_KEY_STORE_PASSWORD`, `SSL_KEY_STORE_TYPE` | Keystore | Si `SSL_ENABLED=true` |
| `COOKIE_SECURE` | Cookie solo por HTTPS | Recomendado en prod |
| `SERVER_PORT` | Puerto publicado en Docker | No |

### 11.2 Docker

```bash
cp .env.example .env
# edita .env
docker compose up --build
```

Servicios:

- `postgres`: PostgreSQL 16, volumen persistente y healthcheck.
- `app`: imagen multi-etapa (Maven → JRE 21), usuario sin privilegios, perfil `prod`, logs en
  volumen.

El `app` espera a que `postgres` esté sano antes de arrancar. Si Flyway falla al inicio, el
contenedor se reinicia; revisa `docker compose logs app`.

### 11.3 Logs y salud

- `logs/mundo-manitas.log`: aplicación, rotativo.
- `logs/auditoria.log`: accesos, bloqueos y cambios administrativos.
- `/actuator/health`: estado de la app y la base.
- `/actuator/info`: nombre y descripción.
- `/actuator/metrics`: métricas; en producción conviene restringirlo.

En Docker, los logs quedan en el volumen `app-logs`.

### 11.4 Respaldo

```bash
pg_dump -U postgres -d mundo_manitas -F c -f mundo_manitas.backup
pg_restore -U postgres -d mundo_manitas mundo_manitas.backup
```

Prueba el respaldo antes de necesitarlo. Un backup que nunca se restauró es una hipótesis.

### 11.5 TLS

Dos opciones válidas:

1. Terminar TLS en un proxy inverso (Nginx, Caddy, balanceador) y dejar `SSL_ENABLED=false` con
   `server.forward-headers-strategy=framework`.
2. Activar SSL en Spring con un keystore (`SSL_ENABLED=true` y las variables del keystore).

En ambos casos, `COOKIE_SECURE=true` y `SameSite=Lax`.

---

## 12. Problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
| --- | --- | --- |
| `Unable to rename ...jar to ...jar.original` al empaquetar | La app está corriendo y bloquea el JAR | Detén el proceso Java y vuelve a ejecutar `package` |
| `Port 8080 was already in use` | Otra instancia sigue viva | `Get-NetTCPConnection -LocalPort 8080` y cierra el proceso |
| Página sin estilos | Falta una hoja o el orden es incorrecto | Revisa los `<link>`; si usa panel, agrega `panel.css` |
| 403 al entrar a `/admin` | El rol no tiene la autoridad o la ruta no está permitida | Revisa `SecurityConfig` y `rol_modulo` |
| 403 en la API con token válido | El rol no alcanza para ese endpoint | Usa una cuenta con el rol correcto; el token conserva autoridades antiguas |
| 401 en la API sin token | Falta `Authorization: Bearer` | Envía el encabezado |
| Flyway no arranca | Falta `spring-boot-starter-flyway` | En Spring Boot 4 no basta `flyway-core`; el `pom.xml` ya lo incluye |
| `Validate failed: checksum mismatch` | Se editó una migración aplicada | Crea una migración nueva; en local, recrea la base |
| `LazyInitializationException` | Se tocó una relación fuera de la transacción | Resuelve el dato en el servicio y pásalo en el DTO |
| Acentos raros en consola | Codificación de PowerShell | `chcp 65001`; los archivos están en UTF-8 |
| `&&` no funciona | PowerShell 5.1 | Usa `;` o PowerShell 7 |
| `npm.ps1` o `uipro.ps1` bloqueado | ExecutionPolicy | Usa el `.cmd` correspondiente o `-ExecutionPolicy Bypass` |
| `psql` pide contraseña y no la acepta | `PGPASSWORD` no definida | `$env:PGPASSWORD = "..."` antes de invocar `psql` |
| El docente ve un curso ajeno | Se asignó mal `idDocente` | Corrige el módulo desde administración o la base |
| El certificado no se genera | El módulo no está al 100 % | Completa videos y ejercicios; el servicio lo exige |
| La landing muestra "0 videos" | Ya no debería: los conteos se ocultan si son cero | Si reaparece, revisa la condición en `landing.html` |

Cuando algo no encaja, revisa en este orden: logs de la app, respuesta HTTP real (código y
cuerpo), estado de la base y, al final, el HTML generado. Casi siempre el problema está en una
de las dos primeras.

---

## 13. Mapa rápido de archivos

| Quiero cambiar... | Archivo(s) |
| --- | --- |
| La página pública | `templates/landing.html`, `static/css/landing.css`, `static/js/landing.js` |
| El login | `templates/login.html`, `controller/LoginController.java`, `config/SecurityConfig.java` |
| El registro de estudiante | `templates/registro-estudiante.html`, `controller/RegistroController.java`, `service/UsuarioService.java` |
| La postulación docente | `templates/registro-docente.html`, `service/PostulacionService.java`, `service/PostulacionValidator.java` |
| El catálogo | `templates/cursos.html`, `static/css/cursos.css`, `controller/CursosController.java`, `service/CursoService.java` |
| El detalle y el progreso | `templates/curso-detalle.html`, `static/css/curso-detalle.css`, `service/ProgresoService.java`, `service/EjercicioService.java` |
| La compra premium | `service/CompraService.java`, `dao/CompraDao.java`, `entity/Compra.java`, `templates/cursos.html`, `templates/curso-detalle.html` |
| El panel docente | `templates/docente/*`, `controller/DocenteController.java`, `service/PanelService.java` |
| La administración | `templates/admin/*`, `controller/Admin*.java`, `static/css/panel.css` |
| Los reportes | `service/ReporteService.java`, `service/CertificadoService.java`, `controller/ReporteController.java` |
| La seguridad | `config/SecurityConfig.java`, `security/*`, `service/PasswordPolicy.java` |
| La base | `db/migration/*.sql`, `entity/*`, `config/DatosInicialesConfig.java` |
| Las pruebas | `src/test/java/com/mundomanitas/*` |

### Cómo agregar una pantalla nueva

1. Crea el método en un controlador y decide la autoridad que la protege.
2. Agrega la lógica en un servicio; no la escribas en el controlador.
3. Crea el DTO de vista con los datos que la plantilla necesita.
4. Crea la plantilla y enlaza las hojas en el orden correcto.
5. Si la pantalla debe aparecer en el sidebar, registra el `ModuloSistema`, asígnalo a los roles
   en `DatosInicialesConfig` y agrega la regla en `SecurityConfig`.
6. Agrega una prueba de acceso (200/403) y, si hay reglas, una prueba de servicio.
7. Actualiza la tabla de rutas de este manual.

El paso 5 es el que más se olvida: la ruta puede funcionar escribiéndola a mano y aun así no
aparecer en el menú, porque el menú se lee de la base.

---

## 14. Deuda técnica y decisiones discutibles

Esta sección existe para que nadie tenga que redescubrir el contexto a los golpes.

- **Solo el módulo 3 tiene videos y ejercicios.** Es el curso premium de demostración
  (`S/ 49.90`) y sirve para mostrar el paywall y el desbloqueo. Los otros tres existen como
  catálogo y tienen progreso demo, pero no contenido. La landing lo maneja ocultando los
  conteos en cero. La solución de fondo es cargar contenido, no maquillar la vista.
- **Los testimonios de la landing son estáticos.** No hay tabla ni moderación. Si el producto
  crece, esto debería pasar a base de datos con aprobación.
- **La capa DAO es más gruesa de lo necesario.** Se mantiene por el alcance académico. No es
  el lugar para una refactorización rápida.
- **Las pruebas usan H2.** Cubren reglas y seguridad, no el comportamiento exacto de PostgreSQL.
  La verificación local compensa en parte.
- **El progreso es monotónico.** Un ejercicio mal respondido no baja el porcentaje. Es una
  decisión de producto; si se cambia, hay que revisar `ProgresoService.recalcular` y las pruebas.
- **El token JWT no se invalida al cambiar el rol.** Para un sistema real convendría una lista
  de revocación o tokens cortos con refresh.
- **La landing y el interior tienen sistemas visuales distintos.** Es intencional; unificarlos
  requiere una fase de diseño, no un parche de CSS.

---

## 15. Glosario

| Término | Significado en este proyecto |
| --- | --- |
| Módulo | Curso. En la base y en el código se llama `modulo` para no chocar con `ModuloSistema` |
| Módulo del sistema | Sección del sidebar (`DASHBOARD`, `CURSOS`, etc.) |
| Progreso | Porcentaje de videos y ejercicios completados en un módulo |
| Intento | Registro de una respuesta a un ejercicio, correcta o no |
| Postulación | Solicitud para ser docente; no crea cuenta hasta ser aprobada |
| Vitrina | Subconjunto del catálogo que se muestra en la landing |
| Autoridad | Permiso `MODULO_*` que habilita una sección |
| Flash | Mensaje temporal que se muestra tras una redirección |

---

## 16. Checklist de entrega

- [x] `.\mvnw.cmd test` en verde (85 pruebas).
- [x] `.\mvnw.cmd -DskipTests package` sin errores.
- [x] Landing en 1440 px y 390 px sin solapamientos.
- [x] Login con los cinco roles.
- [x] `/admin/usuarios`, `/admin/postulaciones`, `/admin/contenido` con estilos completos.
- [x] `/docente/cursos` y `/docente/cursos/{id}` solo con cursos asignados.
- [x] `/cursos/3` con videos, ejercicios y progreso.
- [x] Compra premium registrada en `compra` y curso desbloqueado.
- [ ] Certificado generado al 100 %.
- [ ] `scripts/verificar-local.ps1` sin fallos.
- [x] README y manual actualizados si cambió una ruta o un comando.
- [x] Sin secretos en el diff.

---

*Este manual se mantiene junto al código. Si algo de aquí ya no coincide con la aplicación, el
error es del documento: corrígelo en el mismo commit que cambió el comportamiento.*
