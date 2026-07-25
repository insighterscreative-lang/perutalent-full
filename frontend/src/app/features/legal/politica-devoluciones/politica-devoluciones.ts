import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-politica-devoluciones',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './politica-devoluciones.html',
  styleUrl: './politica-devoluciones.scss'
})
export class PoliticaDevolucionesComponent {}