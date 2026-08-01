import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { EmpleadorService } from 'src/app/core/services/empleador';
import { SuscripcionService } from 'src/app/core/services/suscripcion';
import { UsoPlanUsuario } from 'src/app/core/models/suscripcion';
import { EmpleadorResponse, TrabajoPerfil } from 'src/app/core/models/empleador';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-mis-ofertas-empleador',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './mis-ofertas-empleador.html',
  styleUrl: './mis-ofertas-empleador.scss'
})
export class MisOfertasEmpleadorComponent implements OnInit {

  perfil?: EmpleadorResponse;

  cargando = true;
  mensajeError = '';
  cargandoUsoPlan = false;

  miUso?: UsoPlanUsuario;

  tabActiva: 'activos' | 'finalizados' = 'activos';

  drawerOpen = false;
  ofertaSeleccionada: TrabajoPerfil | null = null;

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
        console.error('Error cargando ofertas del empleador:', error);

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

        this.mensajeError = error?.error?.message || 'No se pudieron cargar tus ofertas.';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarTab(tab: 'activos' | 'finalizados'): void {
    this.tabActiva = tab;
  }

  get ofertasActivas(): TrabajoPerfil[] {
    return this.perfil?.trabajosActivosDetalle || [];
  }

  get ofertasFinalizadas(): TrabajoPerfil[] {
    return this.perfil?.trabajosFinalizadosDetalle || [];
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

  verDetalle(oferta: TrabajoPerfil): void {
    this.ofertaSeleccionada = oferta;
    this.drawerOpen = false;

    document.body.style.overflow = 'hidden';
    this.cdr.detectChanges();

    setTimeout(() => {
      this.drawerOpen = true;
      this.cdr.detectChanges();
    }, 20);
  }

  cerrarDetalle(): void {
    this.drawerOpen = false;
    document.body.style.overflow = '';
    this.cdr.detectChanges();

    setTimeout(() => {
      this.ofertaSeleccionada = null;
      this.cdr.detectChanges();
    }, 250);
  }

  editarOferta(oferta: TrabajoPerfil): void {
    this.router.navigate(['/empleador/ofertas/editar', oferta.idOferta]);
  }

  verPostulantes(oferta: TrabajoPerfil): void {
    this.drawerOpen = false;
    document.body.style.overflow = '';
    this.cdr.detectChanges();

    const idOferta = oferta.idOferta;

    setTimeout(() => {
      this.ofertaSeleccionada = null;

      this.router.navigate([
        '/postulaciones/ofertas',
        idOferta,
        'postulantes'
      ]);

      this.cdr.detectChanges();
    }, 250);
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

  volverPerfil(): void {
    this.router.navigate(['/empleador/perfil']);
  }

  puedeEditar(oferta: TrabajoPerfil): boolean {
    return oferta.estadoOferta === 'ABIERTA';
  }
}