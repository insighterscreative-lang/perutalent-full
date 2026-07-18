import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { EmpleadorService } from 'src/app/core/services/empleador';
import { EmpleadorResponse } from 'src/app/core/models/empleador';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-perfil-empleador',
  standalone: true,
  imports: [CommonModule, RouterLink, TopbarComponent],
  templateUrl: './perfil-empleador.html',
  styleUrl: './perfil-empleador.scss'
})
export class PerfilEmpleadorComponent implements OnInit {

  perfil?: EmpleadorResponse;

  cargando = true;
  mensajeError = '';

  tabActiva: 'finalizados' | 'activos' = 'finalizados';

  constructor(
    private empleadorService: EmpleadorService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.obtenerPerfil();
  }

  obtenerPerfil(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.empleadorService.obtenerPerfil().subscribe({
      next: (response) => {
        this.perfil = response.data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener perfil empleador:', error);

        this.cargando = false;

        if (error.status === 404) {
          this.router.navigate(['/empleador/crear-perfil']);
          this.cdr.detectChanges();
          return;
        }

        if (error.status === 401 || error.status === 403) {
          this.mensajeError = 'No estás autorizado. Inicia sesión nuevamente.';
          this.cdr.detectChanges();
          return;
        }

        this.mensajeError = error?.error?.message || 'No se pudo cargar el perfil del empleador';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarTab(tab: 'finalizados' | 'activos'): void {
    this.tabActiva = tab;
  }

  editarPerfil(): void {
    this.router.navigate(['/empleador/editar-perfil']);
  }

  verMisOfertas(): void {
    this.router.navigate(['/empleador/mis-ofertas']);
  }

  visitarSitioWeb(): void {
    if (!this.perfil?.sitioWeb) {
      return;
    }

    let url = this.perfil.sitioWeb.trim();

    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = `https://${url}`;
    }

    window.open(url, '_blank');
  }

  obtenerIniciales(): string {
    if (!this.perfil?.nombreComercial) {
      return 'EP';
    }

    return this.perfil.nombreComercial
      .split(' ')
      .slice(0, 2)
      .map(palabra => palabra.charAt(0))
      .join('')
      .toUpperCase();
  }

  tieneContactoOpcional(): boolean {
    return !!(
      this.perfil?.sitioWeb ||
      this.perfil?.correoContacto ||
      this.perfil?.telefonoContacto
    );
  }
}