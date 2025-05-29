import { Component, ChangeDetectorRef } from '@angular/core';
import { iniciarEscaneo } from '../../services/scan.service'; 
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import axios from 'axios'; // ✅ Necesario para la solicitud de Targeted Scan

const BASE_URL = 'http://localhost:8090/scan';

@Component({
  selector: 'app-scan',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  templateUrl: './scan.component.html',
  styleUrls: ['./scan.component.css']
})
export class ScanComponent {
  targetedFiles: File[] = [];
  scanType: string = '';
  scanning = false;
  scanCompleted = false;
  scannedFilesCount: number | null = null;
  detectedThreats: string[] = [];
  scanResults: any = {}; 

  constructor(private cdr: ChangeDetectorRef) {}

  async startQuickScan() {
    await this.startScan('quick'); 
  }

  async startDeepScan() {
    await this.startScan('deep'); 
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

  async startTargetedScan() {
    if (this.targetedFiles.length === 0) {
      alert('⚠️ Selecciona un archivo antes de iniciar el escaneo.');
      return;
    }

    const filePath = `C:/Users/juanc/Downloads/aaaaaaaaaaaaaaa/${this.targetedFiles[0].name}`;
    console.log(`📌 Ejecutando solicitud: GET ${BASE_URL}/scanSystem?scanType=targeted&filePath=${encodeURIComponent(filePath)}`);

    try {
      const response = await axios.get(`${BASE_URL}/scanSystem?scanType=targeted&filePath=${encodeURIComponent(filePath)}`);
      console.log("📌 Respuesta completa del backend:", response.data); 

      if (response.data) {
        this.scanType = 'targeted';
        this.scanResults = { filePath, result: response.data }; // ✅ Manejo correcto del texto plano recibido
        this.detectedThreats = response.data.includes("🚨") ? ["Amenaza detectada"] : [];
        this.cdr.detectChanges(); // ✅ Forzar actualización de Angular
      }
    } catch (error) {
      console.error("⚠ Error al iniciar escaneo dirigido:", error);
    }

    this.scanning = false;
    this.scanCompleted = true;
  }

  async startScan(tipo: string) {
    this.scanType = tipo;
    this.scanning = true;
    this.scanCompleted = false;
    this.detectedThreats = [];
    this.scanResults = {};
    this.scannedFilesCount = null;

    console.log(`🛡️ Escaneo "${this.scanType}" iniciado...`);

    const response = await iniciarEscaneo(tipo);
    console.log("📌 Respuesta del backend recibida:", response.data);

    if (response) {
      if (tipo !== 'targeted') {
        this.scannedFilesCount = response.totalFilesScanned || 0;
        this.scanResults = response.scanResults || {};
        this.detectedThreats = response.detectedThreats || [];
      } else {
        this.scanResults = response;
        this.detectedThreats = response.includes("🚨") ? ["Amenaza detectada"] : [];
        this.cdr.detectChanges(); // ✅ Forzar actualización de la UI
      }
    }

    this.scanning = false;
    this.scanCompleted = true;
  }
}
