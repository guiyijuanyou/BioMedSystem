param(
    [ValidateSet("dev", "prod")]
    [string]$Mode = "dev"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$Mvnw = "$ProjectRoot\backend\mvnw.cmd"

# Build frontend
Write-Host "Building frontend..." -ForegroundColor Green
Push-Location "$ProjectRoot\frontend"
npm install
if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
npm run build
if ($LASTEXITCODE -ne 0) { throw "npm run build failed" }
Pop-Location

# Build backend
if (-not (Test-Path $Mvnw)) {
    throw "Maven Wrapper not found at $Mvnw. Run: cd backend && mvn wrapper:wrapper -Dmaven=3.9.9"
}

Write-Host "Building backend (mode: $Mode)..." -ForegroundColor Green
Push-Location "$ProjectRoot\backend"
$profileFlag = if ($Mode -eq "prod") { "-Pprod" } else { "-Pdev" }
& $Mvnw $profileFlag "-DskipTests" package
if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }
Pop-Location

# Start application
$jar = "$ProjectRoot\backend\target\biomed-digital-system-1.0.0.jar"
if (-not (Test-Path $jar)) {
    throw "JAR not found at $jar"
}

Write-Host "Starting application (mode: $Mode)..." -ForegroundColor Green
$env:SPRING_PROFILES_ACTIVE = $Mode
java -jar $jar
