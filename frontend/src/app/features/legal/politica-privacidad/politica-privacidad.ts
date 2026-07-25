import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-politica-privacidad',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './politica-privacidad.html',
  styleUrl: './politica-privacidad.scss'
})
export class PoliticaPrivacidadComponent {}