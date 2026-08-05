import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { SuscripcionService } from 'src/app/core/services/suscripcion';
import { OfertaService, PaginaResponse } from 'src/app/core/services/oferta.service';
import { UsoPlanUsuario } from 'src/app/core/models/suscripcion';
import { TrabajoPerfil } from 'src/app/core/models/empleador';
import { Oferta } from 'src/app/core/models/oferta';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-mis-ofertas-empleador',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './mis-ofertas-empleador.html',
  styleUrl: './mis-ofertas-empleador.scss'
})
export class MisOfertasEmpleadorComponent implements OnInit {

  cargando = true;
  mensajeError = '';
  mensajeExito = '';
  cargandoUsoPlan = false;
  gestionandoOferta = false;

  miUso?: UsoPlanUsuario;

  tabActiva: 'activos' | 'finalizados' = 'activos';

  ofertasActivasPagina: TrabajoPerfil[] = [];
  ofertasFinalizadasPagina: TrabajoPerfil[] = [];
  paginaActivos = 0;
  paginaFinalizados = 0;
  totalPaginasActivos = 0;
  totalPaginasFinalizados = 0;
  totalActivos = 0;
  totalFinalizados = 0;
  tamanoPagina = 6;
  cargandoOfertas = false;

  drawerOpen = false;
  ofertaSeleccionada: TrabajoPerfil | null = null;

  constructor(
    private suscripcionService: SuscripcionService,
    private ofertaService: OfertaService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarResumenYOfertas();
    this.cargarMiUsoPlan();
  }

  cargarResumenYOfertas(): void {
    this.cargando = true;
    this.cargandoOfertas = true;
    this.mensajeError = '';

    forkJoin({
      activas: this.ofertaService.getMisOfertas('ACTIVAS', 0, this.tamanoPagina),
      finalizadas: this.ofertaService.getMisOfertas('FINALIZADAS', 0, this.tamanoPagina)
    }).subscribe({
      next: ({ activas, finalizadas }) => {
        const paginaActivas = activas.data;
        const paginaFinalizadas = finalizadas.data;

        this.ofertasActivasPagina = (paginaActivas.content || []).map((oferta) => this.mapearOferta(oferta));
        this.ofertasFinalizadasPagina = (paginaFinalizadas.content || []).map((oferta) => this.mapearOferta(oferta));

        this.paginaActivos = paginaActivas.page;
        this.totalPaginasActivos = paginaActivas.totalPages;
        this.totalActivos = paginaActivas.totalElements;

        this.paginaFinalizados = paginaFinalizadas.page;
        this.totalPaginasFinalizados = paginaFinalizadas.totalPages;
        this.totalFinalizados = paginaFinalizadas.totalElements;

        this.cargando = false;
        this.cargandoOfertas = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando ofertas del empleador:', error);
        this.cargando = false;
        this.cargandoOfertas = false;

        if (error.status === 401 || error.status === 403 || error.status === 404) {
          this.router.navigate(['/empleador/crear-perfil']);
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
    const pagina = tab === 'activos' ? this.paginaActivos : this.paginaFinalizados;
    this.cargarOfertasTab(tab, pagina);
  }

  get ofertasActivas(): TrabajoPerfil[] {
    return this.ofertasActivasPagina;
  }

  get ofertasFinalizadas(): TrabajoPerfil[] {
    return this.ofertasFinalizadasPagina;
  }

  cargarOfertasTab(tab: 'activos' | 'finalizados', page = 0): void {
    this.cargandoOfertas = true;
    this.mensajeError = '';

    const estado = tab === 'activos' ? 'ACTIVAS' : 'FINALIZADAS';

    this.ofertaService.getMisOfertas(estado, page, this.tamanoPagina).subscribe({
      next: (response) => {
        const pagina = response.data;
        const ofertas = (pagina.content || []).map((oferta) => this.mapearOferta(oferta));

        if (tab === 'activos') {
          this.ofertasActivasPagina = ofertas;
          this.paginaActivos = pagina.page;
          this.totalPaginasActivos = pagina.totalPages;
          this.totalActivos = pagina.totalElements;
        } else {
          this.ofertasFinalizadasPagina = ofertas;
          this.paginaFinalizados = pagina.page;
          this.totalPaginasFinalizados = pagina.totalPages;
          this.totalFinalizados = pagina.totalElements;
        }

        this.cargandoOfertas = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando ofertas paginadas:', error);
        this.cargandoOfertas = false;
        this.mensajeError = error?.error?.message || 'No se pudieron cargar tus ofertas.';
        this.cdr.detectChanges();
      }
    });
  }

  cambiarPagina(nuevaPagina: number): void {
    const totalPaginas = this.tabActiva === 'activos'
      ? this.totalPaginasActivos
      : this.totalPaginasFinalizados;
    const paginaActual = this.tabActiva === 'activos'
      ? this.paginaActivos
      : this.paginaFinalizados;

    if (this.cargandoOfertas || nuevaPagina < 0 || nuevaPagina >= totalPaginas || nuevaPagina === paginaActual) {
      return;
    }

    this.cargarOfertasTab(this.tabActiva, nuevaPagina);
  }

  get paginaActualVisible(): number {
    const totalPaginas = this.tabActiva === 'activos'
      ? this.totalPaginasActivos
      : this.totalPaginasFinalizados;
    const pagina = this.tabActiva === 'activos'
      ? this.paginaActivos
      : this.paginaFinalizados;

    return totalPaginas === 0 ? 0 : pagina + 1;
  }

  get totalPaginasActual(): number {
    return this.tabActiva === 'activos'
      ? this.totalPaginasActivos
      : this.totalPaginasFinalizados;
  }

  get paginaActualIndice(): number {
    return this.tabActiva === 'activos' ? this.paginaActivos : this.paginaFinalizados;
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

  private refrescarListados(): void {
    this.cargarResumenYOfertas();
  }


  private mapearOferta(oferta: Oferta): TrabajoPerfil {
    return {
      idOferta: oferta.id,
      titulo: oferta.titulo,
      descripcion: oferta.descripcion,
      categoria: oferta.categoria,
      modalidad: oferta.modalidad,
      estadoOferta: oferta.estadoOferta,
      empleador: oferta.empleador,
      distrito: oferta.distrito,
      montoTotal: oferta.montoTotal,
      experiencia: oferta.experiencia,
      tipoDuracion: oferta.tipoDuracion,
      fechaPublicacion: oferta.fechaPublicacion,
      fechaTerminoPostulacion: oferta.fechaTerminoPostulacion,
      habilidades: oferta.habilidades
    };
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

  finalizarOfertaDesdeDetalle(oferta: TrabajoPerfil): void {
    if (!this.puedeEditar(oferta) || this.gestionandoOferta) {
      return;
    }

    if (!confirm('¿Seguro que quieres finalizar esta oferta? Ya no recibirá postulantes nuevos.')) {
      return;
    }

    this.gestionandoOferta = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cdr.detectChanges();

    this.ofertaService.finalizarOferta(oferta.idOferta).subscribe({
      next: () => {
        this.gestionandoOferta = false;
        this.mensajeExito = 'Oferta finalizada exitosamente.';
        this.cerrarDetalle();
        this.refrescarListados();
        this.cargarMiUsoPlan();
      },
      error: (error) => this.manejarErrorGestionOferta(error, 'No se pudo finalizar la oferta.')
    });
  }

  eliminarOferta(oferta: TrabajoPerfil): void {
    if (this.gestionandoOferta) {
      return;
    }

    const mensajeConfirmacion = oferta.estadoOferta === 'ABIERTA'
      ? '¿Seguro que quieres eliminar esta oferta? Dejará de mostrarse y ya no recibirá postulantes.'
      : '¿Seguro que quieres eliminar esta oferta? Dejará de mostrarse en tus listados.';

    if (!confirm(mensajeConfirmacion)) {
      return;
    }

    this.gestionandoOferta = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cdr.detectChanges();

    this.ofertaService.eliminarOferta(oferta.idOferta).subscribe({
      next: () => {
        this.gestionandoOferta = false;
        this.mensajeExito = 'Oferta eliminada exitosamente.';
        this.cerrarDetalle();
        this.refrescarListados();
        this.cargarMiUsoPlan();
      },
      error: (error) => this.manejarErrorGestionOferta(error, 'No se pudo eliminar la oferta.')
    });
  }

  manejarErrorGestionOferta(error: any, mensajeDefault: string): void {
    console.error('Error gestionando oferta:', error);

    this.gestionandoOferta = false;
    this.mensajeExito = '';

    if (error.status === 401 || error.status === 403) {
      this.mensajeError = 'No tienes permiso para realizar esta acción.';
    } else if (error.status === 409 || error.status === 400) {
      this.mensajeError = error.error?.message || mensajeDefault;
    } else if (error.status === 0) {
      this.mensajeError = 'No se pudo conectar con el servidor. Intenta nuevamente.';
    } else {
      this.mensajeError = error.error?.message || mensajeDefault;
    }

    this.cdr.detectChanges();
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

  obtenerTextoEstadoOferta(oferta: TrabajoPerfil): string {
    return oferta.estadoOferta;
  }

  puedeEditar(oferta: TrabajoPerfil): boolean {
    return oferta.estadoOferta === 'ABIERTA';
  }
}
