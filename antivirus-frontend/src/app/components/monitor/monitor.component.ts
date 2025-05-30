import { Component, OnDestroy } from '@angular/core';
import { iniciarMonitoreo, obtenerCambiosMonitoreados } from '../../services/monitor.service';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-monitor',
  standalone: true,
  imports: [MatCardModule, CommonModule],
  templateUrl: './monitor.component.html',
  styleUrls: ['./monitor.component.css']
})
export class MonitorComponent implements OnDestroy {
  rutaSeleccionada: string = '';
  cambios: string[] = [];
  mensaje = '';
  private cambiosSubscription!: Subscription;

  seleccionarRuta() {
    const input = document.createElement('input');
    input.type = 'file';
    input.webkitdirectory = true; // Permite seleccionar carpetas en algunos navegadores
    input.multiple = false;

    input.addEventListener('change', (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files && target.files.length > 0) {
        // Se utiliza webkitRelativePath si está disponible para obtener la ruta completa de la carpeta.
        this.rutaSeleccionada = target.files[0].webkitRelativePath || target.files[0].name;
        console.log('📂 Carpeta/archivo seleccionado:', this.rutaSeleccionada);
      }
    });

    input.click();
  }

  async iniciarMonitoreo() {
    if (!this.rutaSeleccionada) {
      alert('⚠️ Selecciona una carpeta o archivo antes de iniciar el monitoreo.');
      return;
    }

    try {
      const data = await iniciarMonitoreo(this.rutaSeleccionada);
      this.mensaje = data ? `✅ Monitoreo iniciado en: ${this.rutaSeleccionada}` : '⚠ Error al iniciar monitoreo';
      console.log("📌 Log del backend tras inicio de monitoreo:", data);
      this.actualizarCambios();
    } catch (error) {
      console.error('⚠ Error al iniciar monitoreo:', error);
    }
  }

  actualizarCambios() {
    this.cambiosSubscription = interval(20000).subscribe(async () => {
      try {
        const data = await obtenerCambiosMonitoreados();
        if (data && JSON.stringify(this.cambios) !== JSON.stringify(data)) {
          this.cambios = data;
          console.log("📌 Cambios detectados:", data);
        } else {
          console.log("✅ No hay cambios detectados.");
        }
      } catch (error) {
        console.error('⚠ Error al obtener cambios monitoreados:', error);
      }
    });
  }

  // Nueva función para detener el monitoreo:
  detenerMonitoreo() {
    if (this.cambiosSubscription) {
      this.cambiosSubscription.unsubscribe();
      this.mensaje = "⛔ Monitoreo detenido.";
      console.log("🛑 Monitoreo detenido.");
    }
  }

  ngOnDestroy() {
    if (this.cambiosSubscription) {
      this.cambiosSubscription.unsubscribe();
    }
  }
}
