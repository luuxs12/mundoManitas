# =====================================================================
# Verificacion local de Mundo Manitas
# Arranca el JAR con el perfil dev, espera a que responda y ejecuta
# pruebas HTTP basicas (paginas, actuator, JWT, roles y descargas).
#
# Nota: se usa System.Net.Http.HttpClient en lugar de
# Invoke-WebRequest -MaximumRedirection 0 porque PowerShell 5.1 lanza
# "Operacion no valida dado el estado actual del objeto" al recibir
# un 302 con redirecciones deshabilitadas.
# =====================================================================
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$jar = Get-ChildItem -Path "target\mundomanitas-*.jar" |
    Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } |
    Select-Object -First 1

if (-not $jar) {
    Write-Output "No se encontro el JAR en target/"
    exit 1
}

$outLog = "target\run-dev.out.log"
$errLog = "target\run-dev.err.log"

Remove-Item $outLog, $errLog -ErrorAction SilentlyContinue

Write-Output "Arrancando $($jar.Name) con perfil dev..."
$proceso = Start-Process -FilePath "java" `
    -ArgumentList "-jar", "`"$($jar.FullName)`"", "--spring.profiles.active=dev" `
    -RedirectStandardOutput $outLog `
    -RedirectStandardError $errLog `
    -PassThru

$listo = $false

for ($intento = 1; $intento -le 180; $intento++) {
    Start-Sleep -Seconds 1

    if ($proceso.HasExited) {
        break
    }

    try {
        $respuesta = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 3
        if ($respuesta.StatusCode -eq 200) {
            $listo = $true
            break
        }
    } catch {
        # El servidor todavia no responde.
    }
}

if (-not $listo) {
    Write-Output "=== La aplicacion no arranco ==="
    Get-Content $outLog -Tail 120 -ErrorAction SilentlyContinue
    Get-Content $errLog -Tail 60 -ErrorAction SilentlyContinue
    if (-not $proceso.HasExited) { Stop-Process -Id $proceso.Id -Force }
    exit 1
}

Write-Output "Aplicacion lista en http://localhost:8080"

# ---------------------------------------------------------------------
# Utilidades
# ---------------------------------------------------------------------
$fallos = 0
$global:tokenJwt = $null

function Probar($nombre, [scriptblock]$accion) {
    try {
        $resultado = & $accion
        Write-Output "OK    $nombre -> $resultado"
    } catch {
        Write-Output "FALLA $nombre -> $($_.Exception.Message)"
        $script:fallos++
    }
}

function Nuevo-Cliente([bool]$seguirRedireccion) {
    $handler = New-Object System.Net.Http.HttpClientHandler
    $handler.AllowAutoRedirect = $seguirRedireccion
    $handler.CookieContainer = New-Object System.Net.CookieContainer

    $cliente = New-Object System.Net.Http.HttpClient($handler)
    $cliente.BaseAddress = New-Object System.Uri("http://localhost:8080/")
    $cliente.Timeout = [TimeSpan]::FromSeconds(60)

    return $cliente
}

function Contenido($respuesta) {
    return $respuesta.Content.ReadAsStringAsync().Result
}

function Csrf($html) {
    $coincidencia = [regex]::Match($html, 'name="_csrf"\s+value="([^"]+)"')
    if (-not $coincidencia.Success) {
        throw "no se encontro el token CSRF"
    }
    return $coincidencia.Groups[1].Value
}

function Iniciar-Sesion([string]$email, [string]$contrasena) {
    $cliente = Nuevo-Cliente $true

    $login = $cliente.GetAsync("login").Result
    if ([int]$login.StatusCode -ne 200) {
        throw "login devolvio $([int]$login.StatusCode)"
    }

    $token = Csrf (Contenido $login)

    $campos = New-Object 'System.Collections.Generic.Dictionary[string,string]'
    $campos.Add("username", $email)
    $campos.Add("password", $contrasena)
    $campos.Add("_csrf", $token)

    $formulario = New-Object System.Net.Http.FormUrlEncodedContent($campos)
    $respuesta = $cliente.PostAsync("login", $formulario).Result

    if ([int]$respuesta.StatusCode -ne 200) {
        throw "autenticacion devolvio $([int]$respuesta.StatusCode)"
    }

    return $cliente
}

# ---------------------------------------------------------------------
# Pruebas
# ---------------------------------------------------------------------

Probar "login responde 200 con el formulario" {
    $cliente = Nuevo-Cliente $true
    $respuesta = $cliente.GetAsync("login").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch 'name="username"') { throw "sin campo username" }
    if ($html -notmatch 'name="password"') { throw "sin campo password" }
    if ($html -notmatch 'name="_csrf"') { throw "sin token CSRF" }
    "200"
}

Probar "actuator/health" {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 15
    if ($r.status -ne "UP") { throw "estado $($r.status)" }
    "UP"
}

Probar "dashboard anonimo redirige al login" {
    $cliente = Nuevo-Cliente $false
    $respuesta = $cliente.GetAsync("dashboard").Result
    $codigo = [int]$respuesta.StatusCode

    if ($codigo -ne 302) { throw "status $codigo" }

    $destino = $respuesta.Headers.Location.ToString()
    if ($destino -notmatch "/login") { throw "redirige a $destino" }
    "302 -> $destino"
}

Probar "API login JWT" {
    $body = '{"email":"alumno@mundomanitas.com","password":"123456"}'
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post `
        -ContentType "application/json" -Body $body -TimeoutSec 30

    if (-not $r.token) { throw "sin token" }
    $global:tokenJwt = $r.token
    "token emitido"
}

Probar "API cursos con JWT" {
    if (-not $global:tokenJwt) { throw "no hay token" }

    $headers = @{ Authorization = "Bearer $global:tokenJwt" }
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/cursos" -Headers $headers -TimeoutSec 30

    if (-not $r.content) { throw "sin contenido" }
    "$($r.content.Count) modulos"
}

Probar "API admin con rol estudiante devuelve 403" {
    if (-not $global:tokenJwt) { throw "no hay token" }

    $headers = @{ Authorization = "Bearer $global:tokenJwt" }
    try {
        Invoke-RestMethod -Uri "http://localhost:8080/api/admin/usuarios" -Headers $headers -TimeoutSec 30 | Out-Null
        throw "no bloqueo el acceso"
    } catch {
        $codigo = $_.Exception.Response.StatusCode.value__
        if ($codigo -ne 403) { throw "status $codigo" }
        "403"
    }
}

Probar "login web y acceso al catalogo de cursos" {
    $cliente = Iniciar-Sesion "alumno@mundomanitas.com" "123456"

    $respuesta = $cliente.GetAsync("cursos").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch "Fundamentos de Java") { throw "catalogo no visible" }
    "sesion y catalogo OK"
}

Probar "estudiante no descarga el reporte general" {
    $cliente = Iniciar-Sesion "alumno@mundomanitas.com" "123456"

    $respuesta = $cliente.GetAsync("reportes/progreso.xlsx").Result
    $codigo = [int]$respuesta.StatusCode

    if ($codigo -ne 403) { throw "status $codigo" }
    "403"
}

Probar "docente descarga el reporte Excel" {
    $cliente = Iniciar-Sesion "docente@mundomanitas.com" "123456"

    $respuesta = $cliente.GetAsync("reportes/progreso.xlsx").Result
    $codigo = [int]$respuesta.StatusCode

    if ($codigo -ne 200) { throw "status $codigo" }

    $bytes = $respuesta.Content.ReadAsByteArrayAsync().Result
    if ($bytes.Length -lt 100) { throw "archivo vacio" }
    if ($bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) { throw "no es un XLSX valido" }
    "$($bytes.Length) bytes"
}

Probar "estudiante descarga su certificado Word" {
    $cliente = Iniciar-Sesion "alumno@mundomanitas.com" "123456"

    $respuesta = $cliente.GetAsync("reportes/certificado/1.docx").Result
    $codigo = [int]$respuesta.StatusCode

    if ($codigo -ne 200) { throw "status $codigo" }

    $bytes = $respuesta.Content.ReadAsByteArrayAsync().Result
    if ($bytes.Length -lt 100) { throw "archivo vacio" }
    if ($bytes[0] -ne 0x50 -or $bytes[1] -ne 0x4B) { throw "no es un DOCX valido" }
    "$($bytes.Length) bytes"
}

Probar "registro publico responde 200" {
    $cliente = Nuevo-Cliente $true
    $respuesta = $cliente.GetAsync("registro").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch "Crear cuenta de estudiante") { throw "formulario no visible" }
    if ($html -notmatch "Postular como docente") { throw "sin opcion de postulacion docente" }
    "200"
}

Probar "formulario de estudiante responde 200" {
    $cliente = Nuevo-Cliente $true
    $respuesta = $cliente.GetAsync("registro/estudiante").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch 'name="confirmarContrasena"') { throw "sin confirmacion de contrasena" }
    "200"
}

Probar "postulacion docente publica responde 200" {
    $cliente = Nuevo-Cliente $true
    $respuesta = $cliente.GetAsync("registro/docente").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch 'name="nivelLsp"') { throw "sin campo nivel de LSP" }
    if ($html -notmatch 'name="portafolio"') { throw "sin campo portafolio" }
    if ($html -notmatch 'name="aniosExperiencia"') { throw "sin campo de experiencia en TI" }
    "200"
}

Probar "administrador revisa las postulaciones docentes" {
    $cliente = Iniciar-Sesion "admin@mundomanitas.com" "123456"
    $respuesta = $cliente.GetAsync("admin/postulaciones").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch "Postulaciones docentes") { throw "pagina no visible" }
    if ($html -notmatch "Ver portafolio") { throw "sin postulacion demo pendiente" }
    "200"
}

Probar "docente no revisa postulaciones docentes" {
    $cliente = Iniciar-Sesion "docente@mundomanitas.com" "123456"
    $respuesta = $cliente.GetAsync("admin/postulaciones").Result
    $codigo = [int]$respuesta.StatusCode

    if ($codigo -ne 403) { throw "status $codigo" }
    "403"
}

Probar "login enlaza al registro publico" {
    $cliente = Nuevo-Cliente $true
    $respuesta = $cliente.GetAsync("login").Result
    $html = Contenido $respuesta

    if ($html -notmatch 'href="/registro"') { throw "sin enlace a registro" }
    "enlace presente"
}

Probar "estudiante ve la edicion de su perfil" {
    $cliente = Iniciar-Sesion "alumno@mundomanitas.com" "123456"
    $respuesta = $cliente.GetAsync("perfil").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch "Editar mis datos") { throw "formulario no visible" }
    "200"
}

Probar "docente accede a su panel de cursos" {
    $cliente = Iniciar-Sesion "docente@mundomanitas.com" "123456"
    $respuesta = $cliente.GetAsync("docente/cursos").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch "Cursos a tu cargo") { throw "panel no visible" }
    "200"
}

Probar "docente no accede al panel global" {
    $cliente = Iniciar-Sesion "docente@mundomanitas.com" "123456"
    $respuesta = $cliente.GetAsync("admin").Result
    $codigo = [int]$respuesta.StatusCode

    if ($codigo -ne 403) { throw "status $codigo" }
    "403"
}

Probar "administrador accede al panel general" {
    $cliente = Iniciar-Sesion "admin@mundomanitas.com" "123456"
    $respuesta = $cliente.GetAsync("admin").Result
    if ([int]$respuesta.StatusCode -ne 200) { throw "status $([int]$respuesta.StatusCode)" }

    $html = Contenido $respuesta
    if ($html -notmatch "Estado de la plataforma") { throw "panel no visible" }
    "200"
}

Write-Output ""
Write-Output "=== Ultimas lineas del log ==="
Get-Content $outLog -Tail 20 -ErrorAction SilentlyContinue

Write-Output ""
Write-Output "=== Errores (si hay) ==="
Get-Content $errLog -Tail 20 -ErrorAction SilentlyContinue

if (-not $proceso.HasExited) { Stop-Process -Id $proceso.Id -Force }

Write-Output ""
if ($fallos -gt 0) {
    Write-Output "Verificacion con $fallos fallo(s)."
    exit 1
}

Write-Output "Verificacion completa sin fallos."
