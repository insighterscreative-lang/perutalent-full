import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import {
  OperacionInicialService,
  ReporteProblemaTecnicoRequest,
  ReporteProblemaTecnicoResponse
} from 'src/app/core/services/operacion-inicial.service';
import { TopbarComponent } from 'src/app/shared/components/topbar/topbar';

@Component({
  selector: 'app-reportar-problema',
  standalone: true,
  imports: [CommonModule, FormsModule, TopbarComponent],
  templateUrl: './reportar-problema.html',
  styleUrl: './reportar-problema.scss'
})
export class ReportarProblemaComponent {

  reporte: ReporteProblemaTecnicoRequest = this.crearFormularioVacio();

  enviando = false;
  error = '';
  resultado: ReporteProblemaTecnicoResponse | null = null;

  constructor(
    private operacionService: OperacionInicialService,
    private cdr: ChangeDetectorRef
  ) {}

  enviar(form: NgForm): void {
    this.error = '';
    this.resultado = null;

    if (form.invalid || !this.reporte.aceptaDeclaracion) {
      form.control.markAllAsTouched();
      this.error = 'Completa correctamente todos los campos obligatorios.';
      this.subirPagina();
      return;
    }

    this.enviando = true;

    const request: ReporteProblemaTecnicoRequest = {
      ...this.reporte,
      nombreCompleto: this.reporte.nombreCompleto.trim(),
      email: this.reporte.email.trim().toLowerCase(),
      pantalla: this.reporte.pantalla.trim(),
      descripcion: this.reporte.descripcion.trim(),
      pasosReproducir: this.reporte.pasosReproducir?.trim() || undefined,
      informacionAdicional: this.reporte.informacionAdicional?.trim() || undefined
    };

    this.operacionService.registrarProblemaTecnico(request).subscribe({
      next: (response) => {
        this.resultado = response.data;
        this.enviando = false;
        this.reporte = this.crearFormularioVacio();
        form.resetForm(this.reporte);
        this.cdr.detectChanges();
        this.subirPagina();
      },
      error: (err) => {
        this.error = err?.error?.message || 'No se pudo registrar el problema. Inténtalo nuevamente.';
        this.enviando = false;
        this.cdr.detectChanges();
        this.subirPagina();
      }
    });
  }

  private crearFormularioVacio(): ReporteProblemaTecnicoRequest {
    return {
      nombreCompleto: '',
      email: '',
      tipoProblema: '',
      pantalla: '',
      descripcion: '',
      pasosReproducir: '',
      informacionAdicional: '',
      aceptaDeclaracion: false
    };
  }

  private subirPagina(): void {
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 0);
  }
}
