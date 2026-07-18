import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { PostulacionService } from '../../../core/services/postulacion';

@Component({
  selector: 'app-postular-oferta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './postular-oferta.html',
  styleUrl: './postular-oferta.scss'
})
export class PostularOfertaComponent {

  idOferta!: number;

  usarCvPerfil: boolean = false;
  cvSeleccionado: File | null = null;

  mensajeExito: string = '';
  mensajeError: string = '';

  cargando: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private postulacionService: PostulacionService
  ) {}

  ngOnInit(): void {
    this.idOferta = Number(this.route.snapshot.paramMap.get('id'));

    console.log('ID oferta desde la ruta:', this.idOferta);
  }

  get mostrarUpgradePostulaciones(): boolean {
    const mensaje = this.normalizarMensaje(this.mensajeError);

    return mensaje.includes('limite mensual de postulaciones') ||
      mensaje.includes('límite mensual de postulaciones') ||
      mensaje.includes('has alcanzado el limite mensual') ||
      mensaje.includes('has alcanzado el límite mensual');
  }

  cambiarOpcionCv(valor: boolean): void {
    this.usarCvPerfil = valor;

    if (valor) {
      this.cvSeleccionado = null;
    }

    this.mensajeError = '';
    this.mensajeExito = '';

    console.log('Opción CV cambiada. usarCvPerfil:', this.usarCvPerfil);
  }

  seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;

    console.log('Evento input file:', input.files);

    if (!input.files || input.files.length === 0) {
      this.cvSeleccionado = null;
      console.log('No se seleccionó archivo');
      return;
    }

    const archivo = input.files[0];

    console.log('Archivo seleccionado:', archivo);
    console.log('Nombre:', archivo.name);
    console.log('Tipo:', archivo.type);
    console.log('Tamaño:', archivo.size);

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
    console.log('CLICK EN POSTULAR');

    this.mensajeExito = '';
    this.mensajeError = '';

    console.log('idOferta:', this.idOferta);
    console.log('usarCvPerfil:', this.usarCvPerfil);
    console.log('cvSeleccionado:', this.cvSeleccionado);

    if (!this.idOferta) {
      this.mensajeError = 'No se encontró la oferta laboral.';
      return;
    }

    if (!this.usarCvPerfil && !this.cvSeleccionado) {
      this.mensajeError = 'Debes seleccionar un CV en PDF.';
      console.log('Se detuvo porque no hay CV seleccionado');
      return;
    }

    this.cargando = true;

    this.postulacionService.postular(
      this.idOferta,
      this.usarCvPerfil,
      this.cvSeleccionado ?? undefined
    ).subscribe({
      next: (response) => {
        console.log('Respuesta OK:', response);

        this.cargando = false;

        this.mensajeExito =
          response?.message ||
          response?.mensaje ||
          'Postulación realizada exitosamente.';

        setTimeout(() => {
          this.router.navigate(['/ofertas']);
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
      }
    });
  }

  irSuscripciones(): void {
    this.router.navigate(['/suscripciones']);
  }

  volver(): void {
    this.router.navigate(['/ofertas']);
  }

  private normalizarMensaje(texto: string): string {
    return (texto || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }
}