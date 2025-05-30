package com.antivirus.Antivirus.controller;

import com.antivirus.Antivirus.agenda.ScanAgenda;
import com.antivirus.Antivirus.bst.FileBST;
import com.antivirus.Antivirus.bst.ScannedFile;
import com.antivirus.Antivirus.service.ScanFactory;
import com.antivirus.Antivirus.service.ScanStrategy;
import com.antivirus.Antivirus.service.QuickScanStrategy;
import com.antivirus.Antivirus.service.DeepScanStrategy;
import com.antivirus.Antivirus.service.TargetedScanStrategy;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/scan")
public class ScanController {

    // Bandera para la cancelación del escaneo.
    private volatile boolean cancelScanReq = false;
    // Contador atómico para llevar el conteo en tiempo real de archivos escaneados.
    private final AtomicInteger realTimeScannedCount = new AtomicInteger(0);
    // Instancia de ScanAgenda para registrar todos los eventos (ya sean amenazas, archivos no relevantes o limpios).
    private final ScanAgenda scanAgenda = new ScanAgenda();
    // Instancia de FileBST para almacenar los archivos escaneados.
    private final FileBST fileBST = new FileBST();

    /**
     * Método helper que determina la clasificación según el resultado.
     * Si el resultado contiene "🚨", se clasifica como amenaza.
     * Si contiene "✅ Sin amenazas", se clasifica como archivo sin amenazas conocidas.
     * De lo contrario, se clasifica como archivo no relevante.
     */
    private String classifyResult(String result) {
        if(result.contains("🚨")) {
            return "Amenaza detectada";
        } else if(result.contains("✅ Sin amenazas")) {
            return "Sin amenazas conocidas";
        } else {
            return "Archivo no relevante";
        }
    }

    @GetMapping("/scanSystem")
    public String scanSystem(@RequestParam String scanType, @RequestParam(required = false) String filePath) {
        try {
            // Reiniciamos los conteos y la bandera antes de iniciar un nuevo escaneo.
            QuickScanStrategy.clearDetectedThreats();
            DeepScanStrategy.clearDetectedThreats();
            cancelScanReq = false;
            realTimeScannedCount.set(0);
            scanAgenda.clearAgenda();
            
            // Registro de inicio del escaneo.
            scanAgenda.addEvent("Inicio de escaneo con estrategia: " + scanType);

            // Manejo especial para "targeted"
            if ("targeted".equalsIgnoreCase(scanType)) {
                if (filePath == null || filePath.isEmpty()) {
                    return "⚠ Error: Se requiere un archivo para Targeted Scan.";
                }
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    return "⚠ Archivo no válido: " + filePath;
                }
                scanAgenda.addEvent("Realizando Targeted Scan para el archivo: " + filePath);
                TargetedScanStrategy scanner = new TargetedScanStrategy(file);
                String result = scanner.scan(file);
                String classification = classifyResult(result);
                // Se registra en la agenda el resultado con clasificación mediante un mensaje con formato:
                // "Resultado de archivo [<filePath>]: <result> (Clasificación: <classification>)"
                scanAgenda.addEvent("Resultado de archivo [" + file.getAbsolutePath() + "]: " 
                        + result + " (Clasificación: " + classification + ")");
                // No inserto directamente en el BST; se hará una vez finalizado el escaneo.
                return result;
            }

            // Escaneo Global (Quick/Deep)
            ScanStrategy strategy = ScanFactory.getScanStrategy(scanType, null);
            if (strategy == null) {
                return "⚠ Tipo de escaneo no válido: " + scanType;
            }

            File[] roots = File.listRoots();
            int[] threatCount = { 0 };

            for (File root : roots) {
                scanAgenda.addEvent("Escaneando unidad: " + root.getAbsolutePath());
                scanDirectoryRecursively(root, strategy, scanType, threatCount);
                if (cancelScanReq) {
                    scanAgenda.addEvent("Escaneo cancelado durante la unidad: " + root.getAbsolutePath());
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
            scanAgenda.addEvent("Fin del escaneo: " + message);
            
            // AL FINAL del proceso, extraemos y poblamos el BST según los registros de la agenda.
            populateBSTFromAgenda();
            
            return message;
        } catch (Exception e) {
            scanAgenda.addEvent("Error durante el escaneo: " + e.getMessage());
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
                    scanAgenda.addEvent("Ingresando a directorio: " + file.getAbsolutePath());
                    return scanDirectoryRecursively(file, strategy, scanType, threatCount);
                } else {
                    try {
                        if (cancelScanReq) return 0;
                        scanAgenda.addEvent("Escaneando archivo: " + file.getAbsolutePath());
                        String result = strategy.scan(file);
                        String classification = classifyResult(result);
                        // Se registra el resultado del escaneo con la clasificación en la agenda.
                        scanAgenda.addEvent("Resultado de archivo [" + file.getAbsolutePath() + "]: " 
                                + result + " (Clasificación: " + classification + ")");
                        // Incremento del contador de archivos escaneados.
                        realTimeScannedCount.incrementAndGet();
                        if (classification.equals("Amenaza detectada")) {
                            threatCount[0]++;
                        }
                        // No se inserta directamente en el BST.
                        return 1;
                    } catch (Exception e) {
                        scanAgenda.addEvent("⚠ Error al escanear archivo " + file.getAbsolutePath() + ": " + e.getMessage());
                        return 0;
                    }
                }
            })
            .sum();
    }

    // Endpoint para detener el escaneo global (Quick o Deep).
    @PostMapping("/stopScan")
    public String stopScan() {
        cancelScanReq = true;
        scanAgenda.addEvent("Solicitud de cancelación recibida.");
        System.out.println("🛑 Solicitud de cancelación recibida. Se detendrá el escaneo.");
        return "Escaneo cancelado por el cliente";
    }

    // Endpoint para consultar el conteo en tiempo real de archivos escaneados.
    @GetMapping("/currentCount")
    public int currentCount() {
        return realTimeScannedCount.get();
    }
    
    // Endpoint para consultar la agenda de eventos.
    @GetMapping("/getAgenda")
    public Map<String, List<String>> getAgenda() {
        Map<String, List<String>> agendaFormatted = new HashMap<>();
        scanAgenda.getAgenda().forEach((date, events) -> {
            agendaFormatted.put(date.toString(), events);
        });
        return agendaFormatted;
    }

    // Endpoint para consultar los archivos escaneados (BST).
    @GetMapping("/scannedFiles")
    public List<ScannedFile> getScannedFiles() {
        return fileBST.inOrderTraversal();
    }
    
    /**
     * Método que recorre la agenda para extraer eventos que tengan el formato registrado

     */
    private void populateBSTFromAgenda() {
        // Patrón para extraer filePath, result y clasificación.
        Pattern pattern = Pattern.compile("Resultado de archivo \\[(.*?)\\]:\\s*(.*?)\\s*\\(Clasificación:\\s*(.*?)\\)");
        
        // Opción: limpiar el BST antes de poblarlo, si se cuenta con un método clear() o reinicializarlo.
        // fileBST.clear();

        // Recorremos las entradas de la agenda.
        scanAgenda.getAgenda().forEach((date, events) -> {
            for (String event : events) {
                Matcher matcher = pattern.matcher(event);
                if (matcher.find()) {
                    String filePath = matcher.group(1);
                    String result = matcher.group(2);
                    String classification = matcher.group(3);
                    // Creamos un objeto ScannedFile.
                    ScannedFile scannedFile = new ScannedFile(filePath, new Date(), result, classification);
                    // Insertamos en el BST.
                    fileBST.insert(scannedFile);
                }
            }
        });
    }
}
