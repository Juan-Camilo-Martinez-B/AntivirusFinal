package com.antivirus.Antivirus.service;

public interface SystemObserver {
    void onFileChanged(String filePath);
}
