import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService, CuentaUsuario } from 'src/app/core/services/auth';
import { cumplePoliticaPassword } from 'src/app/core/validation/password-policy';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-editar-cuenta',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './editar-cuenta.html',
  styleUrl: './editar-cuenta.scss'
})
export class EditarCuentaComponent implements OnInit {

  cuenta?: CuentaUsuario;

  nuevoEmail = '';
  passwordActualEmail = '';

  passwordActual = '';
  nuevaPassword = '';
  confirmarPassword = '';

  passwordEliminar = '';
  confirmacionEliminar = '';

  cargando = true;
  guardandoEmail = false;
  guardandoPassword = false;
  eliminandoCuenta = false;

  mensajeExito = '';
  mensajeError = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCuenta();
  }

  cargarCuenta(): void {
    this.cargando = true;
    this.mensajeError = '';

    this.authService.obtenerCuenta().subscribe({
      next: (response) => {
        this.cuenta = response.data;
        this.nuevoEmail = response.data.email;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando cuenta:', error);
        this.cargando = false;

        if (error.status === 401 || error.status === 403) {
          this.mostrarError('Tu sesión expiró. Inicia sesión nuevamente.');
          this.authService.limpiarSesion();
          this.router.navigate(['/login']);
          return;
        }

        this.mostrarError(error?.error?.message || 'No se pudo cargar la información de la cuenta.');
      }
    });
  }

  actualizarEmail(): void {
    this.limpiarMensajes();

    const nuevoEmailNormalizado = this.nuevoEmail.trim().toLowerCase();

    if (!nuevoEmailNormalizado) {
      this.mostrarError('Ingresa el nuevo correo electrónico.');
      return;
    }

    if (!this.passwordActualEmail) {
      this.mostrarError('Ingresa tu contraseña actual para confirmar el cambio de correo.');
      return;
    }

    if (this.cuenta && nuevoEmailNormalizado === this.cuenta.email.toLowerCase()) {
      this.mostrarError('El nuevo correo debe ser diferente al correo actual.');
      return;
    }

    this.guardandoEmail = true;

    this.authService.actualizarEmail({
      nuevoEmail: nuevoEmailNormalizado,
      passwordActual: this.passwordActualEmail
    }).subscribe({
      next: (response) => {
        this.authService.guardarSesion(response.data);

        this.cuenta = {
          ...(this.cuenta as CuentaUsuario),
          email: response.data.email
        };

        this.nuevoEmail = response.data.email;
        this.passwordActualEmail = '';
        this.guardandoEmail = false;
        this.mostrarExito('Correo actualizado correctamente. Tu sesión fue actualizada con el nuevo correo.');
      },
      error: (error) => {
        console.error('Error actualizando email:', error);
        this.guardandoEmail = false;
        this.mostrarError(error?.error?.message || 'No se pudo actualizar el correo.');
      }
    });
  }

  actualizarPassword(): void {
    this.limpiarMensajes();

    if (!this.passwordActual || !this.nuevaPassword || !this.confirmarPassword) {
      this.mostrarError('Completa todos los campos de contraseña.');
      return;
    }

    if (!cumplePoliticaPassword(this.nuevaPassword)) {
      this.mostrarError('La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&).');
      return;
    }

    if (this.nuevaPassword !== this.confirmarPassword) {
      this.mostrarError('La nueva contraseña y la confirmación no coinciden.');
      return;
    }

    this.guardandoPassword = true;

    this.authService.actualizarPassword({
      passwordActual: this.passwordActual,
      nuevaPassword: this.nuevaPassword,
      confirmarPassword: this.confirmarPassword
    }).subscribe({
      next: () => {
        this.passwordActual = '';
        this.nuevaPassword = '';
        this.confirmarPassword = '';
        this.guardandoPassword = false;
        this.mostrarExito('Contraseña actualizada correctamente.');
      },
      error: (error) => {
        console.error('Error actualizando contraseña:', error);
        this.guardandoPassword = false;
        this.mostrarError(error?.error?.message || 'No se pudo actualizar la contraseña.');
      }
    });
  }

  eliminarCuenta(): void {
    this.limpiarMensajes();

    if (!this.passwordEliminar) {
      this.mostrarError('Ingresa tu contraseña actual para eliminar la cuenta.');
      return;
    }

    if (this.confirmacionEliminar.trim().toUpperCase() !== 'ELIMINAR') {
      this.mostrarError('Para confirmar la eliminación debes escribir ELIMINAR.');
      return;
    }

    const confirmado = confirm(
      '¿Seguro que deseas eliminar tu cuenta? Esta acción eliminará tu perfil, postulaciones, ofertas asociadas y archivos relacionados. No se puede deshacer.'
    );

    if (!confirmado) {
      return;
    }

    this.eliminandoCuenta = true;

    this.authService.eliminarCuenta({
      passwordActual: this.passwordEliminar,
      confirmacion: this.confirmacionEliminar.trim().toUpperCase()
    }).subscribe({
      next: () => {
        this.eliminandoCuenta = false;
        this.passwordEliminar = '';
        this.confirmacionEliminar = '';
        this.authService.limpiarSesion();
        this.mostrarExito('Cuenta eliminada correctamente. Serás redirigido al inicio de sesión.');

        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1600);
      },
      error: (error) => {
        console.error('Error eliminando cuenta:', error);
        this.eliminandoCuenta = false;
        this.mostrarError(error?.error?.message || 'No se pudo eliminar la cuenta.');
      }
    });
  }

  volver(): void {
    if (this.cuenta?.esEmpleado) {
      this.router.navigate(['/empleado/perfil']);
      return;
    }

    if (this.cuenta?.esEmpleador) {
      this.router.navigate(['/empleador/perfil']);
      return;
    }

    this.router.navigate(['/suscripciones']);
  }

  private limpiarMensajes(): void {
    this.mensajeExito = '';
    this.mensajeError = '';
  }

  private mostrarExito(mensaje: string): void {
    this.mensajeExito = mensaje;
    this.mensajeError = '';
    this.cdr.detectChanges();
    this.subirAlInicio();
  }

  private mostrarError(mensaje: string): void {
    this.mensajeError = mensaje;
    this.mensajeExito = '';
    this.cdr.detectChanges();
    this.subirAlInicio();
  }

  private subirAlInicio(): void {
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 0);
  }

  get passwordTieneLongitud(): boolean {
    return this.nuevaPassword.length >= 8;
  }

  get passwordTieneMayuscula(): boolean {
    return /[A-Z]/.test(this.nuevaPassword);
  }

  get passwordTieneMinuscula(): boolean {
    return /[a-z]/.test(this.nuevaPassword);
  }

  get passwordTieneNumero(): boolean {
    return /\d/.test(this.nuevaPassword);
  }

  get passwordTieneEspecial(): boolean {
    return /[@$!%*?&]/.test(this.nuevaPassword);
  }

  get passwordValida(): boolean {
    return cumplePoliticaPassword(this.nuevaPassword);
  }

  get tipoCuenta(): string {
    if (this.cuenta?.esEmpleado) return 'Empleado';
    if (this.cuenta?.esEmpleador) return 'Empleador';
    return 'Usuario';
  }
}
