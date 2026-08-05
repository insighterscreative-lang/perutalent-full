import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth';
import { cumplePoliticaPassword } from 'src/app/core/validation/password-policy';

@Component({
  selector: 'app-recuperar-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './recuperar-password.html',
  styleUrl: './recuperar-password.scss'
})
export class RecuperarPasswordComponent implements OnDestroy {

  paso: 'correo' | 'codigo' | 'password' | 'completado' = 'correo';

  email = '';
  emailEnmascarado = '';
  codigo = '';
  nuevaPassword = '';
  confirmarPassword = '';

  loading = false;
  loadingReenvio = false;
  error = '';
  mensaje = '';

  segundosRestantes = 0;
  readonly segundosEsperaReenvio = 60;
  private timerId: ReturnType<typeof setInterval> | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnDestroy(): void {
    this.detenerTimer();
  }

  solicitarCodigo(): void {
    this.error = '';
    this.mensaje = '';

    const emailLimpio = this.email.trim().toLowerCase();

    if (!emailLimpio) {
      this.error = 'Ingresa tu correo electrónico registrado.';
      this.subirPagina();
      return;
    }

    // Se muestra el paso del código inmediatamente mientras el backend envía el correo.
    this.loading = true;
    this.email = emailLimpio;
    this.emailEnmascarado = this.enmascararEmail(emailLimpio);
    this.codigo = '';
    this.paso = 'codigo';
    this.mensaje = `Estamos enviando un código de confirmación a ${this.emailEnmascarado}. Puedes ingresarlo apenas llegue.`;
    this.iniciarTimer();
    this.subirPagina();

    this.authService.solicitarCodigoRecuperacion({ email: emailLimpio }).subscribe({
      next: () => {
        this.loading = false;
        this.mensaje = `Te enviamos un código de confirmación a ${this.emailEnmascarado}. Ingresa el código dentro de 2 minutos.`;
        this.actualizarVista();
        this.subirPagina();
      },
      error: (err) => {
        this.loading = false;
        this.detenerTimer();
        this.codigo = '';
        this.paso = 'correo';
        this.mensaje = '';
        this.error = err.error?.message || 'No se pudo enviar el código de recuperación.';
        this.actualizarVista();
        this.subirPagina();
      }
    });
  }

  reenviarCodigo(): void {
    if (!this.puedeReenviarCodigo || !this.email.trim()) {
      return;
    }

    this.error = '';
    this.codigo = '';
    this.loadingReenvio = true;

    const emailLimpio = this.email.trim().toLowerCase();
    this.email = emailLimpio;
    this.emailEnmascarado = this.enmascararEmail(emailLimpio);
    this.mensaje = `Estamos enviando un nuevo código a ${this.emailEnmascarado}. El código anterior dejará de funcionar.`;
    this.iniciarTimer();
    this.subirPagina();

    this.authService.solicitarCodigoRecuperacion({ email: emailLimpio }).subscribe({
      next: () => {
        this.loadingReenvio = false;
        this.mensaje = `Te enviamos un nuevo código a ${this.emailEnmascarado}. El código anterior ya no funcionará.`;
        this.actualizarVista();
        this.subirPagina();
      },
      error: (err) => {
        this.loadingReenvio = false;
        this.detenerTimer();
        this.error = err.error?.message || 'No se pudo reenviar el código.';
        this.actualizarVista();
        this.subirPagina();
      }
    });
  }

  verificarCodigo(): void {
    this.error = '';
    this.mensaje = '';

    if (this.segundosRestantes <= 0) {
      this.error = 'El código venció. Solicita uno nuevo.';
      this.subirPagina();
      return;
    }

    const codigoLimpio = this.codigo.trim();

    if (!/^[0-9]{6}$/.test(codigoLimpio)) {
      this.error = 'Ingresa el código de confirmación de 6 dígitos.';
      this.subirPagina();
      return;
    }

    this.loading = true;

    this.authService.verificarCodigoRecuperacion({
      email: this.email.trim().toLowerCase(),
      codigo: codigoLimpio
    }).subscribe({
      next: () => {
        this.loading = false;
        this.detenerTimer();
        this.nuevaPassword = '';
        this.confirmarPassword = '';
        this.paso = 'password';
        this.mensaje = 'Código confirmado. Ahora ingresa tu nueva contraseña.';

        // En Angular zoneless, el callback HTTP no siempre vuelve a dibujar
        // automáticamente el componente. Forzamos la actualización inmediata.
        this.actualizarVista();
        this.subirPagina();
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'No se pudo validar el código.';
        this.actualizarVista();
        this.subirPagina();
      }
    });
  }

  restablecerPassword(): void {
    this.error = '';
    this.mensaje = '';

    if (!this.nuevaPassword || !this.confirmarPassword) {
      this.error = 'Ingresa y confirma tu nueva contraseña.';
      this.subirPagina();
      return;
    }

    if (!cumplePoliticaPassword(this.nuevaPassword)) {
      this.error = 'La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&).';
      this.subirPagina();
      return;
    }

    if (this.nuevaPassword !== this.confirmarPassword) {
      this.error = 'La nueva contraseña y la confirmación no coinciden.';
      this.subirPagina();
      return;
    }

    this.loading = true;

    this.authService.restablecerPassword({
      email: this.email.trim().toLowerCase(),
      codigo: this.codigo.trim(),
      nuevaPassword: this.nuevaPassword,
      confirmarPassword: this.confirmarPassword
    }).subscribe({
      next: () => {
        this.loading = false;
        this.paso = 'completado';
        this.mensaje = 'Contraseña restablecida correctamente. Ya puedes iniciar sesión.';
        this.actualizarVista();
        this.subirPagina();
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'No se pudo restablecer la contraseña.';
        this.actualizarVista();
        this.subirPagina();
      }
    });
  }

  volverAIngresarCorreo(): void {
    this.detenerTimer();
    this.codigo = '';
    this.nuevaPassword = '';
    this.confirmarPassword = '';
    this.error = '';
    this.mensaje = '';
    this.paso = 'correo';
    this.subirPagina();
  }

  volverLogin(): void {
    this.router.navigate(['/login']);
  }

  private iniciarTimer(): void {
    this.detenerTimer();
    this.segundosRestantes = 120;

    this.timerId = setInterval(() => {
      this.segundosRestantes -= 1;

      if (this.segundosRestantes <= 0) {
        this.segundosRestantes = 0;
        this.detenerTimer();
        this.error = 'El código venció. Solicita uno nuevo.';
        this.subirPagina();
      }

      // Mantiene actualizado el contador también cuando la app funciona sin ZoneJS.
      this.actualizarVista();
    }, 1000);
  }

  private detenerTimer(): void {
    if (this.timerId !== null) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private actualizarVista(): void {
    this.cdr.detectChanges();
  }

  private subirPagina(): void {
    setTimeout(() => window.scrollTo({ top: 0, behavior: 'smooth' }), 0);
  }

  get puedeReenviarCodigo(): boolean {
    return !this.loading
      && !this.loadingReenvio
      && this.segundosRestantes <= this.segundosEsperaReenvio;
  }

  get segundosParaReenvio(): number {
    return Math.max(0, this.segundosRestantes - this.segundosEsperaReenvio);
  }

  get tiempoParaReenvioFormateado(): string {
    const minutos = Math.floor(this.segundosParaReenvio / 60).toString().padStart(2, '0');
    const segundos = (this.segundosParaReenvio % 60).toString().padStart(2, '0');
    return `${minutos}:${segundos}`;
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

  get tiempoFormateado(): string {
    const minutos = Math.floor(this.segundosRestantes / 60).toString().padStart(2, '0');
    const segundos = (this.segundosRestantes % 60).toString().padStart(2, '0');
    return `${minutos}:${segundos}`;
  }

  private enmascararEmail(email: string): string {
    const [local, dominio] = email.split('@');

    if (!local || !dominio) {
      return email;
    }

    const ultimos = local.slice(-3);
    const cantidadAsteriscos = Math.max(local.length - 3, 6);

    return `${'*'.repeat(cantidadAsteriscos)}${ultimos}@${dominio}`;
  }
}
