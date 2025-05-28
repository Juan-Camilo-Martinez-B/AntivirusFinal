package com.antivirus.Antivirus.service;

import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FileMonitor {
    private final WatchService watchService;
    private final Set<Consumer<String>> observers = new HashSet<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isMonitoring = false;

    public FileMonitor() throws Exception {
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    public void addObserver(Consumer<String> observer) {
        observers.add(observer);
    }

    public void watchFile(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        if (!path.toFile().exists() || !path.toFile().isFile()) {
            throw new Exception("⚠ Error: El archivo no existe o no es válido.");
        }

        System.out.println("🔎 Monitoreando archivo: " + filePath);
        Path parentDir = path.getParent();
        parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        startMonitoringLoop(parentDir);
    }

    public void watchDirectory(String directoryPath) throws Exception {
        Path path = Paths.get(directoryPath);
        if (!path.toFile().exists() || !path.toFile().isDirectory()) {
            throw new Exception("⚠ Error: El directorio no existe o no es válido.");
        }

        System.out.println("🔎 Monitoreando directorio: " + directoryPath);
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, 
                      StandardWatchEventKinds.ENTRY_MODIFY, 
                      StandardWatchEventKinds.ENTRY_DELETE);

        startMonitoringLoop(path);
    }

    private void startMonitoringLoop(Path monitoredPath) {
        isMonitoring = true;
        executorService.submit(() -> {
            try {
                while (isMonitoring) {
                    WatchKey wk = watchService.take();
                    for (WatchEvent<?> event : wk.pollEvents()) {
                        Path changed = monitoredPath.resolve((Path) event.context());
                        System.out.println("⚠ Cambio detectado: " + changed);
                        observers.forEach(observer -> observer.accept(changed.toString()));
                    }
                    if (!wk.reset()) break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void stopMonitoring() throws Exception {
        isMonitoring = false;
        watchService.close();
        executorService.shutdown();
        System.out.println("✅ Monitoreo detenido correctamente.");
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }
}
