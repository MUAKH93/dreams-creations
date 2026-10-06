# Creates a standalone shop project from feature/shop-v1 (git worktree).
# Run from dreams-creations repo root.

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot
$ShopPath = Join-Path (Split-Path -Parent $RepoRoot) "Projects\dreams-creations-shop"

Write-Host "Dreams Creations - bootstrap standalone shop project"
Write-Host "Target: $ShopPath"

Set-Location $RepoRoot

$branch = git branch --list feature/shop-v1
if (-not $branch) {
    Write-Host "Fetching feature/shop-v1..."
    git fetch origin feature/shop-v1 2>$null
}

if (Test-Path $ShopPath) {
    Write-Host "Shop project folder already exists: $ShopPath"
    Write-Host "Remove it first or open that folder directly."
    exit 1
}

$projectsDir = Split-Path $ShopPath -Parent
if (-not (Test-Path $projectsDir)) {
    New-Item -ItemType Directory -Path $projectsDir | Out-Null
}

git worktree add $ShopPath feature/shop-v1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to create worktree. Ensure feature/shop-v1 exists locally or on origin."
    exit 1
}

$shopProps = Join-Path $ShopPath "backend\src\main\resources\application.properties"
$shopPropsExample = Join-Path $ShopPath "backend\src\main\resources\application.properties.example"
if (-not (Test-Path $shopProps) -and (Test-Path $shopPropsExample)) {
    Copy-Item $shopPropsExample $shopProps
}
if (Test-Path $shopProps) {
    $content = Get-Content $shopProps -Raw
    if ($content -match 'server\.port=\d+') {
        $content = $content -replace 'server\.port=\d+', 'server.port=8082'
    } else {
        $content += "`nserver.port=8082`n"
    }
    if ($content -notmatch 'app\.erp\.url=') {
        $content += "`napp.erp.url=http://localhost:8080`napp.erp.integration.api-key=change-me-shop-integration-key`n"
    }
    Set-Content -Path $shopProps -Value ($content.TrimEnd() + "`n")
}

$viteConfig = Join-Path $ShopPath "frontend\vite.config.js"
if (Test-Path $viteConfig) {
    $vite = Get-Content $viteConfig -Raw
    if ($vite -match 'port:\s*\d+') {
        $vite = $vite -replace 'port:\s*\d+', 'port: 3002'
    } else {
        $vite = $vite + "`n// Added by bootstrap-shop-project.ps1`nexport default { server: { port: 3002 } }`n"
    }
    Set-Content -Path $viteConfig -Value ($vite.TrimEnd() + "`n")
}

$shopReadme = Join-Path $ShopPath "SHOP-README.md"
$readmeLines = @(
    '# Dreams Creations Online Shop',
    '',
    'Standalone e-commerce app (extracted from ERP shop module).',
    '',
    '## Dev ports',
    '',
    '- Frontend: 3002',
    '- Backend: 8082',
    '',
    '## ERP integration',
    '',
    'Shop orders sync to the main ERP for inventory and finance.',
    '',
    '- ERP URL: http://localhost:8080',
    '- Endpoint: POST /api/integration/shop/orders/sync',
    '- Header: X-Shop-Integration-Key (must match ERP app.shop.integration.api-key)',
    '',
    'See parent repo docs/MODULE-ARCHITECTURE.md.',
    '',
    '## Run',
    '',
    'Backend: cd backend; .\mvnw.cmd spring-boot:run',
    'Frontend: cd frontend; npm install; npm run dev',
    '',
    'Storefront: http://localhost:3002/store'
)
Set-Content -Path $shopReadme -Value ($readmeLines -join "`n")

Write-Host ""
Write-Host "Done. Open: $ShopPath"
Write-Host "Read SHOP-README.md and docs/MODULE-ARCHITECTURE.md in the ERP repo."
