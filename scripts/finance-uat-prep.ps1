# Finance UAT prep — run from repo root before manual UAT (docs/finance-uat-checklist.md)
$ErrorActionPreference = "Continue"
$root = Split-Path $PSScriptRoot -Parent
if (-not (Test-Path (Join-Path $root "backend\pom.xml"))) {
    Write-Host "Run from dreams-creations repo (scripts folder)." -ForegroundColor Red
    exit 1
}

Write-Host "`n=== Finance UAT prep ===" -ForegroundColor Cyan
Write-Host "Repo: $root`n"

$checks = @()

function Test-Check($label, $ok, $hint) {
    $icon = if ($ok) { "[OK]" } else { "[!!]" }
    $color = if ($ok) { "Green" } else { "Yellow" }
    Write-Host "$icon $label" -ForegroundColor $color
    if (-not $ok -and $hint) { Write-Host "    -> $hint" -ForegroundColor DarkYellow }
    $script:checks += [pscustomobject]@{ Label = $label; Ok = $ok }
}

# Git branch
Push-Location $root
$branch = (git branch --show-current 2>$null)
Test-Check "Branch is feature/finance-v2 (current: $branch)" ($branch -eq "feature/finance-v2") "git checkout feature/finance-v2"

# SQL scripts
$db = Join-Path $root "backend\src\main\resources\db"
foreach ($sql in @("add-finance-module.sql", "add-finance-payables.sql", "add-finance-bank.sql")) {
    Test-Check "SQL file exists: $sql" (Test-Path (Join-Path $db $sql)) "Missing migration script"
}

# Backend config
$props = Join-Path $root "backend\src\main\resources\application.properties"
$propsOk = $false
if (Test-Path $props) {
    $text = Get-Content $props -Raw
    $propsOk = $text -match "modules\.finance\.enabled\s*=\s*true"
    Test-Check "modules.finance.enabled=true" $propsOk "Copy from application-finance.properties.example"
    Test-Check "modules.finance.auto-post-ar=true" ($text -match "modules\.finance\.auto-post-ar\s*=\s*true") "Enable for UAT AR tests"
    Test-Check "modules.finance.auto-post-inventory=true" ($text -match "modules\.finance\.auto-post-inventory\s*=\s*true") "Enable for UAT COGS tests"
} else {
    Test-Check "application.properties exists" $false "Copy application.properties.example"
}

# Frontend env
$envFile = Join-Path $root "frontend\.env"
if (Test-Path $envFile) {
    $envText = Get-Content $envFile -Raw
    Test-Check "VITE_FINANCE_MODULE_ENABLED=true" ($envText -match "VITE_FINANCE_MODULE_ENABLED=true") "Set in frontend/.env"
} else {
    Test-Check "frontend/.env exists" $false "Copy from .env.example if present"
}

# Compile
Write-Host "`nCompiling backend..." -ForegroundColor Cyan
Push-Location (Join-Path $root "backend")
& .\mvnw.cmd -q compile -DskipTests 2>&1 | Out-Null
$compileOk = $LASTEXITCODE -eq 0
Pop-Location
Test-Check "Backend compiles" $compileOk "Fix compile errors before UAT"

Pop-Location

$failed = @($checks | Where-Object { -not $_.Ok }).Count
Write-Host "`nSummary: $($checks.Count - $failed)/$($checks.Count) checks passed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
Write-Host "Next: run manual UAT using docs/finance-uat-checklist.md`n" -ForegroundColor Cyan
exit $(if ($failed -eq 0) { 0 } else { 1 })
