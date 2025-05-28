package com.antivirus.Antivirus.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class YARAScanStrategy implements ScanStrategy {
    @Override
    public String scan(File file) {
        try {
            if (!file.exists() || !file.isFile()) {
                return "⚠ Archivo no válido para escaneo: " + file.getAbsolutePath();
            }

            Process process = new ProcessBuilder("yara", "src/main/resources/rules.yar", file.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

            InputStream inputStream = process.getInputStream();
            process.waitFor();
            
            byte[] outputBytes = inputStream.readAllBytes();
            String result = new String(outputBytes, StandardCharsets.UTF_8).trim();

            return result.isEmpty() ? "✅ Sin amenazas detectadas." : "🚨 Amenaza detectada: " + result;
        } catch (IOException | InterruptedException e) {
            return "⚠ Error al ejecutar YARA: " + e.getMessage();
        }
    }
}
