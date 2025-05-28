package com.antivirus.Antivirus.controller;

import com.antivirus.Antivirus.service.QuickScanStrategy;
import com.antivirus.Antivirus.service.DeepScanStrategy;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/threats")
public class ThreatController {

    @GetMapping("/getThreats")
    public Map<String, List<String>> getThreats() {
        try {
            Map<String, List<String>> threats = new HashMap<>();
            
            // 📌 Eliminamos duplicados y ordenamos las amenazas detectadas
            threats.put("quick", QuickScanStrategy.getDetectedThreats().stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

            threats.put("deep", DeepScanStrategy.getDetectedThreats().stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

            return threats; // 📌 Devuelve amenazas únicas, ordenadas por estrategia
        } catch (Exception e) {
            return Collections.singletonMap("error", 
                List.of("⚠ Error al obtener amenazas: " + e.getMessage()));
        }
    }
}
