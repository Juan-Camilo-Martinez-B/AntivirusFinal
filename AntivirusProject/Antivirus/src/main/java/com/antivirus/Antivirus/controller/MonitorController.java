package com.antivirus.Antivirus.controller;

import com.antivirus.Antivirus.service.FileMonitor;
import com.antivirus.Antivirus.service.SystemObserver;
import com.antivirus.Antivirus.util.CircularList; // 🔥 Importamos CircularList
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/monitor")
public class MonitorController implements SystemObserver {

    private final FileMonitor fileMonitor;
    private final CircularList<String> detectedChanges = new CircularList<>(50); // 🔄 Guarda solo los últimos 50 cambios

    public MonitorController() {
        try {
            this.fileMonitor = new FileMonitor();
            fileMonitor.addObserver(this::onFileChanged);
        } catch (Exception e) {
            throw new RuntimeException("⚠ Error al inicializar FileMonitor: " + e.getMessage(), e);
        }
    }

    @PostMapping("/startMonitoring")
    public String startMonitoring(@RequestParam String directoryPath) {
        try {
            fileMonitor.watchDirectory(directoryPath);
            return fileMonitor.isMonitoring() 
                ? "🔎 Monitoreo iniciado en: " + directoryPath
                : "⚠ No se pudo iniciar el monitoreo.";
        } catch (Exception e) {
            return "⚠ Error al iniciar el monitoreo: " + e.getMessage();
        }
    }

    @PostMapping("/stopMonitoring")
    public String stopMonitoring() {
        try {
            fileMonitor.stopMonitoring();
            return "⏹ Monitoreo detenido.";
        } catch (Exception e) {
            return "⚠ Error al detener el monitoreo: " + e.getMessage();
        }
    }

    @GetMapping("/getMonitoredChanges")
    public List<String> getMonitoredChanges() {
        return detectedChanges.getAll(); // 📌 Devuelve los últimos 50 cambios monitoreados
    }

    @Override
    public void onFileChanged(String filePath) {
        detectedChanges.add(filePath);
        System.out.println("⚠ Archivo modificado: " + filePath);
    }
}
