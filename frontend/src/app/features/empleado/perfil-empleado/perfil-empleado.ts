import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { EmpleadoService } from 'src/app/core/services/empleado';
import { EmpleadoResponse } from 'src/app/core/models/empleado';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-perfil-empleado',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './perfil-empleado.html',
  styleUrl: './perfil-empleado.scss'
})
export class PerfilEmpleadoComponent implements OnInit {

  perfil?: EmpleadoResponse;

  cargando = true;
  mensajeError = '';

  tabActiva: 'finalizados' | 'activos' = 'finalizados';

  constructor(
    private empleadoService: EmpleadoService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.obtenerPerfil();
  }

  obtenerPerfil(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.empleadoService.obtenerPerfil().subscribe({
      next: (response) => {
        this.perfil = response.data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener perfil:', error);

        this.cargando = false;

        if (error.status === 404) {
          this.router.navigate(['/empleado/crear-perfil']);
          this.cdr.detectChanges();
          return;
        }

        if (error.status === 401 || error.status === 403) {
          this.mensajeError = 'No estás autorizado. Inicia sesión nuevamente.';
          this.cdr.detectChanges();
          return;
        }

        this.mensajeError = error?.error?.message || 'No se pudo cargar el perfil';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarTab(tab: 'finalizados' | 'activos'): void {
    this.tabActiva = tab;
  }

  editarPerfil(): void {
    this.router.navigate(['/empleado/editar-perfil']);
  }

  verOfertas(): void {
    this.router.navigate(['/ofertas']);
  }

  verRecomendaciones(): void {
    this.router.navigate(['/ofertas/para-ti']);
  }

  descargarCV(): void {
    if (!this.perfil?.curriculum) {
      return;
    }

    window.open(this.perfil.curriculum, '_blank');
  }

  obtenerIniciales(): string {
    if (!this.perfil) {
      return 'UP';
    }

    const nombre = this.perfil.nombre?.charAt(0) || '';
    const apellido = this.perfil.apellido?.charAt(0) || '';

    return `${nombre}${apellido}`.toUpperCase();
  }

  obtenerDisponibilidadEquipo(): string[] {
    if (!this.perfil?.disponibilidadEquipo) {
      return [];
    }

    return this.perfil.disponibilidadEquipo
      .split(',')
      .map(item => item.trim())
      .filter(item => item.length > 0);
  }
}