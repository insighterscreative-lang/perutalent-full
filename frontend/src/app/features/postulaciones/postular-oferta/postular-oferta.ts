import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Oferta } from '../../../core/models/oferta';
import { UsoPlanUsuario } from '../../../core/models/suscripcion';
import { OfertaService } from '../../../core/services/oferta.service';
import { PostulacionService } from '../../../core/services/postulacion';
import { SuscripcionService } from '../../../core/services/suscripcion';

@Component({
  selector: 'app-postular-oferta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './postular-oferta.html',
  styleUrl: './postular-oferta.scss'
})
export class PostularOfertaComponent implements OnInit {

  idOferta!: number;
  oferta?: Oferta;

  usarCvPerfil = false;
  cvSeleccionado: File | null = null;

  mensajeExito = '';
  mensajeError = '';

  cargando = false;
  cargandoOferta = true;
  cargandoUsoPlan = false;

  miUso?: UsoPlanUsuario;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ofertaService: OfertaService,
    private postulacionService: PostulacionService,
    private suscripcionService: SuscripcionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.idOferta = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.idOferta) {
      this.cargandoOferta = false;
      this.mensajeError = 'No se encontró la oferta laboral.';
      return;
    }

    this.cargarOferta();
    this.cargarMiUsoPlan();
  }

  get ofertaVencida(): boolean {
    if (!this.oferta?.fechaTerminoPostulacion) {
      return true;
    }

    return this.oferta.fechaTerminoPostulacion < this.obtenerFechaLocalActual();
  }

  get ofertaDisponible(): boolean {
    return Boolean(this.oferta) &&
      this.oferta?.estadoOferta === 'ABIERTA' &&
      !this.ofertaVencida;
  }

  get limitePostulacionesAlcanzado(): boolean {
    const restantes = this.miUso?.postulacionesRestantes;

    return restantes !== null && restantes !== undefined && restantes <= 0;
  }

  get mostrarUpgradePostulaciones(): boolean {
    if (!this.ofertaDisponible) {
      return false;
    }

    const mensaje = this.normalizarMensaje(this.mensajeError);

    return this.limitePostulacionesAlcanzado ||
      mensaje.includes('limite mensual de postulaciones') ||
      mensaje.includes('has alcanzado el limite mensual');
  }

  get detalleUsoPostulaciones(): string {
    if (!this.miUso || this.miUso.maxPostulacionesMes === null || this.miUso.maxPostulacionesMes === undefined) {
      return 'Tu plan actual permite postular sin límite mensual.';
    }

    return `Has usado ${this.miUso.postulacionesUsadas} de ${this.miUso.maxPostulacionesMes} postulaciones mensuales.`;
  }

  cargarOferta(): void {
    this.cargandoOferta = true;

    this.ofertaService.getOfertaById(this.idOferta).subscribe({
      next: (oferta) => {
        this.oferta = oferta;
        this.cargandoOferta = false;

        if (!this.ofertaDisponible) {
          this.mensajeError = this.ofertaVencida
            ? `El periodo de postulación finalizó el ${oferta.fechaTerminoPostulacion}.`
            : 'Esta oferta ya no se encuentra abierta para postulaciones.';
        }

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando la oferta:', error);

        this.cargandoOferta = false;
        this.mensajeError =
          error.error?.message ||
          error.error?.mensaje ||
          error.error?.error ||
          'No se pudo cargar la oferta laboral.';

        this.cdr.detectChanges();
      }
    });
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

  cambiarOpcionCv(valor: boolean): void {
    this.usarCvPerfil = valor;

    if (valor) {
      this.cvSeleccionado = null;
    }

    this.mensajeError = '';
    this.mensajeExito = '';
  }

  seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      this.cvSeleccionado = null;
      return;
    }

    const archivo = input.files[0];

    if (archivo.type !== 'application/pdf') {
      this.mensajeError = 'El CV debe ser un archivo PDF.';
      this.cvSeleccionado = null;
      input.value = '';
      return;
    }

    this.mensajeError = '';
    this.cvSeleccionado = archivo;
  }

  postular(): void {
    this.mensajeExito = '';
    this.mensajeError = '';

    if (!this.idOferta || !this.oferta) {
      this.mensajeError = 'No se encontró la oferta laboral.';
      return;
    }

    if (!this.ofertaDisponible) {
      this.mensajeError = this.ofertaVencida
        ? `El periodo de postulación finalizó el ${this.oferta.fechaTerminoPostulacion}.`
        : 'Esta oferta ya no se encuentra abierta para postulaciones.';
      return;
    }

    if (this.limitePostulacionesAlcanzado) {
      this.mensajeError = 'Has alcanzado el límite mensual de postulaciones de tu plan actual.';
      return;
    }

    if (!this.usarCvPerfil && !this.cvSeleccionado) {
      this.mensajeError = 'Debes seleccionar un CV en PDF.';
      return;
    }

    this.cargando = true;

    this.postulacionService.postular(
      this.idOferta,
      this.usarCvPerfil,
      this.cvSeleccionado ?? undefined
    ).subscribe({
      next: (response) => {
        this.cargando = false;

        this.mensajeExito =
          response?.message ||
          response?.mensaje ||
          'Postulación realizada exitosamente.';

        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/empleado/mis-postulaciones']);
        }, 1200);
      },
      error: (error) => {
        console.error('Error al postular:', error);

        this.cargando = false;

        this.mensajeError =
          error.error?.message ||
          error.error?.mensaje ||
          error.error?.error ||
          'Ocurrió un error al postular.';

        if (this.normalizarMensaje(this.mensajeError).includes('periodo de postulacion')) {
          this.cargarOferta();
        }

        this.cdr.detectChanges();
      }
    });
  }

  irSuscripciones(): void {
    this.router.navigate(['/suscripciones']);
  }

  irPoliticaPrivacidad(): void {
    this.router.navigate(['/politica-privacidad']);
  }

  volver(): void {
    this.router.navigate(['/ofertas']);
  }

  private obtenerFechaLocalActual(): string {
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  private normalizarMensaje(texto: string): string {
    return (texto || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }
}
