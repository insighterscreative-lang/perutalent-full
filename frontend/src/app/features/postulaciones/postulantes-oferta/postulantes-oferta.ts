import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import {
  PostulacionResponseDTO,
  PostulacionService
} from '../../../core/services/postulacion';

import { TopbarComponent } from '../../../shared/components/topbar/topbar';

interface OpcionFiltro {
  id: number;
  nombre: string;
}

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
  mensajeError = '';
  mensajeExito = '';

  accionandoId: number | null = null;

  filtroEstado = 'TODOS';
  filtroNombre = '';
  filtroDistritoId = '';
  filtroModalidadId = '';
  filtroHabilidadId = '';
  filtroHerramientaId = '';

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

    this.cargarPostulantes();
  }

  cargarPostulantes(): void {
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cdr.detectChanges();

    this.postulacionService.listarPostulantesPorOferta(this.idOferta).subscribe({
      next: (response) => {
        this.postulantes = response.data || [];
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

  get postulantesFiltrados(): PostulacionResponseDTO[] {
    return this.postulantes.filter((postulante) => {
      if (this.filtroEstado !== 'TODOS' && postulante.estadoPostulacion !== this.filtroEstado) {
        return false;
      }

      const textoBusqueda = this.normalizarTexto(
        `${postulante.nombreEmpleado} ${postulante.apellidoEmpleado} ${postulante.emailEmpleado}`
      );

      if (this.filtroNombre.trim()) {
        const nombreBuscado = this.normalizarTexto(this.filtroNombre);

        if (!textoBusqueda.includes(nombreBuscado)) {
          return false;
        }
      }

      if (this.filtroDistritoId) {
        const idDistrito = Number(this.filtroDistritoId);

        if (postulante.idDistrito !== idDistrito) {
          return false;
        }
      }

      if (this.filtroModalidadId) {
        const idModalidad = Number(this.filtroModalidadId);

        if (!(postulante.modalidadIds || []).includes(idModalidad)) {
          return false;
        }
      }

      if (this.filtroHabilidadId) {
        const idHabilidad = Number(this.filtroHabilidadId);

        if (!(postulante.habilidadIds || []).includes(idHabilidad)) {
          return false;
        }
      }

      if (this.filtroHerramientaId) {
        const idHerramienta = Number(this.filtroHerramientaId);

        if (!(postulante.herramientaIds || []).includes(idHerramienta)) {
          return false;
        }
      }

      return true;
    });
  }

  get distritosDisponibles(): OpcionFiltro[] {
    const opciones = this.postulantes
      .filter((postulante) => postulante.idDistrito && postulante.distrito)
      .map((postulante) => ({
        id: postulante.idDistrito as number,
        nombre: postulante.distrito as string
      }));

    return this.obtenerOpcionesUnicas(opciones);
  }

  get modalidadesDisponibles(): OpcionFiltro[] {
    return this.obtenerOpcionesDesdeListas('modalidadIds', 'modalidades');
  }

  get habilidadesDisponibles(): OpcionFiltro[] {
    return this.obtenerOpcionesDesdeListas('habilidadIds', 'habilidades');
  }

  get herramientasDisponibles(): OpcionFiltro[] {
    return this.obtenerOpcionesDesdeListas('herramientaIds', 'herramientas');
  }

  limpiarFiltros(): void {
    this.filtroEstado = 'TODOS';
    this.filtroNombre = '';
    this.filtroDistritoId = '';
    this.filtroModalidadId = '';
    this.filtroHabilidadId = '';
    this.filtroHerramientaId = '';
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
      next: (response) => {
        const postulacionActualizada = response.data;

        this.postulantes = this.postulantes.map((item) =>
          item.idPostulacion === postulacionActualizada.idPostulacion
            ? postulacionActualizada
            : item
        );

        this.mensajeExito =
          nuevoEstado === 'ACEPTADA'
            ? 'Postulación aceptada exitosamente.'
            : 'Postulación rechazada exitosamente.';

        this.accionandoId = null;
        this.cdr.detectChanges();
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

  private obtenerOpcionesDesdeListas(
    campoIds: 'modalidadIds' | 'habilidadIds' | 'herramientaIds',
    campoNombres: 'modalidades' | 'habilidades' | 'herramientas'
  ): OpcionFiltro[] {
    const opciones: OpcionFiltro[] = [];

    this.postulantes.forEach((postulante) => {
      const ids = postulante[campoIds] || [];
      const nombres = postulante[campoNombres] || [];

      ids.forEach((id, index) => {
        const nombre = nombres[index];

        if (id && nombre) {
          opciones.push({ id, nombre });
        }
      });
    });

    return this.obtenerOpcionesUnicas(opciones);
  }

  private obtenerOpcionesUnicas(opciones: OpcionFiltro[]): OpcionFiltro[] {
    const mapa = new Map<number, OpcionFiltro>();

    opciones.forEach((opcion) => {
      if (!mapa.has(opcion.id)) {
        mapa.set(opcion.id, opcion);
      }
    });

    return Array.from(mapa.values())
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }

  private normalizarTexto(texto: string): string {
    return texto
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }
}