import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { EmpleadorResponse } from 'src/app/core/models/empleador';
import { EmpleadorService } from 'src/app/core/services/empleador';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-perfil-empleador-publico',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './perfil-empleador-publico.html',
  styleUrl: './perfil-empleador-publico.scss'
})
export class PerfilEmpleadorPublicoComponent implements OnInit {

  idEmpleador!: number;
  perfil?: EmpleadorResponse;

  cargando = true;
  mensajeError = '';

  tabActiva: 'finalizados' | 'activos' = 'activos';

  constructor(
    private route: ActivatedRoute,
    private empleadorService: EmpleadorService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.idEmpleador = Number(this.route.snapshot.paramMap.get('idEmpleador'));

    if (!this.idEmpleador) {
      this.cargando = false;
      this.mensajeError = 'No se encontró la empresa seleccionada.';
      this.cdr.detectChanges();
      return;
    }

    this.obtenerPerfilPublico();
  }

  obtenerPerfilPublico(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.empleadorService.obtenerPerfilPublico(this.idEmpleador).subscribe({
      next: (response) => {
        this.perfil = response.data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener perfil público de empleador:', error);

        this.cargando = false;

        if (error.status === 404) {
          this.mensajeError = 'No se encontró el perfil de la empresa.';
          this.cdr.detectChanges();
          return;
        }

        if (error.status === 401 || error.status === 403) {
          this.mensajeError = 'No estás autorizado para ver este perfil.';
          this.cdr.detectChanges();
          return;
        }

        this.mensajeError =
          error?.error?.message ||
          error?.error?.mensaje ||
          'No se pudo cargar el perfil de la empresa.';

        this.cdr.detectChanges();
      }
    });
  }

  cambiarTab(tab: 'finalizados' | 'activos'): void {
    this.tabActiva = tab;
  }

  volver(): void {
    window.history.back();
  }

  obtenerUrlLogo(): string {
    if (!this.perfil?.logoEmpleador || !this.idEmpleador) {
      return '';
    }

    return this.empleadorService.obtenerUrlLogoPublico(
      this.idEmpleador,
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

  visitarSitioWeb(): void {
    if (!this.perfil?.sitioWeb) {
      return;
    }

    let url = this.perfil.sitioWeb.trim();

    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = `https://${url}`;
    }

    window.open(url, '_blank');
  }

  tieneContactoOpcional(): boolean {
    return !!(
      this.perfil?.sitioWeb ||
      this.perfil?.correoContacto ||
      this.perfil?.telefonoContacto
    );
  }
}
