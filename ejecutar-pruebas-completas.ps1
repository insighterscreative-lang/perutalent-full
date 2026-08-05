[CmdletBinding()]
param(
    [switch]$ReinstalarFrontend
)

[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$ErrorActionPreference = 'Continue'
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ResultsDir = Join-Path $ProjectRoot 'resultados-pruebas'
$Timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$SummaryPath = Join-Path $ResultsDir "resumen-$Timestamp.txt"

New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null

$results = [System.Collections.Generic.List[object]]::new()

function Invoke-ValidationStep {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [Parameter(Mandatory)] [scriptblock]$Action
    )

    $safeName = ($Name -replace '[^a-zA-Z0-9_-]', '-').ToLowerInvariant()
    $logPath = Join-Path $ResultsDir "$Timestamp-$safeName.log"

    Write-Host ""
    Write-Host ('=' * 72) -ForegroundColor Cyan
    Write-Host $Name -ForegroundColor Cyan
    Write-Host ('=' * 72) -ForegroundColor Cyan

    $start = Get-Date
    $exitCode = 1

    Push-Location $WorkingDirectory
    try {
        & $Action 2>&1 | Tee-Object -FilePath $logPath
        if ($null -eq $LASTEXITCODE) {
            $exitCode = 0
        } else {
            $exitCode = $LASTEXITCODE
        }
    }
    catch {
        $_ | Out-String | Tee-Object -FilePath $logPath -Append | Write-Host
        $exitCode = 1
    }
    finally {
        Pop-Location
    }

    $duration = [math]::Round(((Get-Date) - $start).TotalSeconds, 2)
    $status = if ($exitCode -eq 0) { 'CORRECTO' } else { 'ERROR' }

    $results.Add([pscustomobject]@{
        Paso = $Name
        Estado = $status
        Segundos = $duration
        Log = $logPath
    })

    if ($exitCode -eq 0) {
        Write-Host "[$status] $Name" -ForegroundColor Green
    } else {
        Write-Host "[$status] $Name. Revisa: $logPath" -ForegroundColor Red
    }
}

function Assert-CommandAvailable {
    param([string]$CommandName, [string]$FriendlyName)

    if (-not (Get-Command $CommandName -ErrorAction SilentlyContinue)) {
        throw "No se encontró $FriendlyName ($CommandName) en PATH."
    }
}

Write-Host "PeruTalent - Suite completa de validación del MVP" -ForegroundColor Yellow
Write-Host "Proyecto: $ProjectRoot"
Write-Host "Resultados: $ResultsDir"

Invoke-ValidationStep -Name 'Backend: pruebas, seguridad y empaquetado' `
    -WorkingDirectory (Join-Path $ProjectRoot 'backend') `
    -Action {
        if (Get-Command mvn -ErrorAction SilentlyContinue) {
            mvn clean verify
        }
        elseif (Test-Path '.\mvnw.cmd') {
            & '.\mvnw.cmd' clean verify
        }
        else {
            throw 'No se encontró Maven ni backend\mvnw.cmd.'
        }
    }

Invoke-ValidationStep -Name 'Frontend: dependencias disponibles' `
    -WorkingDirectory (Join-Path $ProjectRoot 'frontend') `
    -Action {
        Assert-CommandAvailable 'node' 'Node.js'
        Assert-CommandAvailable 'npm' 'npm'

        if ($ReinstalarFrontend -or -not (Test-Path '.\node_modules')) {
            npm ci
        }
        else {
            Write-Host 'node_modules ya existe; se omite npm ci.'
            node --version
            npm --version
        }
    }

Invoke-ValidationStep -Name 'Frontend: pruebas unitarias y contratos HTTP' `
    -WorkingDirectory (Join-Path $ProjectRoot 'frontend') `
    -Action {
        $frontendSource = (Get-Location).Path
        $rutaConCaracteresProblematicos = $frontendSource -match '[\(\)\[\]]'

        if (-not $rutaConCaracteresProblematicos) {
            npm run test:ci
        }
        else {
            Write-Host 'La ruta contiene parentesis o corchetes. Angular/Vitest puede no descubrir pruebas en Windows.'
            Write-Host 'Se ejecutaran las pruebas desde una copia temporal con una ruta simple.'

            $frontendTemporal = Join-Path $env:TEMP 'perutalent-frontend-tests'
            if (Test-Path $frontendTemporal) {
                Remove-Item -Path $frontendTemporal -Recurse -Force
            }
            New-Item -ItemType Directory -Force -Path $frontendTemporal | Out-Null

            $excluidos = @('node_modules', 'dist', '.angular')
            Get-ChildItem -Path $frontendSource -Force |
                Where-Object { $excluidos -notcontains $_.Name } |
                ForEach-Object {
                    Copy-Item -Path $_.FullName -Destination $frontendTemporal -Recurse -Force
                }

            Push-Location $frontendTemporal
            try {
                npm ci
                if ($LASTEXITCODE -ne 0) {
                    throw "npm ci fallo en la copia temporal con codigo $LASTEXITCODE."
                }

                npm run test:ci
            }
            finally {
                Pop-Location
                if (Test-Path $frontendTemporal) {
                    Remove-Item -Path $frontendTemporal -Recurse -Force -ErrorAction SilentlyContinue
                }
            }
        }
    }

Invoke-ValidationStep -Name 'Frontend: build de producción' `
    -WorkingDirectory (Join-Path $ProjectRoot 'frontend') `
    -Action {
        npm run build
    }

$allOk = -not ($results | Where-Object { $_.Estado -ne 'CORRECTO' })

$summaryLines = @()
$summaryLines += '========================================================================'
$summaryLines += 'RESULTADO GENERAL DE PERUTALENT'
$summaryLines += '========================================================================'
$summaryLines += "Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$summaryLines += ''
foreach ($result in $results) {
    $summaryLines += ('{0,-48} {1,-10} {2,8}s' -f $result.Paso, $result.Estado, $result.Segundos)
}
$summaryLines += ''
$summaryLines += if ($allOk) {
    'RESULTADO FINAL: TODAS LAS VALIDACIONES PASARON'
} else {
    'RESULTADO FINAL: EXISTEN VALIDACIONES CON ERROR'
}
$summaryLines += ''
$summaryLines += 'Los logs detallados están en la carpeta resultados-pruebas.'
$summaryLines | Set-Content -Path $SummaryPath -Encoding UTF8

Write-Host ""
$summaryLines | ForEach-Object { Write-Host $_ }
Write-Host "Resumen guardado en: $SummaryPath"

if (-not $allOk) {
    exit 1
}

exit 0
