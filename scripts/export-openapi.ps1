$ErrorActionPreference = 'Stop'

param(
  [string]$BaseUrl = 'http://localhost:8123/api',
  [string]$Group = 'default',
  [string]$OutFile = ''
)

if ([string]::IsNullOrWhiteSpace($OutFile)) {
  $OutFile = Join-Path $PSScriptRoot "..\docs\openapi\openapi.json"
}

$apiDocsUrl = "$BaseUrl/v3/api-docs/$Group"

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutFile) | Out-Null

Write-Host "Fetching OpenAPI: $apiDocsUrl"
Write-Host "Writing to: $OutFile"

$resp = Invoke-WebRequest -Uri $apiDocsUrl -Headers @{ Accept = 'application/json' }
$resp.Content | Out-File -FilePath $OutFile -Encoding utf8NoBOM

Write-Host "Done."
