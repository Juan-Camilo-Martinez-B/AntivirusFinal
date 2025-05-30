package com.antivirus.Antivirus.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DeepScanStrategy implements ScanStrategy {
    private static final String YARA_EXECUTABLE = "yara"; // 📌 Ajusta la ruta si es necesario
    private static final String RULES_FILE = "src/main/resources/rules.yar"; // 📌 Archivo con reglas YARA
    private static final List<String> detectedThreats = new ArrayList<>(); // 📌 Lista de amenazas detectadas en escaneo profundo

    @Override
    public String scan(File file) {
        if (!file.exists() || !file.isFile()) {
            return "⚠ Archivo no válido para escaneo profundo: " + file.getAbsolutePath();
        }

        try {
            Process process = new ProcessBuilder(YARA_EXECUTABLE, RULES_FILE, file.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

            InputStream inputStream = process.getInputStream();
            process.waitFor();

            byte[] outputBytes = inputStream.readAllBytes();
            String result = new String(outputBytes, StandardCharsets.UTF_8).trim();

            // Si se detecta una amenaza, guardar el nombre del archivo en la lista
            if (!result.isEmpty()) {
                addDetectedThreat(file.getAbsolutePath()); // 📌 Usa el nuevo método
            }

            return result.isEmpty() ? "✅ Sin amenazas detectadas en escaneo profundo." : "🚨 Amenaza detectada en escaneo profundo: " + result;
        } catch (IOException | InterruptedException e) {
            return "⚠ Error al ejecutar escaneo profundo con YARA: " + e.getMessage();
        }
    }

    // Método para agregar una amenaza a la lista
    public static void addDetectedThreat(String filePath) {
        detectedThreats.add(filePath);
    }

    // Método para limpiar la lista de amenazas antes de un nuevo escaneo
    public static void clearDetectedThreats() {
        detectedThreats.clear();
    }

    // Método para obtener la lista de archivos detectados con amenazas
    public static List<String> getDetectedThreats() {
        return detectedThreats;
    }
}
