import { CommonModule } from '@angular/common';
import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth';
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

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  get esEmpleado(): boolean {
    return this.authService.tieneRol('ROLE_EMPLEADO');
  }

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

  irMisPostulaciones(): void {
    this.menuAbierto = false;
    this.router.navigate(['/empleado/mis-postulaciones']);
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