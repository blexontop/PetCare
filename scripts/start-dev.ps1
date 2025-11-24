# start-dev.ps1
# Loads .env variables into the current process and runs the Spring Boot app with profile 'dev'.
# Usage: .\scripts\start-dev.ps1

$envFile = Join-Path $PSScriptRoot "..\.env"
if (Test-Path $envFile) {
    Write-Output "Loading environment variables from $envFile"
    Get-Content $envFile | ForEach-Object {
        $_ = $_.Trim()
        if (-not $_ -or $_ -match '^#') { return }
        $parts = $_ -split '=', 2
        if ($parts.Length -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            Write-Output "Setting env $name"
            $env:$name = $value
        }
    }
} else {
    Write-Output ".env not found at $envFile"
}

# Kill any java process listening on 8080 to avoid conflicts
try {
    $listeners = (cmd /c "netstat -aon | findstr :8080") -split "\r?\n" | ForEach-Object { $_.Trim() } | Where-Object { $_ -match 'LISTENING' }
    foreach ($l in $listeners) {
        if ($l -match '\b(\d+)\$') { $pid = $matches[1]; if ($pid) { Write-Output "Killing process PID $pid"; taskkill /PID $pid /F | Out-Null } }
    }
} catch {
    Write-Output "No process to kill or error while checking listeners: $_"
}

# Run Maven with dev profile
$mvnc = Join-Path $env:USERPROFILE "tools\apache-maven-3.9.6\bin\mvn.cmd"
if (-not (Test-Path $mvnc)) { Write-Error "Maven not found at $mvnc"; exit 1 }

Write-Output "Starting Spring Boot (profile=dev)..."
& cmd /c "`"$mvnc`" spring-boot:run -Dspring-boot.run.profiles=dev"
