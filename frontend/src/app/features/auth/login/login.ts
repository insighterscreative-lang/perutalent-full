import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  email: string = '';
  password: string = '';

  loading: boolean = false;
  error: string = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  login(): void {
    this.error = '';
    this.loading = true;

    this.authService.login({
      email: this.email,
      password: this.password
    }).subscribe({
      next: (res: any) => {
        console.log('LOGIN RESPONSE:', res);

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
          this.router.navigate(['/empleado/perfil']);
          return;
        }

        if (esEmpleador) {
          this.router.navigate(['/empleador/perfil']);
          return;
        }

        this.router.navigate(['/ofertas']);
      },
      error: (err: any) => {
        console.error('LOGIN ERROR', err);

        this.loading = false;
        this.error = err.error?.message || 'Credenciales inválidas';

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