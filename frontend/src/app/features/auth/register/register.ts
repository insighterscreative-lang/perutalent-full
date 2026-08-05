import { CommonModule } from '@angular/common';
import { Component, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth';
import { cumplePoliticaPassword } from 'src/app/core/validation/password-policy';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {

  nombre: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  tipo: string = '';
  aceptaTerminos: boolean = false;
  mostrarResumenTerminos: boolean = false;

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

    if (!cumplePoliticaPassword(this.password)) {
      this.error = 'La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&)';
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

    if (!this.aceptaTerminos) {
      this.error = 'Debes aceptar los Términos y Condiciones y la Política de Privacidad';
      return;
    }

    this.loading = true;

    const payload = {
      email: this.email,
      password: this.password,
      esEmpleado: this.tipo === 'empleado',
      esEmpleador: this.tipo === 'empleador',
      aceptaTerminos: this.aceptaTerminos
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

  get passwordTieneLongitud(): boolean {
    return this.password.length >= 8;
  }

  get passwordTieneMayuscula(): boolean {
    return /[A-Z]/.test(this.password);
  }

  get passwordTieneMinuscula(): boolean {
    return /[a-z]/.test(this.password);
  }

  get passwordTieneNumero(): boolean {
    return /\d/.test(this.password);
  }

  get passwordTieneEspecial(): boolean {
    return /[@$!%*?&]/.test(this.password);
  }

  get passwordValida(): boolean {
    return cumplePoliticaPassword(this.password);
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