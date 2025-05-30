import { Component, ChangeDetectorRef } from '@angular/core';
import { iniciarEscaneo } from '../../services/scan.service'; 
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import axios from 'axios';

const BASE_URL = 'http://localhost:8090/scan';

// Definición de la interfaz según lo que retorna el backend
interface ScannedFile {
  filePath: string;
  scanTime: string; // O Date, según lo que retorne tu backend
  result: string;
  category: string;
}

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
  scannedFilesCount: number | null = null;
  detectedThreats: string[] = [];
  scanResults: any = {}; 
  // Nueva propiedad para los archivos "limpios"
  cleanFiles: ScannedFile[] = [];

  // Referencia al intervalo usado para el sondeo (polling)
  private pollInterval: any;

  constructor(private cdr: ChangeDetectorRef) {}

  // Método para iniciar el sondeo que consulta el endpoint /currentCount
  private startPolling(): void {
    this.pollInterval = setInterval(() => {
      axios.get(`${BASE_URL}/currentCount`)
        .then(response => {
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

  async startQuickScan() {
    if (this.scanning) return;
    await this.startScan('quick');
  }

  async startDeepScan() {
    if (this.scanning) return;
    await this.startScan('deep');
  }

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
    // Inicializamos el contador, se actualizará mediante el polling
    this.scannedFilesCount = 0;

    this.startPolling();

    console.log(`🛡️ Escaneo "${this.scanType}" iniciado...`);

    try {
      const response = await iniciarEscaneo(type);
      console.log("📌 Respuesta del backend recibida:", response);

      if (response) {
        if (type !== 'targeted') {
          // No sobrescribir el contador, se actualiza via polling.
          // this.scannedFilesCount = response.totalFilesScanned || 0;
          this.scanResults = response.scanResults || {};
          this.detectedThreats = response.detectedThreats || [];
          console.log("📌 Resultados obtenidos del backend:", this.scanResults);
        } else {
          this.scanResults = response;
          this.detectedThreats = response.includes("🚨") ? ["Amenaza detectada"] : [];
          this.cdr.detectChanges();
        }
      }
    } catch (error: any) {
      console.error("⚠ Error durante el escaneo:", error);
    } finally {
      this.stopPolling();
      this.scanning = false;
      this.scanCompleted = true;
    }
  }

  public detenerEscaneo(): void {
    axios.post(`${BASE_URL}/stopScan`)
      .then(response => {
        console.log("🛑 Detención solicitada en backend:", response.data);
      })
      .catch(error => {
        console.error("⚠ Error al detener el escaneo:", error);
      });
    this.stopPolling();
    this.scanning = false;
    this.scanCompleted = true;
  }

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

  // Método para cargar los archivos del BST con la categoría "Sin amenazas conocidas"
  async loadCleanFiles() {
    try {
      const response = await axios.get(`${BASE_URL}/scannedFiles`);
      if (Array.isArray(response.data)) {
        // Filtra los archivos que tengan la categoría "Sin amenazas conocidas"
        this.cleanFiles = response.data.filter((file: ScannedFile) => file.category === "Sin amenazas conocidas");
        console.log("Archivos limpios:", this.cleanFiles);
        this.cdr.detectChanges();
      }
    } catch (error) {
      console.error("⚠ Error al obtener archivos limpios:", error);
    }
  }
}
