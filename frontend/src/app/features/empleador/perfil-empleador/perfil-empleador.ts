import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { EmpleadorService } from 'src/app/core/services/empleador';
import { SuscripcionService } from 'src/app/core/services/suscripcion';
import { UsoPlanUsuario } from 'src/app/core/models/suscripcion';
import { EmpleadorResponse } from 'src/app/core/models/empleador';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-perfil-empleador',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './perfil-empleador.html',
  styleUrl: './perfil-empleador.scss'
})
export class PerfilEmpleadorComponent implements OnInit {

  perfil?: EmpleadorResponse;

  cargando = true;
  mensajeError = '';
  cargandoUsoPlan = false;

  miUso?: UsoPlanUsuario;

  tabActiva: 'finalizados' | 'activos' = 'finalizados';

  constructor(
    private empleadorService: EmpleadorService,
    private suscripcionService: SuscripcionService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.obtenerPerfil();
    this.cargarMiUsoPlan();
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

  get limiteOfertasAlcanzado(): boolean {
    const restantes = this.miUso?.ofertasRestantes;

    return restantes !== null && restantes !== undefined && restantes <= 0;
  }

  get detalleUsoOfertas(): string {
    if (!this.miUso || this.miUso.maxOfertasActivas === null || this.miUso.maxOfertasActivas === undefined) {
      return 'Tu plan actual permite publicar ofertas activas sin límite.';
    }

    return `Tienes ${this.miUso.ofertasPublicadas} de ${this.miUso.maxOfertasActivas} ofertas activas permitidas.`;
  }

  cargarMiUsoPlan(): void {
    this.cargandoUsoPlan = true;

    this.suscripcionService.obtenerMiUso().subscribe({
      next: (uso) => {
        this.miUso = uso;
        this.cargandoUsoPlan = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando uso del plan:', error);
        this.cargandoUsoPlan = false;
        this.cdr.detectChanges();
      }
    });
  }

  crearOferta(): void {
    if (this.limiteOfertasAlcanzado) {
      return;
    }

    this.router.navigate(['/empleador/ofertas/crear']);
  }

  irSuscripciones(): void {
    this.router.navigate(['/suscripciones']);
  }

  editarPerfil(): void {
    this.router.navigate(['/empleador/editar-perfil']);
  }

  verMisOfertas(): void {
    this.router.navigate(['/empleador/mis-ofertas']);
  }

  obtenerUrlLogo(): string {
    if (!this.perfil?.logoEmpleador || !this.perfil.idEmpleador) {
      return '';
    }

    return this.empleadorService.obtenerUrlLogoPublico(
      this.perfil.idEmpleador,
      this.perfil.logoEmpleador
    );
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