<#
.SYNOPSIS
    Registers a Windows Scheduled Task that runs watch-remote-git.ps1 hourly.

.EXAMPLE
    .\install-remote-git-watch-task.ps1 -RemoteUrl "https://github.com/org/repo.git"
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RemoteUrl,

    [string]$Branch = "",
    [string]$TaskName = "Watch Remote Git Commits"
)

$watchScript = Join-Path $PSScriptRoot "watch-remote-git.ps1"
$arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$watchScript`" -RemoteUrl `"$RemoteUrl`""
if (-not [string]::IsNullOrWhiteSpace($Branch)) {
    $arguments += " -Branch `"$Branch`""
}

$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument $arguments
$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date).Date.AddMinutes(1) -RepetitionInterval (New-TimeSpan -Hours 1) -RepetitionDuration (New-TimeSpan -Days 3650)
Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Description "Fetches remote Git commits every hour." -Force | Out-Null

Write-Host "Scheduled task '$TaskName' has been registered."
