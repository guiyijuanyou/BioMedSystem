param(
    [string]$BaseUrl = "http://localhost:8088",
    [string]$BodyFile = "$PSScriptRoot\mobile-batch-request.json"
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($env:BIOMED_DEVICE_TOKEN)) {
    throw "Set the BIOMED_DEVICE_TOKEN environment variable first."
}

$headers = @{
    "X-Device-Token" = $env:BIOMED_DEVICE_TOKEN
    "X-Request-Id" = "app-$([guid]::NewGuid().ToString('N'))"
}
$body = Get-Content -Raw -Encoding UTF8 $BodyFile

Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/mobile/growth-records/batch" `
    -Headers $headers `
    -ContentType "application/json; charset=utf-8" `
    -Body $body
