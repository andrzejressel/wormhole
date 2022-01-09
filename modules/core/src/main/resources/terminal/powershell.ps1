if ($host.Name -eq 'ConsoleHost')
{
    Import-Module PSReadLine
}

Set-PSReadlineOption -ExtraPromptLineCount 1

$global:prompt = "TEST1"
$global:showPrompt = $true
$global:promptTimer = Get-Date
$global:previousLocation = ""
$global:previousEnvs = @()

Register-EngineEvent -SourceIdentifier PowerShell.OnIdle -Action {
    "Don't allow powershell to go idle"
    "PowerShell idle at {0}" -f (Get-Date) |
    Out-File -FilePath $null -Append
}

function global:Update-Prompt-All {
    $global:previousLocation = ""
    $global:previousEnvs = @()
    Update-Prompt-Directory
    Update-Prompt-Environment
}

function global:Save-Prompt-Event($promptEvent) {
    $time = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $OutputFile = "TEMP_DIR\$time.json"
    $Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding($False)
    [System.IO.File]::WriteAllLines($OutputFile, $promptEvent, $Utf8NoBomEncoding)
}

function global:Update-Prompt-Directory {
    $currentDirectory = (Get-Location).Path
    if (($currentDirectory) -ne ($global:previousLocation)) {
        $global:previousLocation = $currentDirectory
        $json = @{
            "type" = "change_dir"
            "dir" = $currentDirectory
        } | ConvertTo-Json
        global:Save-Prompt-Event($json)
#         $json | Out-File -encoding utf8 -FilePath ("TEMP_DIR\$time.json") -Append
    }
}

function global:Update-Prompt-Environment {
    $currentEnv = (Get-Childitem -Path Env:*)
    if (Compare-Object -ReferenceObject $global:previousEnvs -DifferenceObject $currentEnv) {
        # Envs changed
        $global:previousEnvs = $currentEnv
        $envs = @{}
        foreach ($env in $currentEnv)  {
            $envs.add($env.Key, $env.Value)
        }
        $json = @{
            "type" = "set_environment"
            "env" = $envs
        } | ConvertTo-Json
        global:Save-Prompt-Event($json)
        # $json | Out-File -encoding utf8 -FilePath ("TEMP_DIR\$time.json") -Append
    }
}

function global:prompt {
    Update-Prompt-Directory
    Update-Prompt-Environment
    $global:prompt
}

# TODO: Add support for Ctrl-C
Set-PSReadLineKeyHandler -Chord Enter -ScriptBlock {
    $global:promptTimer = (Get-Date).AddSeconds(1)
    $backup = $global:prompt
    $global:prompt = "> "
    [Microsoft.PowerShell.PSConsoleReadLine]::InvokePrompt()
    $global:prompt = $backup
    [Microsoft.PowerShell.PSConsoleReadLine]::AcceptLine()
    $global:promptTimer = (Get-Date).AddMilliseconds(10)
}

$file = "TEMP_FILE_PARENT"
$filter = "TEMP_FILE_NAME"
$Watcher = New-Object IO.FileSystemWatcher $file, $filter -Property @{
    IncludeSubdirectories = $false
    NotifyFilter = [IO.NotifyFilters]'LastWrite'
}
Register-ObjectEvent $Watcher -EventName Changed -SourceIdentifier PromptFileCreated -Action {
   try {
    $path = $Event.SourceEventArgs.FullPath
    $text = [System.IO.File]::ReadAllText($path);
    $global:prompt = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($text))
    if ((Get-Date) -ge $global:promptTimer) {
        [Microsoft.PowerShell.PSConsoleReadLine]::InvokePrompt()
    }
   } catch {
    #    Write-Host "An error occurred:"
    #    Write-Host $_
   }
}
