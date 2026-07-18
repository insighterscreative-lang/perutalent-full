import { CommonModule } from '@angular/common';
import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {

  nombre: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  tipo: string = '';

  loading: boolean = false;
  error: string = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  register(): void {
    this.error = '';

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

    if (!this.nombre || !this.email || !this.password || !this.confirmPassword) {
      this.error = 'Todos los campos son obligatorios';
      return;
    }

    if (!emailRegex.test(this.email)) {
      this.error = 'Correo electrónico inválido';
      return;
    }

    if (this.password.length < 6) {
      this.error = 'La contraseña debe tener al menos 6 caracteres';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.error = 'Las contraseñas no coinciden';
      return;
    }

    if (!this.tipo) {
      this.error = 'Selecciona un tipo de cuenta';
      return;
    }

    this.loading = true;

    const payload = {
      email: this.email,
      password: this.password,
      esEmpleado: this.tipo === 'empleado',
      esEmpleador: this.tipo === 'empleador'
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.loginAutomatico();
      },
      error: (err: any) => {
        console.error('REGISTER ERROR', err);

        this.loading = false;
        this.error = err.error?.message || 'Error en el registro';

        this.cdr.detectChanges();
      }
    });
  }

  private loginAutomatico(): void {
    this.authService.login({
      email: this.email,
      password: this.password
    }).subscribe({
      next: (res: any) => {
        console.log('LOGIN AUTOMÁTICO RESPONSE:', res);

        const data = res.data;
        const token = data.token;

        this.authService.guardarToken(token);

        localStorage.setItem('idUsuario', String(data.id));
        localStorage.setItem('email', data.email);

        const esEmpleado =
          data.esEmpleado === true ||
          data.esEmpleado === 'true' ||
          this.tokenTieneRol(token, 'ROLE_EMPLEADO');

        const esEmpleador =
          data.esEmpleador === true ||
          data.esEmpleador === 'true' ||
          this.tokenTieneRol(token, 'ROLE_EMPLEADOR');

        localStorage.setItem('esEmpleado', String(esEmpleado));
        localStorage.setItem('esEmpleador', String(esEmpleador));

        this.loading = false;

        if (esEmpleado) {
          this.router.navigate(['/empleado/crear-perfil']);
          return;
        }

        if (esEmpleador) {
          this.router.navigate(['/empleador/crear-perfil']);
          return;
        }

        this.router.navigate(['/ofertas']);
      },
      error: (err: any) => {
        console.error('LOGIN AUTOMÁTICO ERROR', err);

        this.loading = false;
        this.error = 'Cuenta creada, pero error al iniciar sesión automáticamente';

        this.cdr.detectChanges();
      }
    });
  }

  private tokenTieneRol(token: string, rol: string): boolean {
    try {
      const payloadBase64 = token.split('.')[1];
      const payloadJson = atob(payloadBase64);
      const payload = JSON.parse(payloadJson);

      return Array.isArray(payload.roles) && payload.roles.includes(rol);
    } catch (error) {
      console.error('Error leyendo roles del token:', error);
      return false;
    }
  }
}