import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { OfertaService, FiltrosOfertas, SimpleDTO } from 'src/app/core/services/oferta.service';
import { PostulacionService } from 'src/app/core/services/postulacion';
import { SuscripcionService } from 'src/app/core/services/suscripcion';
import { UsoPlanUsuario } from 'src/app/core/models/suscripcion';
import { Oferta } from 'src/app/core/models/oferta';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-ofertas',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './ofertas.html',
  styleUrls: ['./ofertas.scss']
})
export class OfertasComponent implements OnInit {

  ofertas: Oferta[] = [];

  keyword = '';
  ubicacion = '';

  categoriaSeleccionada = 0;
  modalidadSeleccionada = 0;
  experienciaSeleccionada = 0;

  montoMin?: number;
  montoMax?: number;

  sortBy = '';
  order = 'asc';

  categorias: SimpleDTO[] = [];
  modalidades: SimpleDTO[] = [];
  experiencias: SimpleDTO[] = [];

  cargando = false;
  mensajeError = '';

  drawerOpen = false;
  selectedOferta: Oferta | null = null;

  modoParaTi = false;

  ofertasPostuladas = new Set<number>();

  miUso?: UsoPlanUsuario;
  cargandoUsoPlan = false;

  constructor(
    private ofertaService: OfertaService,
    private postulacionService: PostulacionService,
    private suscripcionService: SuscripcionService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.modoParaTi = this.route.snapshot.data['modo'] === 'para-ti';

    this.cargarFiltros();
    this.cargarOfertasPostuladas();
    this.cargarMiUsoPlan();

    if (this.modoParaTi) {
      this.cargarRecomendaciones();
    } else {
      this.cargarOfertas();
    }
  }

  get tituloPagina(): string {
    return this.modoParaTi ? 'Ofertas para ti' : 'Ofertas laborales';
  }

  get descripcionPagina(): string {
    if (this.modoParaTi) {
      return 'Recomendaciones basadas en las categorías de trabajo de tu perfil.';
    }

    return 'Explora oportunidades publicadas por empleadores y filtra según lo que estás buscando.';
  }

  cargarOfertas(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.ofertaService.getOfertas().subscribe({
      next: (data: Oferta[]) => {
        this.ofertas = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error cargando ofertas:', err);
        this.mensajeError = 'No se pudieron cargar las ofertas laborales.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarRecomendaciones(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.ofertaService.getOfertasParaTi().subscribe({
      next: (response) => {
        this.ofertas = response.data || [];
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error cargando recomendaciones:', err);
        this.mensajeError = 'No se pudieron cargar tus recomendaciones.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarFiltros(): void {
    this.ofertaService.getFiltros().subscribe({
      next: (data) => {
        this.categorias = data.categorias || [];
        this.modalidades = data.modalidades || [];
        this.experiencias = data.experiencias || [];
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error cargando filtros:', err);
      }
    });
  }

  cargarOfertasPostuladas(): void {
    this.postulacionService.listarMisOfertasPostuladas().subscribe({
      next: (response) => {
        this.ofertasPostuladas = new Set(response.data || []);
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando ofertas postuladas:', error);
      }
    });
  }

  yaPostulo(idOferta: number): boolean {
    return this.ofertasPostuladas.has(idOferta);
  }

  get limitePostulacionesAlcanzado(): boolean {
    const restantes = this.miUso?.postulacionesRestantes;

    return restantes !== null && restantes !== undefined && restantes <= 0;
  }

  get detalleUsoPostulaciones(): string {
    if (!this.miUso || this.miUso.maxPostulacionesMes === null || this.miUso.maxPostulacionesMes === undefined) {
      return 'Tu plan actual permite postular sin límite mensual.';
    }

    return `Has usado ${this.miUso.postulacionesUsadas} de ${this.miUso.maxPostulacionesMes} postulaciones mensuales.`;
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

  filtrar(): void {
    const filtros: FiltrosOfertas = {};

    if (this.keyword.trim()) {
      filtros.palabraClave = this.keyword.trim();
    }

    if (this.ubicacion.trim()) {
      filtros.ubicacion = this.ubicacion.trim();
    }

    if (this.categoriaSeleccionada) {
      filtros.categoria = this.categoriaSeleccionada;
    }

    if (this.modalidadSeleccionada) {
      filtros.modalidad = this.modalidadSeleccionada;
    }

    if (this.experienciaSeleccionada) {
      filtros.experiencia = this.experienciaSeleccionada;
    }

    if (this.montoMin !== undefined && this.montoMin !== null) {
      filtros.montoMin = this.montoMin;
    }

    if (this.montoMax !== undefined && this.montoMax !== null) {
      filtros.montoMax = this.montoMax;
    }

    if (this.sortBy) {
      filtros.sortBy = this.sortBy;
      filtros.order = this.order;
    }

    this.cargando = true;
    this.mensajeError = '';

    this.ofertaService.filtrarOfertas(filtros).subscribe({
      next: (data: Oferta[]) => {
        this.ofertas = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Error filtrando ofertas:', err);
        this.mensajeError = 'No se pudieron aplicar los filtros.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  aplicarOrden(sortBy: string, order: string): void {
    this.sortBy = sortBy;
    this.order = order;

    if (this.modoParaTi) {
      this.ordenarOfertasLocales();
      return;
    }

    this.filtrar();
  }

  ordenarOfertasLocales(): void {
    if (this.sortBy === 'fecha') {
      this.ofertas = [...this.ofertas].sort((a, b) => {
        const fechaA = new Date(a.fechaPublicacion).getTime();
        const fechaB = new Date(b.fechaPublicacion).getTime();

        return this.order === 'asc' ? fechaA - fechaB : fechaB - fechaA;
      });
    }

    if (this.sortBy === 'monto') {
      this.ofertas = [...this.ofertas].sort((a, b) => {
        return this.order === 'asc'
          ? a.montoTotal - b.montoTotal
          : b.montoTotal - a.montoTotal;
      });
    }

    this.cdr.detectChanges();
  }

  limpiarFiltros(): void {
    this.keyword = '';
    this.ubicacion = '';

    this.categoriaSeleccionada = 0;
    this.modalidadSeleccionada = 0;
    this.experienciaSeleccionada = 0;

    this.montoMin = undefined;
    this.montoMax = undefined;

    this.sortBy = '';
    this.order = 'asc';

    if (this.modoParaTi) {
      this.cargarRecomendaciones();
    } else {
      this.cargarOfertas();
    }
  }

  irAPerfilEmpleado(): void {
    this.router.navigate(['/empleado/perfil']);
  }

  irARecomendaciones(): void {
    this.router.navigate(['/ofertas/para-ti']);
  }

  irATodasLasOfertas(): void {
    this.router.navigate(['/ofertas']);
  }

  irAEditarPerfil(): void {
    this.router.navigate(['/empleado/editar-perfil']);
  }

  irSuscripciones(): void {
    this.router.navigate(['/suscripciones']);
  }

  openDetail(ofertaId: number): void {
    this.ofertaService.getOfertaById(ofertaId).subscribe({
      next: (oferta: Oferta) => {
        this.selectedOferta = oferta;
        this.drawerOpen = false;

        document.body.style.overflow = 'hidden';
        this.cdr.detectChanges();

        setTimeout(() => {
          this.drawerOpen = true;
          this.cdr.detectChanges();
        }, 20);
      },
      error: (err: any) => {
        console.error('Error cargando detalle de oferta:', err);
      }
    });
  }

  apply(ofertaId: number): void {
    this.openDetail(ofertaId);
  }

  closeDrawer(): void {
    this.drawerOpen = false;
    document.body.style.overflow = '';
    this.cdr.detectChanges();

    setTimeout(() => {
      this.selectedOferta = null;
      this.cdr.detectChanges();
    }, 220);
  }

  applyNow(): void {
    if (!this.selectedOferta) {
      return;
    }

    const idOferta = this.selectedOferta.id;

    if (this.yaPostulo(idOferta) || this.limitePostulacionesAlcanzado) {
      return;
    }

    this.drawerOpen = false;
    document.body.style.overflow = '';
    this.cdr.detectChanges();

    setTimeout(() => {
      this.selectedOferta = null;

      this.router.navigate([
        '/postulaciones/ofertas',
        idOferta,
        'postular'
      ]);

      this.cdr.detectChanges();
    }, 220);
  }

  openInNewWindow(): void {
    if (this.selectedOferta) {
      const url = `/ofertas/${this.selectedOferta.id}`;
      window.open(url, '_blank');
    }
  }
}