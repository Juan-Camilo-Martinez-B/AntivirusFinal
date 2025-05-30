package com.antivirus.Antivirus.service;

import com.antivirus.Antivirus.config.ScanStrategyFactory;
import java.io.File;

public class FileChangeListener implements SystemObserver {
    @Override
    public void onFileChanged(String filePath) {
        System.out.println("⚠ Archivo modificado: " + filePath);
        File file = new File(filePath);
        
        ScanStrategy strategy = ScanStrategyFactory.getStrategy(file.getName());
        System.out.println(strategy.scan(file)); // YARA analizará el archivo en tiempo real
    }
}
