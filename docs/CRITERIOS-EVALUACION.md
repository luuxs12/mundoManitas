# Cumplimiento de los criterios de evaluación

Este documento relaciona cada criterio de **Desarrollo de la Solución Informática** y de
**Control de Versiones** con la evidencia concreta dentro del repositorio.

---

## 1. Desarrollo de la Solución Informática

### 1.1 Arquitectura inicial con MVC, TDD, DAO, SOLID y seguridad

| Principio | Cómo se aplica | Evidencia |
| --- | --- | --- |
| **MVC** | Controladores delgados, servicios con la lógica y vistas Thymeleaf. La API REST reutiliza los mismos servicios. | `src/main/java/com/mundomanitas/controller`, `api`, `service` y `src/main/resources/templates` |
| **DAO** | Contrato genérico `GenericDao<T, ID>`, implementación base `AbstractDao` y DAOs concretos por entidad. Los servicios dependen de las interfaces, no de JPA directamente. | `src/main/java/com/mundomanitas/dao` |
| **SOLID** | SRP (controlador / servicio / DAO separados), OCP y DIP (servicios contra interfaces), LSP e ISP (`GenericDao` + `AbstractDao`), DTO para no exponer entidades. | `dao/`, `service/`, `dto/view/`, `dto/api/` y sección **Arquitectura** del `README.md` |
| **TDD / pruebas automatizadas** | Suite de pruebas de integración y unitarias con JUnit 5, MockMvc, Spring Security Test y H2. Se ejecutan con `./mvnw test`. | `src/test/java/com/mundomanitas` — **97 pruebas**, 0 fallos |
| **Seguridad** | Spring Security con sesión + JWT, CSRF, autorización por rol y por módulo, bloqueo de cuentas, política de contraseñas, recuperación con token de un solo uso y páginas 401/403/404. | `security/`, `config/SecurityConfig`, `application-prod.properties` y sección **Seguridad** del `README.md` |

**Comprobación rápida**

```powershell
./mvnw test
```

Resultado esperado: `Tests run: 97, Failures: 0, Errors: 0`.

---

### 1.2 Librerías de apoyo utilizadas

| Librería | Uso real en el proyecto | Evidencia |
| --- | --- | --- |
| **Google Guava** | Caché en memoria del catálogo con estadísticas de aciertos/fallos. | `service/CatalogoCacheService.java` |
| **Apache POI** | Generación de reportes Excel (`.xlsx`) y certificados Word (`.docx`). | `service/ReporteService.java`, `service/CertificadoService.java` |
| **Apache Commons Lang / Text / IO** | Normalización de textos, validaciones y escape de datos de auditoría. | `service/AuditoriaService.java`, `service/UsuarioService.java`, `service/PerfilService.java`, `security/JwtAuthenticationFilter.java` |
| **Logback** | Consola, archivo rotativo y logger de auditoría independiente. | `src/main/resources/logback-spring.xml`, `service/AuditoriaService.java` |
| **Jackson** | Lectura y escritura del contenido JSON de los ejercicios. | `service/EjercicioService.java` |
| **JJWT** | Firma y validación de tokens de la API REST. | `security/JwtService.java`, `security/JwtAuthenticationFilter.java` |

Todas las dependencias están declaradas y versionadas en `pom.xml`.

---

### 1.3 Avance funcional (mínimo 30 %)

El proyecto supera ampliamente el 30 % solicitado. Funcionalidades entregadas:

- Autenticación web con sesión y API REST con JWT.
- Registro público de estudiantes y postulación de docentes con validación.
- Recuperación y cambio de contraseña con política de seguridad.
- Perfil editable por el propio usuario.
- Catálogo de cursos con búsqueda, paginación e inscripción.
- Detalle de módulo con videos, ejercicios, progreso y certificado.
- Paneles diferenciados para estudiante, docente, administrador y sistemas.
- CRUD real de usuarios, módulos, videos y ejercicios sobre PostgreSQL.
- Aprobación/rechazo de postulaciones docentes con asignación de módulo.
- Reportes Excel y certificados Word.
- KPIs accionables y métricas por curso y por rol.
- Migraciones Flyway y datos semilla idempotentes.

**Buenas prácticas de seguridad aplicadas**

- Contraseñas con BCrypt.
- CSRF activo en la interfaz web.
- Cookies seguras y HTTPS configurables en producción.
- Autorización por rol y por módulo, no solo por ruta.
- Bloqueo temporal tras intentos fallidos.
- Tokens de recuperación de un solo uso con caducidad.
- Auditoría de accesos y cambios administrativos en un log separado.

---

## 2. Control de Versiones

| Criterio | Cumplimiento | Evidencia |
| --- | --- | --- |
| **Integración con Git y GitHub** | Repositorio con historial de commits, ramas de trabajo y remotos configurados. | `git log`, `git remote -v` (`mio` y `upstream`) |
| **Entregas versionadas y documentadas** | Cada avance se agrupa en commits descriptivos; el `README.md` documenta arquitectura, seguridad, API, ejecución, pruebas y despliegue. | `README.md`, historial de Git |
| **Avance al 40 %** | El repositorio contiene un avance funcional muy superior al 40 %, con pruebas y verificación de extremo a extremo. | `scripts/verificar-local.ps1` y suite de pruebas |
| **Taller de versionamiento** | Se aplican los conceptos: ramas, commits atómicos, mensajes descriptivos, remotos y publicación en GitHub. | Flujo de trabajo descrito abajo |

**Flujo de trabajo utilizado**

```bash
git status
git add .
git commit -m "descripcion del cambio"
git push mio main
git push mio feature/mi-cambio
```

**Ramas**

- `main`: versión estable y presentable del proyecto.
- `feature/mi-cambio`: rama de trabajo de la entrega actual.

**Verificación de la entrega**

```powershell
./mvnw -DskipTests package
powershell -ExecutionPolicy Bypass -File scripts/verificar-local.ps1
```

El script comprueba 22 puntos de extremo a extremo: login web, salud de la aplicación,
redirección de anónimos, login JWT, catálogo por API, permisos por rol, sesión web,
reporte Excel, certificado Word y CRUD de contenido.

---

## 3. Recomendaciones para la exposición

1. Mostrar el historial con `git log --oneline --graph`.
2. Abrir el `README.md` y recorrer arquitectura, seguridad y pruebas.
3. Ejecutar `./mvnw test` y mostrar el resumen en verde.
4. Ejecutar `scripts/verificar-local.ps1` y mostrar las 22 comprobaciones.
5. Demostrar un flujo real: crear usuario, iniciar sesión, inscribirse a un curso y completar contenido.
6. Mostrar la rama `main` publicada en GitHub y la rama de trabajo `feature/mi-cambio`.

> El taller de versionamiento es una actividad presencial del curso; el repositorio deja
> preparada la evidencia técnica (ramas, commits, remotos y documentación) para acreditarlo.
