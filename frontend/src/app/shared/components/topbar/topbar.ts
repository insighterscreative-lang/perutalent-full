import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';

import { LogoComponent } from '../logo/logo';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, LogoComponent],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss'
})
export class TopbarComponent {

  menuAbierto = false;

  constructor(private router: Router) {}

  toggleMenu(): void {
    this.menuAbierto = !this.menuAbierto;
  }

  irEditarCuenta(): void {
    this.menuAbierto = false;

    // Esta ruta la podemos crear luego.
    // Por ahora puedes mandarlo a ofertas o dejarla preparada.
    this.router.navigate(['/cuenta/editar']);
  }

  irSuscripciones(): void {
    this.menuAbierto = false;
    this.router.navigate(['/suscripciones']);
  }
    
  cerrarSesion(): void {
    localStorage.clear();
    this.menuAbierto = false;
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  cerrarMenuSiClickFuera(event: MouseEvent): void {
    const target = event.target as HTMLElement;

    if (!target.closest('.user-menu')) {
      this.menuAbierto = false;
    }
  }
}