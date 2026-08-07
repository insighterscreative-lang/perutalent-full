import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';

import { DatosClientePago, MiSuscripcion, PlanSuscripcion, UsoPlanUsuario } from 'src/app/core/models/suscripcion';
import { AuthService } from 'src/app/core/services/auth';
import { SuscripcionService } from 'src/app/core/services/suscripcion';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

declare global {
  interface Window {
    CulqiCheckout: any;
  }
}

@Component({
  selector: 'app-suscripciones',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
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
  culqiEnabled = false;
  planPendientePago?: PlanSuscripcion;

  mostrarModalDatosPago = false;
  planFormularioPago?: PlanSuscripcion;
  mensajeErrorFormulario = '';
  datosClientePago: DatosClientePago = this.crearDatosClienteVacios();

  private culqiCheckout?: any;

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
        this.culqiEnabled = config.enabled;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando configuración de Culqi:', error);
        this.culqiPublicKey = '';
        this.culqiTestMode = false;
        this.culqiEnabled = false;
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
      this.abrirFormularioPago(plan);
      return;
    }

    if (this.miSuscripcion?.esPremium) {
      if (this.miSuscripcion.renovacionAutomatica) {
        this.cancelarSuscripcionPremium();
      } else {
        const vigencia = this.miSuscripcion.fechaFin
          ? ` hasta el ${this.miSuscripcion.fechaFin}`
          : '';

        this.mensajeExito = `La renovación automática ya está cancelada. Mantendrás Premium${vigencia}.`;
        this.cdr.detectChanges();
      }
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

    const fechaFin = this.miSuscripcion?.fechaFin;
    const textoVigencia = fechaFin
      ? ` Mantendrás los beneficios Premium hasta el ${fechaFin}.`
      : '';

    const confirmado = window.confirm(
      `¿Deseas cancelar la renovación automática de tu suscripción Premium? ` +
      `No se realizarán cobros automáticos futuros.${textoVigencia}`
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

        if (suscripcion.esPremium) {
          const vigencia = suscripcion.fechaFin
            ? ` hasta el ${suscripcion.fechaFin}`
            : ' hasta finalizar el periodo pagado';

          this.mensajeExito =
            `La renovación automática fue cancelada. Mantendrás los beneficios Premium${vigencia}. ` +
            `No se realizarán cobros automáticos futuros.`;
        } else {
          this.mensajeExito =
            'La suscripción fue cancelada y tu cuenta quedó en el plan gratuito.';
        }

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

  abrirFormularioPago(plan: PlanSuscripcion): void {
    if (!this.culqiEnabled) {
      this.mensajeError = 'El pago no está disponible en este momento. Intenta nuevamente más tarde.';
      this.cdr.detectChanges();
      return;
    }

    this.planFormularioPago = plan;
    this.mensajeErrorFormulario = '';
    this.datosClientePago = this.crearDatosClienteVacios();
    this.mostrarModalDatosPago = true;
    this.cdr.detectChanges();
  }

  cerrarFormularioPago(): void {
    if (this.pagandoPremium) {
      return;
    }

    this.mostrarModalDatosPago = false;
    this.planFormularioPago = undefined;
    this.mensajeErrorFormulario = '';
    this.cdr.detectChanges();
  }

  confirmarDatosPago(formulario: NgForm): void {
    formulario.control.markAllAsTouched();
    this.mensajeErrorFormulario = '';

    if (!this.planFormularioPago) {
      this.mensajeErrorFormulario = 'No se pudo identificar el plan Premium seleccionado.';
      return;
    }

    const datosNormalizados: DatosClientePago = {
      address: this.normalizarEspacios(this.datosClientePago.address),
      addressCity: this.normalizarEspacios(this.datosClientePago.addressCity),
      aceptaTerminos: !!this.datosClientePago.aceptaTerminos
    };

    const error = this.validarDatosClientePago(datosNormalizados);
    if (error) {
      this.mensajeErrorFormulario = error;
      this.datosClientePago = datosNormalizados;
      this.cdr.detectChanges();
      return;
    }

    const plan = this.planFormularioPago;
    this.datosClientePago = datosNormalizados;
    this.mostrarModalDatosPago = false;
    this.planFormularioPago = undefined;
    this.cdr.detectChanges();

    void this.iniciarPagoPremium(plan);
  }

  private validarDatosClientePago(datos: DatosClientePago): string {
    if (datos.address.length < 5 || datos.address.length > 100) {
      return 'La dirección debe tener entre 5 y 100 caracteres.';
    }

    if (datos.addressCity.length < 2 || datos.addressCity.length > 30) {
      return 'La ciudad debe tener entre 2 y 30 caracteres.';
    }

    if (!datos.aceptaTerminos) {
      return 'Debes aceptar el cobro y la renovación automática mensual para continuar.';
    }

    return '';
  }

  private crearDatosClienteVacios(): DatosClientePago {
    return {
      address: '',
      addressCity: 'Lima',
      aceptaTerminos: false
    };
  }

  private normalizarEspacios(valor: string): string {
    return (valor || '').trim().replace(/\s+/g, ' ');
  }

  async iniciarPagoPremium(plan: PlanSuscripcion): Promise<void> {
    if (!this.culqiEnabled) {
      this.mensajeError = 'El pago no está disponible en este momento. Intenta nuevamente más tarde.';
      this.cdr.detectChanges();
      return;
    }

    if (!this.culqiPublicKey) {
      this.mensajeError = 'No se pudo preparar el formulario de pago. Intenta nuevamente.';
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

      const settings = {
        title: 'PeruTalent Premium',
        currency: plan.moneda || 'PEN',
        amount: plan.precioCentimos
      };

      const paymentMethods = {
        tarjeta: true,
        yape: false,
        billetera: false,
        bancaMovil: false,
        agente: false,
        cuotealo: false
      };

      const options = {
        lang: 'es',
        installments: false,
        modal: true,
        paymentMethods,
        paymentMethodsSort: Object.keys(paymentMethods)
      };

      const client = {
        email: localStorage.getItem('email') || ''
      };

      const appearance = {
        theme: 'default',
        hiddenCulqiLogo: false,
        hiddenBannerContent: false,
        hiddenBanner: false,
        hiddenToolBarAmount: false,
        hiddenEmail: false,
        menuType: 'sidebar',
        buttonCardPayText: 'Suscribirme',
        defaultStyle: {
          buttonBackground: '#22c55e',
          buttonTextColor: '#ffffff',
          priceColor: '#0f172a'
        }
      };

      this.culqiCheckout = new window.CulqiCheckout(this.culqiPublicKey, {
        settings,
        client,
        options,
        appearance
      });

      this.culqiCheckout.culqi = () => {
        this.ngZone.run(() => {
          if (this.culqiCheckout?.token?.id) {
            const tokenId = this.culqiCheckout.token.id;
            this.culqiCheckout.close();
            this.procesarPagoPremium(plan, tokenId);
            return;
          }

          const errorPago = this.culqiCheckout?.error;
          this.mensajeError = this.obtenerMensajePagoSeguro(
            errorPago?.user_message,
            'No pudimos validar los datos de pago. Revísalos e inténtalo nuevamente.'
          );

          this.pagandoPremium = false;
          this.planPendientePago = undefined;
          this.cdr.detectChanges();
        });
      };

      this.culqiCheckout.open();

      // El checkout ya quedó abierto. El bloqueo vuelve a activarse cuando se
      // recibe el token y empieza la llamada real al backend.
      this.pagandoPremium = false;
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Error cargando Culqi Checkout:', error);
      this.mensajeError = 'No se pudo abrir el formulario de pago. Intenta nuevamente.';
      this.pagandoPremium = false;
      this.planPendientePago = undefined;
      this.cdr.detectChanges();
    }
  }

  procesarPagoPremium(plan: PlanSuscripcion, tokenId: string): void {
    if (!tokenId || !tokenId.startsWith('tkn_')) {
      this.mensajeError = 'No se pudo validar la información de pago. Intenta nuevamente.';
      this.planPendientePago = undefined;
      this.cdr.detectChanges();
      return;
    }

    this.pagandoPremium = true;
    this.mensajeError = '';
    this.mensajeExito = 'Procesando tu suscripción...';
    this.cdr.detectChanges();

    this.suscripcionService.pagarPremium({
      idPlan: plan.id,
      tokenId,
      address: this.datosClientePago.address,
      addressCity: this.datosClientePago.addressCity,
      aceptaTerminos: this.datosClientePago.aceptaTerminos
    }).subscribe({
      next: (response) => {
        this.mensajeExito = this.obtenerMensajePagoSeguro(
          response.mensaje,
          'Tu suscripción Premium se activó correctamente.'
        );
        this.pagandoPremium = false;
        this.planPendientePago = undefined;

        this.cargarMiSuscripcion();
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error creando suscripción Premium:', error);
        this.mensajeError = this.obtenerMensajeError(
          error,
          'No se pudo completar la suscripción. Verifica tus datos e inténtalo nuevamente.'
        );
        this.mensajeExito = '';
        this.pagandoPremium = false;
        this.planPendientePago = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  cargarScriptCulqi(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (window.CulqiCheckout) {
        resolve();
        return;
      }

      const scriptExistente = document.getElementById('culqi-checkout-custom-script');

      if (scriptExistente) {
        scriptExistente.addEventListener('load', () => resolve(), { once: true });
        scriptExistente.addEventListener('error', () => reject(), { once: true });
        return;
      }

      const script = document.createElement('script');
      script.id = 'culqi-checkout-custom-script';
      script.src = 'https://js.culqi.com/checkout-js';
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
      return this.miSuscripcion.renovacionAutomatica
        ? 'Cancelar renovación'
        : 'Plan gratuito al vencer';
    }

    if (this.esPremium(plan)) {
      return this.culqiEnabled ? 'Suscribirme a Premium' : 'Pago no disponible';
    }

    return 'Elegir plan';
  }

  tienePremiumRenovable(): boolean {
    return !!this.miSuscripcion?.esPremium && !!this.miSuscripcion?.renovacionAutomatica;
  }

  tienePremiumSinRenovacion(): boolean {
    return !!this.miSuscripcion?.esPremium && !this.miSuscripcion?.renovacionAutomatica;
  }

  esPlanGratuitoPendiente(plan: PlanSuscripcion): boolean {
    return this.tienePremiumSinRenovacion() && !this.esPremium(plan);
  }

  obtenerMensajeError(error: any, fallback: string): string {
    const mensaje = error?.error?.message ||
      error?.error?.mensaje ||
      error?.error?.error ||
      '';

    return this.obtenerMensajePagoSeguro(mensaje, fallback);
  }

  private obtenerMensajePagoSeguro(mensaje: unknown, fallback: string): string {
    if (typeof mensaje !== 'string' || !mensaje.trim()) {
      return fallback;
    }

    const texto = mensaje.trim();
    const contieneDetalleTecnico = /(culqi|webhook|token|customer|subscription|api|http|endpoint|merchant_message|user_message|sk_live|pk_live)/i.test(texto);

    return contieneDetalleTecnico ? fallback : texto;
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
