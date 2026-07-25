import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-libro-reclamaciones',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './libro-reclamaciones.html',
  styleUrl: './libro-reclamaciones.scss'
})
export class LibroReclamacionesComponent {

  enviado = false;

  enviarReclamo(): void {
    this.enviado = true;
  }
}