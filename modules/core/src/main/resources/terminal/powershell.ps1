# if ($host.Name -eq 'ConsoleHost')
# {
    Import-Module PSReadLine
# }

Set-PSReadlineOption -ExtraPromptLineCount 1

function Get-Environmental-Variable-Or-Default($envVarName, $default) {
    $envVar = [System.Environment]::GetEnvironmentVariable($envVarName)
    if ($envVar -eq $null) {
        return $default
    } else {
        return $envVar
    }
}

$global:prompt = "LOADING\r\n>"
$global:showPrompt = $true
$global:promptTimer = Get-Date
$global:previousLocation = ""
$global:previousEnvs = @()
$global:wormholeCommand = Get-Environmental-Variable-Or-Default "WORMHOLE_COMMAND" "wormhole"
$global:eventsDir = Get-Environmental-Variable-Or-Default "PROMPT_EVENTS_DIR" "CONSOLE_EVENTS_DIRECTORY"
$global:promptDir = Get-Environmental-Variable-Or-Default "PROMPT_PROMPT_DIR" "CONSOLE_PROMPT_DIRECTORY"

$null = Register-EngineEvent -SourceIdentifier PowerShell.OnIdle -Action {
    "Don't allow powershell to go idle"
    "PowerShell idle at {0}" -f (Get-Date) | out-null
}

function global:Update-Prompt-All {
    $global:previousLocation = ""
    $global:previousEnvs = @()
    Update-Prompt-Directory
    Update-Prompt-Environment
}

function global:Save-Prompt-Event($promptEvent) {
    $time = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $OutputFile = "$global:eventsDir\$time.json"
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

$null = Set-PSReadLineKeyHandler -Chord Enter -ScriptBlock {
    $global:promptTimer = (Get-Date).AddSeconds(1)
    $backup = $global:prompt
    $global:prompt = "> "
    [Microsoft.PowerShell.PSConsoleReadLine]::InvokePrompt()
    $global:prompt = $backup
    [Microsoft.PowerShell.PSConsoleReadLine]::AcceptLine()
    $global:promptTimer = (Get-Date).AddMilliseconds(10)
}

$null = Set-PSReadLineKeyHandler -Chord Ctrl+c -ScriptBlock {
    [Microsoft.PowerShell.PSConsoleReadLine]::DeleteLine()
}

$file = $global:promptDir
$filter = "prompt.txt"
$Watcher = New-Object IO.FileSystemWatcher $file, $filter -Property @{
    IncludeSubdirectories = $false
    NotifyFilter = [IO.NotifyFilters]'LastWrite'
}
$null = Register-ObjectEvent $Watcher -EventName Changed -SourceIdentifier PromptFileCreated4 -Action {
   try {
    $path = $Event.SourceEventArgs.FullPath
    $text = [System.IO.File]::ReadAllText($path);
    $global:prompt = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($text))
    if ((Get-Date) -ge $global:promptTimer) {

        # https://github.com/kelleyma49/PSFzf/issues/71#issuecomment-961148891
    	$previousOutputEncoding = [Console]::OutputEncoding
    	[Console]::OutputEncoding = [Text.Encoding]::UTF8

    	try {
    		[Microsoft.PowerShell.PSConsoleReadLine]::InvokePrompt()
    	} finally {
    		[Console]::OutputEncoding = $previousOutputEncoding
    	}
    }
   } catch {
       Write-Host "An error occurred:"
       Write-Host $_
   }
}

if ($global:wormholeCommand -ne "none") {
    Start-Process -FilePath "$global:wormholeCommand" -ArgumentList "start", "-t", "Powershell", "--console_events_path", "$global:eventsDir", "--console_prompt_path", "$global:promptDir"
}