package com.antivirus.Antivirus.controller;

import com.antivirus.Antivirus.service.ScanFactory;
import com.antivirus.Antivirus.service.ScanStrategy;
import com.antivirus.Antivirus.service.QuickScanStrategy;
import com.antivirus.Antivirus.service.DeepScanStrategy;
import com.antivirus.Antivirus.service.TargetedScanStrategy;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/scan")
public class ScanController {

    // Bandera para la cancelación del escaneo.
    private volatile boolean cancelScanReq = false;
    // Contador atómico para llevar el conteo en tiempo real de archivos escaneados.
    private final AtomicInteger realTimeScannedCount = new AtomicInteger(0);

    @GetMapping("/scanSystem")
    public String scanSystem(@RequestParam String scanType, @RequestParam(required = false) String filePath) {
        try {
            // Reiniciamos los conteos y la bandera antes de iniciar un nuevo escaneo.
            QuickScanStrategy.clearDetectedThreats();
            DeepScanStrategy.clearDetectedThreats();
            cancelScanReq = false;
            realTimeScannedCount.set(0);

            // Manejo especial para "targeted"
            if ("targeted".equalsIgnoreCase(scanType)) {
                if (filePath == null || filePath.isEmpty()) {
                    return "⚠ Error: Se requiere un archivo para Targeted Scan.";
                }
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    return "⚠ Archivo no válido: " + filePath;
                }
                TargetedScanStrategy scanner = new TargetedScanStrategy(file);
                // Para un escaneo dirigido se ejecuta directamente y no se cuenta en tiempo real
                return scanner.scan(file);
            }

            // Escaneo Global (Quick/Deep)
            ScanStrategy strategy = ScanFactory.getScanStrategy(scanType, null);
            if (strategy == null) {
                return "⚠ Tipo de escaneo no válido: " + scanType;
            }

            File[] roots = File.listRoots();
            int[] threatCount = { 0 };

            for (File root : roots) {
                System.out.println("🔎 Escaneando unidad: " + root.getAbsolutePath());
                // Actualiza el conteo en paralelo de cada archivo escaneado
                scanDirectoryRecursively(root, strategy, scanType, threatCount);
                if (cancelScanReq) {
                    return "❗ Escaneo cancelado por el cliente.";
                }
            }

            int totalScanned = realTimeScannedCount.get();
            String message = "✅ Escaneo global completado con estrategia: " + scanType +
                             ". Total de archivos escaneados: " + totalScanned;
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
                if (cancelScanReq) return 0;
                if (file.isDirectory()) {
                    return scanDirectoryRecursively(file, strategy, scanType, threatCount);
                } else {
                    try {
                        if (cancelScanReq) return 0;
                        System.out.println("🔎 Escaneando archivo: " + file.getAbsolutePath());
                        String result = strategy.scan(file);
                        System.out.println(result);
                        // Incremento del contador de archivos escaneados en tiempo real
                        realTimeScannedCount.incrementAndGet();
                        if (!result.contains("✅ Sin amenazas") && result.contains("🚨")) {
                            threatCount[0]++;
                        }
                        return 1;
                    } catch (Exception e) {
                        System.out.println("⚠ Error al escanear archivo " + file.getAbsolutePath() + ": " + e.getMessage());
                        return 0;
                    }
                }
            })
            .sum();
    }

    // Endpoint para detener el escaneo global (Quick o Deep)
    @PostMapping("/stopScan")
    public String stopScan() {
        cancelScanReq = true;
        System.out.println("🛑 Solicitud de cancelación recibida. Se detendrá el escaneo.");
        return "Escaneo cancelado por el cliente";
    }

    // Endpoint para consultar el conteo en tiempo real de archivos escaneados
    @GetMapping("/currentCount")
    public int currentCount() {
        return realTimeScannedCount.get();
    }
}
