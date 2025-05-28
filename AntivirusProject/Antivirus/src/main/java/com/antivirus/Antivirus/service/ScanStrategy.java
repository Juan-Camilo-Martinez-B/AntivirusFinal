package com.antivirus.Antivirus.service;

import java.io.File;

public interface ScanStrategy {
    String scan(File file);
}
