param(
    [Parameter(Mandatory = $true)]
    [string]$FilePath,
    [string]$BaseUrl = "http://localhost:8088",
    [string]$DownloadFileId = ""
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($env:BIOMED_TOKEN)) {
    throw "Set the BIOMED_TOKEN environment variable first."
}
if (-not (Test-Path -LiteralPath $FilePath -PathType Leaf)) {
    throw "File not found: $FilePath"
}

Write-Host "Uploading file..."
curl.exe --fail-with-body `
    -H "Authorization: Bearer $env:BIOMED_TOKEN" `
    -H "X-Request-Id: file-$([guid]::NewGuid().ToString('N'))" `
    -F "file=@$FilePath" `
    "$BaseUrl/api/files/upload"

if (-not [string]::IsNullOrWhiteSpace($DownloadFileId)) {
    $output = Join-Path (Split-Path -Parent $FilePath) "download-$DownloadFileId.bin"
    Write-Host "Downloading file to $output ..."
    curl.exe --fail-with-body `
        -H "Authorization: Bearer $env:BIOMED_TOKEN" `
        -o $output `
        "$BaseUrl/api/files/$DownloadFileId/download"
}
