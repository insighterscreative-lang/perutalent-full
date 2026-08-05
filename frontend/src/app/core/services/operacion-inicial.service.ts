import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/enviroments/enviroment';

export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface ReclamoRequest {
  nombreCompleto: string;
  email: string;
  telefono?: string;
  tipoDocumento: 'DNI' | 'CE' | 'PASAPORTE' | 'OTRO' | '';
  numeroDocumento: string;
  servicioRelacionado: 'PLAN' | 'POSTULACION' | 'OFERTA' | 'CUENTA' | 'OTRO' | '';
  montoReclamado?: number | null;
  tipoSolicitud: 'RECLAMO' | 'QUEJA' | '';
  asunto: string;
  detalle: string;
  pedido: string;
  aceptaDeclaracion: boolean;
}

export interface ReclamoResponse {
  codigoReclamo: string;
  estado: string;
  fechaCreacion: string;
}

export interface ReporteOfertaRequest {
  motivo:
    | 'POSIBLE_ESTAFA'
    | 'INFORMACION_FALSA'
    | 'CONTENIDO_INAPROPIADO'
    | 'OFERTA_DUPLICADA'
    | 'DATOS_CONTACTO_SOSPECHOSOS'
    | 'DISCRIMINACION'
    | 'OTRO'
    | '';
  descripcion: string;
}

export interface ReporteOfertaResponse {
  idReporte: number;
  estado: string;
  fechaCreacion: string;
}

export interface ReporteProblemaTecnicoRequest {
  nombreCompleto: string;
  email: string;
  tipoProblema:
    | 'ACCESO_CUENTA'
    | 'OFERTAS'
    | 'POSTULACIONES'
    | 'PERFIL'
    | 'PAGOS_SUSCRIPCION'
    | 'ARCHIVOS_CV'
    | 'OTRO'
    | '';
  pantalla: string;
  descripcion: string;
  pasosReproducir?: string;
  informacionAdicional?: string;
  aceptaDeclaracion: boolean;
}

export interface ReporteProblemaTecnicoResponse {
  codigoReporte: string;
  estado: string;
  fechaCreacion: string;
}

@Injectable({
  providedIn: 'root'
})
export class OperacionInicialService {

  private readonly reclamosUrl = `${environment.apiUrl}reclamos`;
  private readonly reportesUrl = `${environment.apiUrl}reportes/ofertas`;
  private readonly problemasUrl = `${environment.apiUrl}reportes-problemas`;

  constructor(private http: HttpClient) {}

  registrarReclamo(
    request: ReclamoRequest
  ): Observable<ApiResponse<ReclamoResponse>> {
    return this.http.post<ApiResponse<ReclamoResponse>>(
      this.reclamosUrl,
      request
    );
  }

  reportarOferta(
    idOferta: number,
    request: ReporteOfertaRequest
  ): Observable<ApiResponse<ReporteOfertaResponse>> {
    return this.http.post<ApiResponse<ReporteOfertaResponse>>(
      `${this.reportesUrl}/${idOferta}`,
      request
    );
  }

  registrarProblemaTecnico(
    request: ReporteProblemaTecnicoRequest
  ): Observable<ApiResponse<ReporteProblemaTecnicoResponse>> {
    return this.http.post<ApiResponse<ReporteProblemaTecnicoResponse>>(
      this.problemasUrl,
      request
    );
  }
}
