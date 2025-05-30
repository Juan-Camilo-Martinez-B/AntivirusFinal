import { Component, ChangeDetectorRef } from '@angular/core';
import { iniciarEscaneo } from '../../services/scan.service'; 
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import axios from 'axios';

const BASE_URL = 'http://localhost:8090/scan';

@Component({
  selector: 'app-scan',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  templateUrl: './scan.component.html',
  styleUrls: ['./scan.component.css']
})
export class ScanComponent {
  // Valores de la UI
  targetedFiles: File[] = [];
  scanType: string = '';
  scanning = false;       // Bandera para indicar que hay un escaneo en curso
  scanCompleted = false;
  // Aquí se mostrará el conteo en tiempo real obtenido del backend
  scannedFilesCount: number | null = null;
  detectedThreats: string[] = [];
  scanResults: any = {}; 

  // Referencia al intervalo usado para el sondeo (polling)
  private pollInterval: any;

  constructor(private cdr: ChangeDetectorRef) {}

  // Método para iniciar el sondeo que consulta el endpoint /currentCount
  private startPolling(): void {
    // Actualiza el conteo cada 2 segundos
    this.pollInterval = setInterval(() => {
      axios.get(`${BASE_URL}/currentCount`)
        .then(response => {
          // Se actualiza el valor en tiempo real
          this.scannedFilesCount = response.data;
          this.cdr.detectChanges();
        })
        .catch(error => {
          console.error("⚠ Error al obtener el conteo en tiempo real:", error);
        });
    }, 2000);
  }

  private stopPolling(): void {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  // Inicia el escaneo rápido si no hay otro en curso
  async startQuickScan() {
    if (this.scanning) return;
    await this.startScan('quick');
  }

  // Inicia el escaneo profundo si no hay otro en curso
  async startDeepScan() {
    if (this.scanning) return;
    await this.startScan('deep');
  }

  // Escaneo dirigido: se evita iniciarlo si ya hay un escaneo activo
  async startTargetedScan() {
    if (this.scanning) {
      alert("Ya existe un escaneo en curso. Detén el escaneo actual antes de iniciar uno nuevo.");
      return;
    }
    if (this.targetedFiles.length === 0) {
      alert('⚠️ Selecciona un archivo antes de iniciar el escaneo.');
      return;
    }

    this.scanning = true;
    this.scanCompleted = false;
    const filePath = `C:/Users/juanc/Downloads/aaaaaaaaaaaaaaa/${this.targetedFiles[0].name}`;
    console.log(`📌 Ejecutando solicitud: GET ${BASE_URL}/scanSystem?scanType=targeted&filePath=${encodeURIComponent(filePath)}`);

    try {
      const response = await axios.get(`${BASE_URL}/scanSystem?scanType=targeted&filePath=${encodeURIComponent(filePath)}`);
      console.log("📌 Respuesta completa del backend:", response.data);

      if (response.data) {
        this.scanType = 'targeted';
        // Empaquetar la respuesta para la UI
        this.scanResults = { filePath, result: response.data };
        this.detectedThreats = response.data.includes("🚨") ? ["Amenaza detectada"] : [];
        this.cdr.detectChanges();
      }
    } catch (error) {
      console.error("⚠ Error al iniciar escaneo dirigido:", error);
    }

    this.scanning = false;
    this.scanCompleted = true;
  }

  // Método general para iniciar escaneo automático (Quick o Deep)
  async startScan(type: string) {
    if (this.scanning) {
      alert("Ya existe un escaneo en curso. Detén el escaneo actual antes de iniciar uno nuevo.");
      return;
    }
    this.scanType = type;
    this.scanning = true;
    this.scanCompleted = false;
    this.detectedThreats = [];
    this.scanResults = {};
    // Reinicia el contador para el sondeo en tiempo real
    this.scannedFilesCount = 0;

    // Arranca el polling para actualizar el conteo en tiempo real
    this.startPolling();

    console.log(`🛡️ Escaneo "${this.scanType}" iniciado...`);

    try {
      // Inicia el escaneo llamando al servicio
      const response = await iniciarEscaneo(type);
      console.log("📌 Respuesta del backend recibida:", response);

      if (response) {
        if (type !== 'targeted') {
          this.scannedFilesCount = response.totalFilesScanned || 0;
          this.scanResults = response.scanResults || {};
          this.detectedThreats = response.detectedThreats || [];
          console.log("📌 Resultados obtenidos del backend:", this.scanResults);
        } else {
          this.scanResults = response;
          this.detectedThreats = response.includes("🚨") ? ["Amenaza detectada"] : [];
          this.cdr.detectChanges(); // Forzar actualización de la UI
        }
      }
    } catch (error: any) {
      console.error("⚠ Error durante el escaneo:", error);
    } finally {
      // Detiene el polling cuando finaliza el escaneo
      this.stopPolling();
      this.scanning = false;
      this.scanCompleted = true;
    }
  }

  // Método público para detener el escaneo en curso (Quick o Deep)
  public detenerEscaneo(): void {
    // Se invoca el endpoint de cancelación en el backend
    axios.post(`${BASE_URL}/stopScan`)
      .then(response => {
        console.log("🛑 Detención solicitada en backend:", response.data);
      })
      .catch(error => {
        console.error("⚠ Error al detener el escaneo:", error);
      });
    // Se detiene el polling en caso de que siga activo
    this.stopPolling();
    // Actualiza el estado de la UI, suponiendo que el backend detendrá el proceso
    this.scanning = false;
    this.scanCompleted = true;
  }

  // Método para seleccionar un archivo para el escaneo dirigido
  selectTargetedFiles() {
    const input = document.createElement('input');
    input.type = 'file';
    input.multiple = false;
    input.accept = '*/*';

    input.addEventListener('change', (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files && target.files.length > 0) {
        this.targetedFiles = Array.from(target.files);
        console.log('📂 Archivo seleccionado:', this.targetedFiles[0].name);
      }
    });

    input.click();
  }
}
