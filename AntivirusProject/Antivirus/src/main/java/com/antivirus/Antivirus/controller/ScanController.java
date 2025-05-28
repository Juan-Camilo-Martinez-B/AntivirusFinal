package com.antivirus.Antivirus.controller;

import com.antivirus.Antivirus.service.ScanFactory;
import com.antivirus.Antivirus.service.ScanStrategy;
import com.antivirus.Antivirus.service.QuickScanStrategy;
import com.antivirus.Antivirus.service.DeepScanStrategy;
import com.antivirus.Antivirus.service.TargetedScanStrategy;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/scan")
public class ScanController {

    @GetMapping("/scanSystem")
    public String scanSystem(@RequestParam String scanType, @RequestParam(required = false) String filePath) {
        try {
            QuickScanStrategy.clearDetectedThreats(); 
            DeepScanStrategy.clearDetectedThreats();

            // ✅ Manejo especial para "targeted"
            if ("targeted".equalsIgnoreCase(scanType)) {
                if (filePath == null || filePath.isEmpty()) {
                    return "⚠ Error: Se requiere un archivo para Targeted Scan.";
                }
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    return "⚠ Archivo no válido: " + filePath;
                }

                TargetedScanStrategy scanner = new TargetedScanStrategy(file);
                return scanner.scan(file); // ✅ Ejecuta escaneo dirigido correctamente
            }

            // ✅ Escaneo Global (Quick/Deep)
            ScanStrategy strategy = ScanFactory.getScanStrategy(scanType, null);
            if (strategy == null) {
                return "⚠ Tipo de escaneo no válido: " + scanType;
            }

            File[] roots = File.listRoots();
            int scannedFiles = 0;
            int[] threatCount = {0}; 

            for (File root : roots) {
                System.out.println("🔎 Escaneando unidad: " + root.getAbsolutePath());
                scannedFiles += scanDirectoryRecursively(root, strategy, scanType, threatCount);
            }

            String message = "✅ Escaneo global completado con estrategia: " + scanType +
                             ". Total de archivos escaneados: " + scannedFiles;

            if (threatCount[0] > 0) {
                message += ". 🚨 Amenazas detectadas: " + threatCount[0];
            } else {
                message += ". ✅ Sin amenazas detectadas.";
            }

            return message;
        } catch (Exception e) {
            return "⚠ Error durante el escaneo: " + e.getMessage();
        }
    }

    private int scanDirectoryRecursively(File directory, ScanStrategy strategy, String scanType, int[] threatCount) {
        File[] files = directory.listFiles();
        if (files == null) return 0;

        return Arrays.stream(files)
            .parallel()
            .mapToInt(file -> {
                if (file.isDirectory()) {
                    return scanDirectoryRecursively(file, strategy, scanType, threatCount);
                } else {
                    try {
                        System.out.println("🔎 Escaneando archivo: " + file.getAbsolutePath());
                        String result = strategy.scan(file);
                        System.out.println(result);

                        if (!result.contains("✅ Sin amenazas") && result.contains("🚨")) { 
                            threatCount[0]++;
                        }

                        return 1;
                    } catch (Exception e) {
                        System.out.println("⚠ Error al escanear archivo " + file.getAbsolutePath() + ": " + e.getMessage());
                        return 0;
                    }
                }
            }).sum();
    }
}
