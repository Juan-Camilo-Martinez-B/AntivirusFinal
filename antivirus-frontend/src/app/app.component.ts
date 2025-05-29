import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, MatToolbarModule, MatCardModule, MatGridListModule, MatSlideToggleModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'antivirus-frontend';

  constructor(private router: Router) {}

  irA(ruta: string) {
    if (ruta) {
      this.router.navigate([ruta]); // 📌 Previene posibles errores de navegación
    }
  }

  toggleModoOscuro() {
    document.body.classList.toggle('dark-mode'); // 🔥 Activa/desactiva modo oscuro
  }
}
