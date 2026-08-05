import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  FiltrosPostulantesRequest,
  FiltrosPostulantesResponse,
  OpcionFiltroPostulante,
  PostulacionResponseDTO,
  PostulacionService
} from '../../../core/services/postulacion';

import { TopbarComponent } from '../../../shared/components/topbar/topbar';

@Component({
  selector: 'app-postulantes-oferta',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './postulantes-oferta.html',
  styleUrl: './postulantes-oferta.scss'
})
export class PostulantesOfertaComponent implements OnInit {

  idOferta!: number;

  postulantes: PostulacionResponseDTO[] = [];

  cargando = false;
  cargandoFiltros = false;
  mensajeError = '';
  mensajeExito = '';

  accionandoId: number | null = null;

  filtroEstado = 'TODOS';
  filtroNombre = '';
  filtroDistritoId = '';
  filtroModalidadId = '';
  filtroHabilidadId = '';
  filtroHerramientaId = '';

  distritosDisponibles: OpcionFiltroPostulante[] = [];
  modalidadesDisponibles: OpcionFiltroPostulante[] = [];
  habilidadesDisponibles: OpcionFiltroPostulante[] = [];
  herramientasDisponibles: OpcionFiltroPostulante[] = [];

  paginaActual = 0;
  tamanoPagina = 8;
  totalElementos = 0;
  totalPaginas = 0;
  primeraPagina = true;
  ultimaPagina = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postulacionService: PostulacionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.idOferta = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.idOferta) {
      this.mensajeError = 'No se encontró la oferta laboral.';
      this.cargando = false;
      this.cdr.detectChanges();
      return;
    }

    this.cargarFiltrosDisponibles();
    this.cargarPostulantes(0);
  }

  cargarPostulantes(page = 0): void {
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cdr.detectChanges();

    this.postulacionService.listarPostulantesPorOferta(
      this.idOferta,
      this.construirFiltros(),
      page,
      this.tamanoPagina
    ).subscribe({
      next: (response) => {
        const pagina = response.data;

        this.postulantes = pagina?.content || [];
        this.paginaActual = pagina?.page || 0;
        this.totalElementos = pagina?.totalElements || 0;
        this.totalPaginas = pagina?.totalPages || 0;
        this.primeraPagina = pagina?.first ?? true;
        this.ultimaPagina = pagina?.last ?? true;

        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando postulantes:', error);

        this.mensajeError =
          error.error?.message ||
          error.error?.mensaje ||
          error.error?.error ||
          'No se pudieron cargar los postulantes de esta oferta.';

        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarFiltrosDisponibles(): void {
    this.cargandoFiltros = true;

    this.postulacionService.listarFiltrosPostulantes(this.idOferta).subscribe({
      next: (response) => {
        const filtros: FiltrosPostulantesResponse = response.data;

        this.distritosDisponibles = filtros?.distritos || [];
        this.modalidadesDisponibles = filtros?.modalidades || [];
        this.habilidadesDisponibles = filtros?.habilidades || [];
        this.herramientasDisponibles = filtros?.herramientas || [];
        this.cargandoFiltros = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando filtros de postulantes:', error);
        this.cargandoFiltros = false;
        this.cdr.detectChanges();
      }
    });
  }

  aplicarFiltros(): void {
    this.cargarPostulantes(0);
  }

  limpiarFiltros(): void {
    this.filtroEstado = 'TODOS';
    this.filtroNombre = '';
    this.filtroDistritoId = '';
    this.filtroModalidadId = '';
    this.filtroHabilidadId = '';
    this.filtroHerramientaId = '';
    this.cargarPostulantes(0);
  }

  cambiarPagina(nuevaPagina: number): void {
    if (
      this.cargando ||
      nuevaPagina < 0 ||
      nuevaPagina >= this.totalPaginas ||
      nuevaPagina === this.paginaActual
    ) {
      return;
    }

    this.cargarPostulantes(nuevaPagina);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  get paginaVisible(): number {
    return this.totalPaginas === 0 ? 0 : this.paginaActual + 1;
  }

  aceptarPostulacion(postulante: PostulacionResponseDTO): void {
    this.cambiarEstadoPostulacion(postulante, 'ACEPTADA');
  }

  rechazarPostulacion(postulante: PostulacionResponseDTO): void {
    this.cambiarEstadoPostulacion(postulante, 'RECHAZADA');
  }

  esPremium(postulante: PostulacionResponseDTO): boolean {
    return Boolean(postulante.empleadoPremium) ||
      postulante.planEmpleado?.toUpperCase() === 'PREMIUM';
  }

  obtenerTextoPlan(postulante: PostulacionResponseDTO): string {
    if (this.esPremium(postulante)) {
      return 'Premium';
    }

    return postulante.planEmpleado || 'Gratuito';
  }

  obtenerTextoEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'Pendiente de revisión';
      case 'ACEPTADA':
        return 'Preseleccionado';
      case 'RECHAZADA':
        return 'No seleccionado';
      default:
        return estado;
    }
  }

  private cambiarEstadoPostulacion(
    postulante: PostulacionResponseDTO,
    nuevoEstado: 'ACEPTADA' | 'RECHAZADA'
  ): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.accionandoId = postulante.idPostulacion;
    this.cdr.detectChanges();

    const accion$ =
      nuevoEstado === 'ACEPTADA'
        ? this.postulacionService.aceptarPostulacion(postulante.idPostulacion)
        : this.postulacionService.rechazarPostulacion(postulante.idPostulacion);

    accion$.subscribe({
      next: () => {
        this.mensajeExito =
          nuevoEstado === 'ACEPTADA'
            ? 'Postulante preseleccionado exitosamente.'
            : 'Postulación marcada como no seleccionada.';

        this.accionandoId = null;
        this.cargarPostulantes(this.paginaActual);
      },
      error: (error) => {
        console.error('Error cambiando estado de postulación:', error);

        this.mensajeError =
          error.error?.message ||
          error.error?.mensaje ||
          error.error?.error ||
          'No se pudo actualizar el estado de la postulación.';

        this.accionandoId = null;
        this.cdr.detectChanges();
      }
    });
  }

  volverAMisOfertas(): void {
    this.router.navigate(['/empleador/mis-ofertas']);
  }

  verCv(postulante: PostulacionResponseDTO): void {
    this.mensajeError = '';

    this.postulacionService.descargarCv(postulante.idPostulacion).subscribe({
      next: (blob) => {
        const file = new Blob([blob], { type: 'application/pdf' });
        const fileURL = URL.createObjectURL(file);

        window.open(fileURL, '_blank');
      },
      error: (error) => {
        console.error('Error al abrir CV:', error);

        this.mensajeError = 'No se pudo abrir el CV del postulante.';
        this.cdr.detectChanges();
      }
    });
  }

  verPerfilEmpleado(postulante: PostulacionResponseDTO): void {
    this.router.navigate([
      '/empleado/perfil-publico',
      postulante.idEmpleado
    ]);
  }

  private construirFiltros(): FiltrosPostulantesRequest {
    return {
      estado: this.filtroEstado,
      texto: this.filtroNombre.trim(),
      distritoId: this.convertirId(this.filtroDistritoId),
      modalidadId: this.convertirId(this.filtroModalidadId),
      habilidadId: this.convertirId(this.filtroHabilidadId),
      herramientaId: this.convertirId(this.filtroHerramientaId)
    };
  }

  private convertirId(valor: string): number | undefined {
    const id = Number(valor);
    return Number.isFinite(id) && id > 0 ? id : undefined;
  }
}
