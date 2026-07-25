import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-terminos-condiciones',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './terminos-condiciones.html',
  styleUrl: './terminos-condiciones.scss'
})
export class TerminosCondicionesComponent {}