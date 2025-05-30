import { Component, OnDestroy } from '@angular/core';
import { iniciarMonitoreo, detenerMonitoreo, obtenerCambiosMonitoreados } from '../../services/monitor.service';
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
  // Almacenará la ruta absoluta del directorio a monitorear.
  rutaSeleccionada: string = '';
  cambios: string[] = [];
  mensaje = '';
  private cambiosSubscription!: Subscription;

  /**
   * En lugar de usar file input (que solo entrega rutas relativas) se utiliza
   * un prompt para que el usuario ingrese la ruta absoluta del directorio.
   */
  seleccionarRuta() {
    const ruta = prompt('Ingresa la ruta absoluta del directorio a monitorear:');
    if (ruta) {
      this.rutaSeleccionada = ruta;
      console.log('📂 Carpeta/archivo seleccionado:', this.rutaSeleccionada);
    }
  }

  /**
   * Llama al backend para iniciar el monitoreo sobre la ruta especificada
   * y luego inicia el polling para actualizar los cambios.
   */
  async iniciarMonitoreo() {
    if (!this.rutaSeleccionada) {
      alert('⚠️ Ingresa o selecciona una carpeta antes de iniciar el monitoreo.');
      return;
    }

    try {
      const data = await iniciarMonitoreo(this.rutaSeleccionada);
      // Se espera que el backend responda con un mensaje de éxito o de error.
      this.mensaje = data ? `✅ Monitoreo iniciado en: ${this.rutaSeleccionada}` : '⚠ Error al iniciar monitoreo';
      console.log("📌 Log del backend tras inicio de monitoreo:", data);
      this.actualizarCambios();
    } catch (error) {
      console.error('⚠ Error al iniciar monitoreo:', error);
    }
  }

  /**
   * Mantiene un polling cada 20 segundos para consultar los cambios detectados
   * por el backend.
   */
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

  /**
   * Llama al backend para detener el monitoreo y cancela el polling en el frontend.
   */
  async detenerMonitoreo() {
    try {
      const data = await detenerMonitoreo();
      console.log("📌 Respuesta del backend al detener monitoreo:", data);
      this.mensaje = "⛔ Monitoreo detenido.";
    } catch (error) {
      console.error("⚠ Error al detener monitoreo:", error);
      this.mensaje = "⚠ Error al detener monitoreo.";
    } finally {
      if (this.cambiosSubscription) {
        this.cambiosSubscription.unsubscribe();
        console.log("🛑 Suscripción de monitoreo cancelada.");
      }
    }
  }

  ngOnDestroy() {
    if (this.cambiosSubscription) {
      this.cambiosSubscription.unsubscribe();
    }
  }
}
