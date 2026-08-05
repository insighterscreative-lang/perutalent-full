import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import {
  OperacionInicialService,
  ReclamoRequest,
  ReclamoResponse
} from 'src/app/core/services/operacion-inicial.service';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-libro-reclamaciones',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './libro-reclamaciones.html',
  styleUrl: './libro-reclamaciones.scss'
})
export class LibroReclamacionesComponent {

  reclamo: ReclamoRequest = this.crearFormularioVacio();

  enviando = false;
  error = '';
  resultado: ReclamoResponse | null = null;

  constructor(
    private operacionService: OperacionInicialService,
    private cdr: ChangeDetectorRef
  ) {}

  enviarReclamo(form: NgForm): void {
    this.error = '';
    this.resultado = null;

    if (form.invalid || !this.reclamo.aceptaDeclaracion) {
      form.control.markAllAsTouched();
      this.error = 'Completa correctamente todos los campos obligatorios.';
      this.subirPagina();
      return;
    }

    this.enviando = true;

    const request: ReclamoRequest = {
      ...this.reclamo,
      nombreCompleto: this.reclamo.nombreCompleto.trim(),
      email: this.reclamo.email.trim().toLowerCase(),
      telefono: this.reclamo.telefono?.trim() || undefined,
      numeroDocumento: this.reclamo.numeroDocumento.trim(),
      asunto: this.reclamo.asunto.trim(),
      detalle: this.reclamo.detalle.trim(),
      pedido: this.reclamo.pedido.trim(),
      montoReclamado: this.reclamo.montoReclamado ?? null
    };

    this.operacionService.registrarReclamo(request).subscribe({
      next: (response) => {
        this.resultado = response.data;
        this.enviando = false;
        this.reclamo = this.crearFormularioVacio();
        form.resetForm(this.reclamo);
        this.cdr.detectChanges();
        this.subirPagina();
      },
      error: (err) => {
        this.error = err?.error?.message || 'No se pudo registrar la solicitud. Inténtalo nuevamente.';
        this.enviando = false;
        this.cdr.detectChanges();
        this.subirPagina();
      }
    });
  }

  private crearFormularioVacio(): ReclamoRequest {
    return {
      nombreCompleto: '',
      email: '',
      telefono: '',
      tipoDocumento: '',
      numeroDocumento: '',
      servicioRelacionado: '',
      montoReclamado: null,
      tipoSolicitud: '',
      asunto: '',
      detalle: '',
      pedido: '',
      aceptaDeclaracion: false
    };
  }

  private subirPagina(): void {
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 0);
  }
}
