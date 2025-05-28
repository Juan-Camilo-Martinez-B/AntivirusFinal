package com.antivirus.Antivirus.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickScanStrategy implements ScanStrategy {
    private static final String YARA_EXECUTABLE = "yara"; // 📌 Ajusta la ruta si es necesario
    private static final String RULES_FILE = "src/main/resources/rules_quick.yar"; // 📌 Archivo con reglas
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".exe", ".dll", ".sys", ".docm", ".js", ".bat");
    private static final List<String> detectedThreats = new ArrayList<>(); // 📌 Lista de archivos con amenazas

    @Override
    public String scan(File file) {
        if (!shouldScan(file)) {
            return "⚠ Archivo no relevante para escaneo rápido: " + file.getAbsolutePath();
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {

            // Carga solo los primeros 512 KB en memoria para acelerar la lectura
            long bufferSize = Math.min(channel.size(), 512 * 1024);
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, bufferSize);

            byte[] bytes = new byte[(int) bufferSize];
            buffer.get(bytes);
            String fileContent = new String(bytes, StandardCharsets.UTF_8);

            // Ejecuta YARA con el archivo mapeado
            Process process = new ProcessBuilder(YARA_EXECUTABLE, RULES_FILE, file.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

            process.waitFor();
            byte[] outputBytes = process.getInputStream().readAllBytes();
            String result = new String(outputBytes, StandardCharsets.UTF_8).trim();

            // 📌 Si se detecta una amenaza, guardar el nombre del archivo en la lista
            if (!result.isEmpty()) {
                addDetectedThreat(file.getAbsolutePath()); // 📌 Usa el nuevo método
            }

            return result.isEmpty() ? "✅ Sin amenazas detectadas en escaneo rápido." : "🚨 Amenaza detectada en escaneo rápido: " + result;
        } catch (IOException | InterruptedException e) {
            return "⚠ Error en escaneo rápido con YARA: " + e.getMessage();
        }
    }

    // 📌 Método para agregar una amenaza a la lista
    public static void addDetectedThreat(String filePath) {
        detectedThreats.add(filePath);
    }

    // 📌 Método para limpiar la lista de amenazas antes de un nuevo escaneo
    public static void clearDetectedThreats() {
        detectedThreats.clear();
    }

    // 📌 Método para obtener la lista de archivos detectados con amenazas
    public static List<String> getDetectedThreats() {
        return detectedThreats;
    }

    private boolean shouldScan(File file) {
        return file.exists() && file.isFile() && file.canRead() && file.length() > 500 * 1024 &&
               ALLOWED_EXTENSIONS.stream().anyMatch(ext -> file.getName().endsWith(ext));
    }
}
