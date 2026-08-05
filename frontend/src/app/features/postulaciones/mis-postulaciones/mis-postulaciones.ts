import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import {
  MiPostulacionResponseDTO,
  PostulacionService
} from '../../../core/services/postulacion';
import { TopbarComponent } from '../../../shared/components/topbar/topbar';

@Component({
  selector: 'app-mis-postulaciones',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './mis-postulaciones.html',
  styleUrl: './mis-postulaciones.scss'
})
export class MisPostulacionesComponent implements OnInit {

  postulaciones: MiPostulacionResponseDTO[] = [];
  postulacionSeleccionada: MiPostulacionResponseDTO | null = null;

  paginaActual = 0;
  tamanioPagina = 6;
  totalElementos = 0;
  totalPaginas = 0;
  primeraPagina = true;
  ultimaPagina = true;

  cargando = true;
  mensajeError = '';
  abriendoCvId: number | null = null;

  constructor(
    private postulacionService: PostulacionService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarPostulaciones(0);
  }

  cargarPostulaciones(pagina: number): void {
    if (pagina < 0 || (this.totalPaginas > 0 && pagina >= this.totalPaginas)) {
      return;
    }

    this.cargando = true;
    this.mensajeError = '';

    this.postulacionService
      .listarMisPostulaciones(pagina, this.tamanioPagina)
      .subscribe({
        next: (response) => {
          const data = response.data;

          this.postulaciones = data?.content ?? [];
          this.paginaActual = data?.page ?? 0;
          this.tamanioPagina = data?.size ?? this.tamanioPagina;
          this.totalElementos = data?.totalElements ?? 0;
          this.totalPaginas = data?.totalPages ?? 0;
          this.primeraPagina = data?.first ?? true;
          this.ultimaPagina = data?.last ?? true;
          this.cargando = false;

          if (
            this.postulacionSeleccionada &&
            !this.postulaciones.some(
              item => item.idPostulacion === this.postulacionSeleccionada?.idPostulacion
            )
          ) {
            this.postulacionSeleccionada = null;
          }

          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error cargando mis postulaciones:', error);

          this.cargando = false;
          this.mensajeError =
            error.error?.message ||
            error.error?.mensaje ||
            error.error?.error ||
            'No se pudieron cargar tus postulaciones.';

          this.cdr.detectChanges();
        }
      });
  }

  get paginasVisibles(): number[] {
    if (this.totalPaginas <= 1) {
      return [];
    }

    const maximo = 5;
    let inicio = Math.max(0, this.paginaActual - 2);
    let fin = Math.min(this.totalPaginas - 1, inicio + maximo - 1);

    inicio = Math.max(0, fin - maximo + 1);

    return Array.from(
      { length: fin - inicio + 1 },
      (_, indice) => inicio + indice
    );
  }

  abrirDetalle(postulacion: MiPostulacionResponseDTO): void {
    this.postulacionSeleccionada = postulacion;
    document.body.style.overflow = 'hidden';
  }

  cerrarDetalle(): void {
    this.postulacionSeleccionada = null;
    document.body.style.overflow = '';
  }

  abrirCv(postulacion: MiPostulacionResponseDTO): void {
    if (!postulacion.cvDisponible || this.abriendoCvId !== null) {
      return;
    }

    this.mensajeError = '';
    this.abriendoCvId = postulacion.idPostulacion;
    this.cdr.detectChanges();

    this.postulacionService.descargarCv(postulacion.idPostulacion).subscribe({
      next: (blob) => {
        const archivo = new Blob([blob], { type: 'application/pdf' });
        const url = URL.createObjectURL(archivo);

        window.open(url, '_blank', 'noopener,noreferrer');

        setTimeout(() => {
          URL.revokeObjectURL(url);
        }, 60000);

        this.abriendoCvId = null;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error abriendo el CV utilizado:', error);

        this.abriendoCvId = null;
        this.mensajeError =
          error.error?.message ||
          error.error?.mensaje ||
          'No se pudo abrir el CV utilizado en esta postulación.';

        this.cdr.detectChanges();
      }
    });
  }

  verPerfilEmpleador(postulacion: MiPostulacionResponseDTO): void {
    if (!postulacion.idEmpleador) {
      return;
    }

    this.cerrarDetalle();
    this.router.navigate([
      '/empleador/perfil-publico',
      postulacion.idEmpleador
    ]);
  }

  explorarOfertas(): void {
    this.cerrarDetalle();
    this.router.navigate(['/ofertas']);
  }

  volverPerfil(): void {
    this.cerrarDetalle();
    this.router.navigate(['/empleado/perfil']);
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
        return estado || 'Sin estado';
    }
  }

  obtenerClaseEstado(estado: string): string {
    switch (estado) {
      case 'ACEPTADA':
        return 'preseleccionado';
      case 'RECHAZADA':
        return 'no-seleccionado';
      default:
        return 'pendiente';
    }
  }

  obtenerTextoDisponibilidadOferta(postulacion: MiPostulacionResponseDTO): string {
    if (!postulacion.ofertaFinalizada) {
      return 'Oferta vigente';
    }

    if (postulacion.ofertaVencida) {
      return 'Postulación cerrada por fecha límite';
    }

    return 'Oferta finalizada';
  }

  formatearFecha(fecha: string | null | undefined): string {
    if (!fecha) {
      return 'No registrada';
    }

    const partes = fecha.split('-');

    if (partes.length !== 3) {
      return fecha;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }
}
