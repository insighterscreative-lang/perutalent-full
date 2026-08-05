import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { MiSuscripcion, PlanSuscripcion, UsoPlanUsuario } from 'src/app/core/models/suscripcion';
import { AuthService } from 'src/app/core/services/auth';
import { SuscripcionService } from 'src/app/core/services/suscripcion';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

declare global {
  interface Window {
    Culqi: any;
    culqi: () => void;
  }
}

@Component({
  selector: 'app-suscripciones',
  standalone: true,
  imports: [CommonModule, TopbarComponent],
  templateUrl: './suscripciones.html',
  styleUrl: './suscripciones.scss'
})
export class SuscripcionesComponent implements OnInit {

  planes: PlanSuscripcion[] = [];
  miSuscripcion?: MiSuscripcion;
  miUso?: UsoPlanUsuario;

  autenticado = false;
  cargando = true;
  cambiandoPlan = false;
  pagandoPremium = false;
  cancelandoSuscripcion = false;

  mensajeError = '';
  mensajeExito = '';

  culqiPublicKey = '';
  culqiTestMode = false;
  planPendientePago?: PlanSuscripcion;

  constructor(
    private suscripcionService: SuscripcionService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.autenticado = this.authService.estaAutenticado();
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.suscripcionService.listarPlanes().subscribe({
      next: (planes) => {
        this.planes = planes || [];

        if (this.autenticado) {
          this.cargarConfigCulqi();
          this.cargarMiSuscripcion();
        } else {
          this.cargando = false;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error cargando planes:', error);
        this.mensajeError = 'No se pudieron cargar los planes de suscripción.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarConfigCulqi(): void {
    this.suscripcionService.obtenerConfigCulqi().subscribe({
      next: (config) => {
        this.culqiPublicKey = config.publicKey;
        this.culqiTestMode = config.testMode;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando configuración de Culqi:', error);
        this.culqiPublicKey = '';
        this.culqiTestMode = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarMiSuscripcion(): void {
    this.suscripcionService.obtenerMiSuscripcion().subscribe({
      next: (suscripcion) => {
        this.miSuscripcion = suscripcion;
        this.cargarMiUso();
      },
      error: (error) => {
        console.error('Error cargando mi suscripción:', error);
        this.mensajeError = 'No se pudo cargar tu suscripción actual.';
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarMiUso(): void {
    this.suscripcionService.obtenerMiUso().subscribe({
      next: (uso) => {
        this.miUso = uso;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando uso del plan:', error);
        this.miUso = undefined;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  seleccionarPlan(plan: PlanSuscripcion): void {
    if (!this.autenticado) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.esPlanActual(plan) || this.cambiandoPlan || this.pagandoPremium || this.cancelandoSuscripcion) {
      return;
    }

    this.mensajeError = '';
    this.mensajeExito = '';

    if (this.esPremium(plan)) {
      this.iniciarPagoPremium(plan);
      return;
    }

    if (this.miSuscripcion?.esPremium) {
      this.cancelarSuscripcionPremium();
      return;
    }

    this.cambiarPlanGratuito(plan);
  }

  cambiarPlanGratuito(plan: PlanSuscripcion): void {
    this.cambiandoPlan = true;

    this.suscripcionService.cambiarPlan(plan.id).subscribe({
      next: (suscripcion) => {
        this.miSuscripcion = suscripcion;
        this.mensajeExito = `Tu plan ahora es ${suscripcion.nombrePlan}.`;
        this.cambiandoPlan = false;
        this.cargarMiUso();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cambiando plan:', error);

        this.mensajeError = this.obtenerMensajeError(error, 'No se pudo cambiar el plan seleccionado.');

        this.cambiandoPlan = false;
        this.cdr.detectChanges();
      }
    });
  }

  cancelarSuscripcionPremium(): void {
    if (!this.autenticado || this.cancelandoSuscripcion) {
      return;
    }

    const confirmado = window.confirm(
      '¿Deseas cancelar tu suscripción Premium? Se cancelarán los cobros automáticos futuros.'
    );

    if (!confirmado) {
      return;
    }

    this.cancelandoSuscripcion = true;
    this.mensajeError = '';
    this.mensajeExito = 'Cancelando suscripción Premium...';
    this.cdr.detectChanges();

    this.suscripcionService.cancelarPremium().subscribe({
      next: (suscripcion) => {
        this.miSuscripcion = suscripcion;
        this.mensajeExito = 'Tu suscripción Premium fue cancelada. Ya no se realizarán cobros automáticos futuros.';
        this.cancelandoSuscripcion = false;
        this.cargarMiUso();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cancelando suscripción:', error);
        this.mensajeError = this.obtenerMensajeError(error, 'No se pudo cancelar la suscripción Premium.');
        this.mensajeExito = '';
        this.cancelandoSuscripcion = false;
        this.cdr.detectChanges();
      }
    });
  }

  async iniciarPagoPremium(plan: PlanSuscripcion): Promise<void> {
    if (!this.culqiPublicKey) {
      this.mensajeError = 'No se pudo cargar la configuración pública de Culqi.';
      this.cdr.detectChanges();
      return;
    }

    if (!plan.precioCentimos || plan.precioCentimos <= 0) {
      this.mensajeError = 'El plan Premium no tiene un precio válido.';
      this.cdr.detectChanges();
      return;
    }

    this.planPendientePago = plan;
    this.pagandoPremium = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cdr.detectChanges();

    try {
      await this.cargarScriptCulqi();

      window.Culqi.publicKey = this.culqiPublicKey;

      window.Culqi.settings({
        title: 'PeruTalent Premium',
        currency: plan.moneda || 'PEN',
        amount: plan.precioCentimos,
        description: 'Suscripción Premium mensual con renovación automática'
      });

      window.Culqi.options({
        lang: 'es',
        installments: false,
        paymentMethods: {
          tarjeta: true,
          yape: false,
          bancaMovil: false,
          agente: false,
          billetera: false,
          cuotealo: false
        },
        style: {
          buttonText: 'Suscribirme',
          buttonBackground: '#22c55e',
          buttonTextColor: '#ffffff',
          priceColor: '#0f172a'
        }
      });

      window.culqi = () => {
        this.ngZone.run(() => {
          if (window.Culqi.token) {
            const tokenId = window.Culqi.token.id;

            if (window.Culqi.close) {
              window.Culqi.close();
            }

            this.procesarPagoPremium(plan, tokenId);
            return;
          }

          if (window.Culqi.error) {
            console.error('Error Culqi:', window.Culqi.error);

            this.mensajeError =
              window.Culqi.error?.user_message ||
              window.Culqi.error?.merchant_message ||
              'No se pudo generar el token de pago.';

            this.pagandoPremium = false;
            this.planPendientePago = undefined;
            this.cdr.detectChanges();
          }
        });
      };

      window.Culqi.open();
    } catch (error) {
      console.error('Error cargando Culqi Checkout:', error);
      this.mensajeError = 'No se pudo abrir el checkout de Culqi.';
      this.pagandoPremium = false;
      this.planPendientePago = undefined;
      this.cdr.detectChanges();
    }
  }

  procesarPagoPremium(plan: PlanSuscripcion, tokenId: string): void {
    this.mensajeError = '';
    this.mensajeExito = 'Creando suscripción mensual con Culqi...';
    this.cdr.detectChanges();

    this.suscripcionService.pagarPremium({
      idPlan: plan.id,
      tokenId
    }).subscribe({
      next: (response) => {
        this.mensajeExito = response.mensaje || 'Suscripción Premium creada correctamente.';
        this.pagandoPremium = false;
        this.planPendientePago = undefined;

        this.cargarMiSuscripcion();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error creando suscripción Premium:', error);

        this.mensajeError = this.obtenerMensajeError(error, 'No se pudo crear la suscripción con Culqi.');

        this.mensajeExito = '';
        this.pagandoPremium = false;
        this.planPendientePago = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  cargarScriptCulqi(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (window.Culqi) {
        resolve();
        return;
      }

      const scriptExistente = document.getElementById('culqi-checkout-script');

      if (scriptExistente) {
        scriptExistente.addEventListener('load', () => resolve());
        scriptExistente.addEventListener('error', () => reject());
        return;
      }

      const script = document.createElement('script');
      script.id = 'culqi-checkout-script';
      script.src = 'https://checkout.culqi.com/js/v4';
      script.async = true;

      script.onload = () => resolve();
      script.onerror = () => reject();

      document.body.appendChild(script);
    });
  }

  esPlanActual(plan: PlanSuscripcion): boolean {
    return this.miSuscripcion?.idPlan === plan.id;
  }

  esPremium(plan: PlanSuscripcion): boolean {
    return plan.nombrePlan?.toUpperCase() === 'PREMIUM';
  }

  obtenerPrecio(plan: PlanSuscripcion): string {
    if (!plan.precioCentimos || plan.precioCentimos === 0) {
      return 'Gratis';
    }

    const monto = plan.precioCentimos / 100;
    const moneda = plan.moneda === 'PEN' ? 'S/' : plan.moneda;

    return `${moneda} ${monto.toFixed(2)}`;
  }

  obtenerPeriodo(plan: PlanSuscripcion): string {
    if (!plan.precioCentimos || plan.precioCentimos === 0) {
      return 'sin pago';
    }

    return 'mensual automático';
  }

  obtenerBeneficios(plan: PlanSuscripcion): string[] {
    const beneficios: string[] = [];

    if (plan.maxPostulacionesMes === null) {
      beneficios.push('Postulaciones ilimitadas al mes');
    } else {
      beneficios.push(`Hasta ${plan.maxPostulacionesMes} postulaciones al mes`);
    }

    if (plan.maxRecomendaciones === null) {
      beneficios.push('Recomendaciones “para ti” ilimitadas');
    } else {
      beneficios.push(`Hasta ${plan.maxRecomendaciones} recomendaciones “para ti”`);
    }

    if (plan.maxOfertasActivas === null) {
      beneficios.push('Ofertas activas ilimitadas como empleador');
    } else {
      beneficios.push(`Hasta ${plan.maxOfertasActivas} ofertas activas como empleador`);
    }

    if (plan.prioridadPostulante) {
      beneficios.push('Prioridad en la lista de postulantes');
    }

    if (this.esPremium(plan)) {
      beneficios.push('Renovación automática mensual mientras no canceles');
      beneficios.push('Puedes cancelar la suscripción desde tu cuenta');
    }

    if (!plan.prioridadPostulante) {
      beneficios.push('Acceso básico a la plataforma');
    }

    return beneficios;
  }

  obtenerTextoCantidad(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) {
      return 'Ilimitado';
    }

    return valor.toString();
  }

  obtenerTextoCantidadPlural(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) {
      return 'Ilimitadas';
    }

    return valor.toString();
  }

  obtenerPorcentajeUsado(usado: number, maximo: number | null): number {
    if (maximo === null || maximo === undefined || maximo <= 0) {
      return 100;
    }

    return Math.min((usado / maximo) * 100, 100);
  }

  obtenerTextoBoton(plan: PlanSuscripcion): string {
    if (!this.autenticado) {
      return 'Iniciar sesión para elegir';
    }

    if (this.esPlanActual(plan)) {
      return 'Plan actual';
    }

    if (this.pagandoPremium && this.planPendientePago?.id === plan.id) {
      return 'Creando suscripción...';
    }

    if (this.cancelandoSuscripcion) {
      return 'Cancelando...';
    }

    if (this.cambiandoPlan) {
      return 'Actualizando...';
    }

    if (this.miSuscripcion?.esPremium && !this.esPremium(plan)) {
      return 'Cancelar Premium';
    }

    if (this.esPremium(plan)) {
      return 'Suscribirme a Premium';
    }

    return 'Elegir plan';
  }

  tienePremiumRenovable(): boolean {
    return !!this.miSuscripcion?.esPremium && !!this.miSuscripcion?.renovacionAutomatica;
  }

  obtenerMensajeError(error: any, fallback: string): string {
    return error?.error?.message ||
      error?.error?.mensaje ||
      error?.error?.error ||
      fallback;
  }

  irLogin(): void {
    this.router.navigate(['/login']);
  }

  irRegistro(): void {
    this.router.navigate(['/register']);
  }

  volver(): void {
    if (!this.autenticado) {
      this.router.navigate(['/login']);
      return;
    }

    const roles = this.authService.obtenerRoles();

    if (roles.includes('ROLE_EMPLEADOR')) {
      this.router.navigate(['/empleador/perfil']);
      return;
    }

    if (roles.includes('ROLE_EMPLEADO')) {
      this.router.navigate(['/empleado/perfil']);
      return;
    }

    this.router.navigate(['/login']);
  }
}
