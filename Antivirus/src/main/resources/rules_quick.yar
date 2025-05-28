import "pe"

rule Quick_Minimal_Scan {
    meta:
        description = "Regla única optimizada para detección rápida"
        author = "Juan Camilo"

    strings:
        $malware = "malware_signature" nocase
        $ransom = "Your files have been encrypted!" wide
        $backdoor = "cmd.exe /c" nocase

    condition:
        filesize > 500KB and any of ($malware, $ransom, $backdoor)
}
