<#
  Evidencia de los casos de prueba del sprint (matriz "CASOS DE PRUEBA").

  Corre ./mvnw test con las clases indicadas y muestra una tabla con cada caso de prueba (CP-xx / HU-xx)
  y su resultado, leyendo los reportes de target/surefire-reports. La tabla tambien se guarda en
  evidencias/<fecha>_casos-de-prueba.txt para adjuntarla al issue o al PR.

  Uso (desde 04_imp/backend):
    powershell -ExecutionPolicy Bypass -File .\evidencia-pruebas.ps1
    powershell -ExecutionPolicy Bypass -File .\evidencia-pruebas.ps1 -Pruebas "AsignacionServiceTest,DisponibilidadServiceTest"

  Las pruebas *IT (PgAsignacionIT, PgAgendaRepositoryIT) necesitan PostgreSQL con application-local.properties;
  sin base se marcan como OMITIDA, no como aprobadas.
#>
param(
    [string]$Pruebas = "AsignacionServiceTest,DisponibilidadServiceTest,CitaServiceTest,PgAsignacionIT,PgAgendaRepositoryIT"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$inicio = Get-Date
Write-Host "==> .\mvnw test -Dtest=$Pruebas" -ForegroundColor Cyan
& .\mvnw.cmd test "-Dtest=$Pruebas"
$codigoMaven = $LASTEXITCODE

$filas = @()
foreach ($clase in $Pruebas.Split(",")) {
    $archivo = Get-ChildItem "target\surefire-reports" -Filter "TEST-*.$($clase.Trim()).xml" -ErrorAction SilentlyContinue |
        Where-Object { $_.LastWriteTime -ge $inicio } | Select-Object -First 1
    if (-not $archivo) { continue }
    [xml]$xml = Get-Content $archivo.FullName -Encoding UTF8
    foreach ($tc in $xml.testsuite.testcase) {
        if ($tc.name -notmatch '^(CP|HU)-\d+') { continue }
        $estado = "APROBADA"
        if ($tc.failure -or $tc.error) { $estado = "FALLIDA" }
        elseif ($tc.skipped) { $estado = "OMITIDA" }
        $filas += [pscustomobject]@{
            Caso      = $Matches[0]
            Resultado = $estado
            Prueba    = $tc.name
            Clase     = ($tc.classname -split '\.')[-1]
            Segundos  = $tc.time
        }
    }
}

$filas = $filas | Sort-Object { $_.Caso.StartsWith("HU") }, { [int](($_.Caso -split '-')[1]) }
$tabla = $filas | Format-Table -AutoSize -Wrap | Out-String -Width 220
$aprobadas = @($filas | Where-Object { $_.Resultado -eq "APROBADA" }).Count

$cabecera = @"
EVIDENCIA DE CASOS DE PRUEBA
Fecha:    $($inicio.ToString("yyyy-MM-dd HH:mm:ss"))
Rama:     $(git rev-parse --abbrev-ref HEAD) ($(git rev-parse --short HEAD))
Comando:  ./mvnw test -Dtest=$Pruebas
Maven:    $(if ($codigoMaven -eq 0) { "BUILD SUCCESS" } else { "BUILD FAILURE (codigo $codigoMaven)" })
Casos:    $aprobadas de $($filas.Count) aprobados
"@

Write-Host ""
Write-Host $cabecera -ForegroundColor Cyan
Write-Host $tabla

New-Item -ItemType Directory -Force "evidencias" | Out-Null
$salida = "evidencias\$($inicio.ToString("yyyy-MM-dd_HHmm"))_casos-de-prueba.txt"
($cabecera + "`r`n" + $tabla) | Out-File $salida -Encoding utf8
Write-Host "Guardado en $salida" -ForegroundColor Green

exit $codigoMaven
