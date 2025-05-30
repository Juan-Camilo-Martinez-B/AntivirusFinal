package com.antivirus.Antivirus.config;

import com.antivirus.Antivirus.service.ScanStrategy;
import com.antivirus.Antivirus.service.YARAScanStrategy;

public class ScanStrategyFactory {
    public static ScanStrategy getStrategy(String fileType) {
        return new YARAScanStrategy(); // Usamos YARA para analizar todos los archivos
    }
}
