package com.antivirus.Antivirus.agenda;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanAgenda {
    
    // La clave es la fecha del escaneo y el valor es la lista de eventos o resultados.
    private Map<Date, List<String>> agenda = new HashMap<>();
    
    /**
     * Agrega un evento a la agenda para la fecha actual.
     * Se sincroniza el método para evitar condiciones de carrera cuando se agregan eventos.
     * @param event El evento (por ejemplo, "Archivo X escaneado, sin amenazas" o "Amenaza detectada en Y").
     */
    public synchronized void addEvent(String event) {
        Date now = new Date();
        agenda.putIfAbsent(now, new ArrayList<>());
        agenda.get(now).add(event);
    }
    
    /**
     * Obtiene la agenda completa.
     * @return El mapa que asocia fechas con la lista de eventos.
     */
    public Map<Date, List<String>> getAgenda() {
        return agenda;
    }
    
    /**
     * Limpia la agenda.
     */
    public void clearAgenda() {
        agenda.clear();
    }
}
