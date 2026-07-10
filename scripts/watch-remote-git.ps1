<#
.SYNOPSIS
    Fetch new commits from a remote Git repository and append them to a log.

.EXAMPLE
    .\watch-remote-git.ps1 -RemoteUrl "https://github.com/org/repo.git"
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RemoteUrl,

    # Leave empty to track the remote repository's default branch.
    [string]$Branch = "",

    [string]$DataDirectory = (Join-Path $PSScriptRoot "remote-git-data")
)

$ErrorActionPreference = "Stop"

function Write-MonitorLog {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp  $Message" | Add-Content -LiteralPath $logFile -Encoding utf8
}

New-Item -ItemType Directory -Force -Path $DataDirectory | Out-Null
$logFile = Join-Path $DataDirectory "commits.log"
$repositoryPath = Join-Path $DataDirectory "repository.git"
$stateFile = Join-Path $DataDirectory "last-commit.txt"

try {
    $git = Get-Command git -ErrorAction Stop
} catch {
    Write-MonitorLog "ERROR: Git was not found. Install Git for Windows and ensure 'git' is in PATH."
    exit 1
}

try {
    if (-not (Test-Path -LiteralPath $repositoryPath)) {
        & $git.Source init --bare --quiet $repositoryPath
        & $git.Source -C $repositoryPath remote add origin $RemoteUrl
        Write-MonitorLog "Initialized local cache for $RemoteUrl."
    }

    if ([string]::IsNullOrWhiteSpace($Branch)) {
        $head = & $git.Source ls-remote --symref origin HEAD | Select-String '^ref:\s+refs/heads/(.+)\s+HEAD$' | Select-Object -First 1
        if ($null -eq $head) {
            throw "Could not determine the remote's default branch. Specify -Branch explicitly."
        }
        $Branch = $head.Matches[0].Groups[1].Value
    }

    & $git.Source -C $repositoryPath fetch --quiet origin "refs/heads/$Branch`:`refs/remotes/origin/$Branch"
    $currentCommit = (& $git.Source -C $repositoryPath rev-parse "refs/remotes/origin/$Branch").Trim()
    $previousCommit = if (Test-Path -LiteralPath $stateFile) {
        (Get-Content -LiteralPath $stateFile -Raw).Trim()
    } else {
        ""
    }

    if ([string]::IsNullOrWhiteSpace($previousCommit)) {
        Write-MonitorLog "Tracking $Branch from $currentCommit (initial run; no historical commits emitted)."
    } elseif ($previousCommit -eq $currentCommit) {
        Write-MonitorLog "No new commits on $Branch."
    } elseif (& $git.Source -C $repositoryPath merge-base --is-ancestor $previousCommit $currentCommit) {
        $commits = & $git.Source -C $repositoryPath log --reverse --pretty=format:'%H|%an|%ad|%s' --date=iso-strict "$previousCommit..$currentCommit"
        foreach ($commit in $commits) {
            Write-MonitorLog "NEW [$Branch] $commit"
        }
    } else {
        Write-MonitorLog "WARNING: $Branch history was rewritten or the previous commit is unavailable. Now at $currentCommit."
    }

    Set-Content -LiteralPath $stateFile -Value $currentCommit -NoNewline -Encoding utf8
} catch {
    Write-MonitorLog "ERROR: $($_.Exception.Message)"
    exit 1
}
