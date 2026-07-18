import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { EmpleadoService } from 'src/app/core/services/empleado';
import { EmpleadoResponse } from 'src/app/core/models/empleado';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-perfil-empleado-publico',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './perfil-empleado-publico.html',
  styleUrl: './perfil-empleado-publico.scss'
})
export class PerfilEmpleadoPublicoComponent implements OnInit {

  idEmpleado!: number;

  perfil?: EmpleadoResponse;

  cargando = true;
  mensajeError = '';

  tabActiva: 'finalizados' | 'activos' = 'finalizados';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private empleadoService: EmpleadoService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.idEmpleado = Number(this.route.snapshot.paramMap.get('idEmpleado'));

    if (!this.idEmpleado) {
      this.cargando = false;
      this.mensajeError = 'No se encontró el empleado seleccionado.';
      this.cdr.detectChanges();
      return;
    }

    this.obtenerPerfilPublico();
  }

  obtenerPerfilPublico(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.empleadoService.obtenerPerfilPublico(this.idEmpleado).subscribe({
      next: (response) => {
        this.perfil = response.data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al obtener perfil público:', error);

        this.cargando = false;

        if (error.status === 404) {
          this.mensajeError = 'No se encontró el perfil del empleado.';
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
          'No se pudo cargar el perfil del empleado.';

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

  obtenerIniciales(): string {
    if (!this.perfil) {
      return 'UP';
    }

    const nombre = this.perfil.nombre?.charAt(0) || '';
    const apellido = this.perfil.apellido?.charAt(0) || '';

    return `${nombre}${apellido}`.toUpperCase();
  }

  obtenerDisponibilidadEquipo(): string[] {
    if (!this.perfil?.disponibilidadEquipo) {
      return [];
    }

    return this.perfil.disponibilidadEquipo
      .split(',')
      .map(item => item.trim())
      .filter(item => item.length > 0);
  }
}