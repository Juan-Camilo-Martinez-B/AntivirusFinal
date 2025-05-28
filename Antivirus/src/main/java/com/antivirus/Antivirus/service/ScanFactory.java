package com.antivirus.Antivirus.service;

import java.io.File;

public class ScanFactory {
    public static ScanStrategy getScanStrategy(String type, String filePath) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("⚠ Estrategia de escaneo no especificada.");
        }

        switch (type.toLowerCase()) {
            case "quick":
                return new QuickScanStrategy();
            case "deep":
                return new DeepScanStrategy();
            case "targeted":
                return new TargetedScanStrategy(new File(filePath)); // 📌 Ahora pasa la ruta del archivo
            default:
                throw new IllegalArgumentException("⚠ Estrategia de escaneo no válida: " + type);
        }
    }
}
