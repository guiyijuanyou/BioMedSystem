param(
    [string]$BaseUrl = "http://localhost:8088",
    [string]$ClientCode = "CQUTCM-SCHOOL",
    [string]$ClientName = "Campus Data Center"
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($env:BIOMED_ADMIN_TOKEN)) {
    throw "Set the BIOMED_ADMIN_TOKEN environment variable first."
}

$headers = @{ Authorization = "Bearer $env:BIOMED_ADMIN_TOKEN" }
$body = @{
    clientCode = $ClientCode
    clientName = $ClientName
    allowedIps = @("127.0.0.1")
} | ConvertTo-Json

$result = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/integration-clients" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $body

$result
Write-Warning "The secret is shown once. Store it in an approved secret manager now."
