import { Component, OnInit } from '@angular/core';
import { obtenerAmenazas } from '../../services/threat.service';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-threat',
  standalone: true,
  imports: [MatCardModule, CommonModule],
  templateUrl: './threat.component.html',
  styleUrls: ['./threat.component.css']
})
export class ThreatComponent implements OnInit {
  quickThreats: string[] = [];
  deepThreats: string[] = [];

  async ngOnInit() {
    await this.cargarAmenazas();
  }

  async cargarAmenazas() {
    try {
      const data = await obtenerAmenazas();
      if (data) {
        this.quickThreats = data.quick || []; // ✅ Almacena amenazas de Quick Scan
        this.deepThreats = data.deep || []; // ✅ Almacena amenazas de Deep Scan
      }
    } catch (error) {
      console.error('⚠ Error al obtener amenazas:', error);
    }
  }
}
