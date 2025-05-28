import "pe"

rule Detect_Generic_Malware {
    meta:
        description = "Regla optimizada para detectar malware evitando falsos positivos"
        author = "Juan Camilo"
        date = "2025-05-25"

    strings:
        $malicious_string1 = "malware_signature" nocase
        $malicious_string2 = "Trojan:Win32" nocase
        $malicious_string3 = "ransomware_payload" nocase
        
        // Exclusiones comunes para evitar falsos positivos
        $legit_string1 = "Microsoft Corporation" wide
        $legit_string2 = "Copyright" wide
    
    condition:
        (pe.machine == pe.MACHINE_I386 or pe.machine == pe.MACHINE_AMD64)
        and filesize > 30KB and filesize < 50MB 
        and pe.number_of_sections > 4 
        and not any of ($legit_string*) 
        and any of ($malicious_string*)
}

rule Detect_Ransomware {
    meta:
        description = "Detecta intentos de cifrado y mensajes de rescate"
        author = "Juan Camilo"

    strings:
        $encrypt_ext = ".encrypted" nocase
        $ransom_note = "Your files have been encrypted!" wide
        $payment_request = "Send Bitcoin to" nocase

    condition:
        (pe.machine == pe.MACHINE_I386 or pe.machine == pe.MACHINE_AMD64)
        and any of ($encrypt_ext, $ransom_note, $payment_request)
}

rule Detect_Keylogger {
    meta:
        description = "Detecta actividad de keyloggers en ejecutables"
        author = "Juan Camilo"

    strings:
        $capture_keystrokes = "GetAsyncKeyState" nocase
        $log_keystrokes = "WriteFile log.txt" nocase
        $send_data = "POST http://malicious-site.com" nocase

    condition:
        (pe.machine == pe.MACHINE_I386 or pe.machine == pe.MACHINE_AMD64)
        and any of ($capture_keystrokes, $log_keystrokes, $send_data)
}

rule Detect_Backdoor {
    meta:
        description = "Detecta presencia de backdoors en archivos ejecutables"
        author = "Juan Camilo"

    strings:
        $remote_control = "cmd.exe /c" nocase
        $hidden_access = "net user administrator" nocase
        $auto_execution = "run -hidden" nocase

    condition:
        (pe.machine == pe.MACHINE_I386 or pe.machine == pe.MACHINE_AMD64)
        and filesize > 30KB 
        and any of ($remote_control, $hidden_access, $auto_execution)
}

rule Detect_Malicious_HTML {
    meta:
        description = "Detecta scripts maliciosos en archivos HTML"
        author = "Juan Camilo"

    strings:
        $script_injection = "<script>evilCode()" wide
        $malicious_redirect = "window.location='http://malicious-site.com'" nocase
        $encoded_payload = "eval(atob(" nocase

    condition:
        filesize < 5MB
        and any of ($script_injection, $malicious_redirect, $encoded_payload)
}

rule Detect_Malicious_Txt {
    meta:
        description = "Detecta comandos sospechosos en archivos TXT"
        author = "Juan Camilo"

    strings:
        $ransom_note = "Your files have been encrypted!" wide
        $payment_request = "Send Bitcoin to" nocase
        $malicious_command = "cmd.exe /c" nocase
        $powershell_exec = "powershell -exec bypass" nocase

    condition:
        filesize < 5MB
        and any of ($ransom_note, $payment_request, $malicious_command, $powershell_exec)
}

rule Detect_Malicious_Docs {
    meta:
        description = "Detecta macros peligrosas en documentos de Word y Excel"
        author = "Juan Camilo"

    strings:
        $macro_exec = "AutoOpen" nocase
        $vba_payload = "Sub AutoExec()" nocase
        $suspicious_vba = "CreateObject('WScript.Shell').Run" nocase

    condition:
        filesize < 10MB
        and any of ($macro_exec, $vba_payload, $suspicious_vba)
}

rule Detect_Malicious_PDF {
    meta:
        description = "Detecta intentos de ejecución remota en archivos PDF"
        author = "Juan Camilo"

    strings:
        $pdf_launch = "/Launch" nocase
        $pdf_url_exec = "http://malicious-site.com" nocase
        $pdf_javascript = "/JavaScript" nocase

    condition:
        filesize < 10MB
        and any of ($pdf_launch, $pdf_url_exec, $pdf_javascript)
}
