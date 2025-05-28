package com.antivirus.Antivirus.service;

import java.io.File;

public class TargetedScanStrategy implements ScanStrategy {
    private final File targetFile;

    public TargetedScanStrategy(File file) {
        this.targetFile = file;
    }

    @Override
    public String scan(File file) {
        if (!targetFile.exists() || !targetFile.isFile()) {
            return "⚠ Archivo no válido para escaneo: " + targetFile.getAbsolutePath();
        }

        YARAScanStrategy yaraScanner = new YARAScanStrategy();
        String yaraResult = yaraScanner.scan(targetFile);

        return "🔍 Escaneo específico en " + targetFile.getName() + ": " + yaraResult;
    }
}
