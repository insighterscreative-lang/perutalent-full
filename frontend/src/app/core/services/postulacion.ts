import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/enviroments/enviroment';

export interface PostulacionResponseDTO {
  idPostulacion: number;

  idEmpleado: number;
  nombreEmpleado: string;
  apellidoEmpleado: string;
  emailEmpleado: string;
  telefonoEmpleado: string;

  idDistrito: number | null;
  distrito: string | null;

  modalidadIds: number[];
  modalidades: string[];

  habilidadIds: number[];
  habilidades: string[];

  herramientaIds: number[];
  herramientas: string[];

  fechaPostulacion: string;
  estadoPostulacion: string;

  cvUrl: string;

  empleadoPremium: boolean;
  planEmpleado: string;
}


export interface MiPostulacionResponseDTO {
  idPostulacion: number;
  idOferta: number;

  tituloOferta: string;
  idEmpleador: number | null;
  empleador: string | null;

  distrito: string | null;
  categoria: string | null;
  modalidad: string | null;
  experiencia: string | null;
  tipoDuracion: string | null;

  cantidadDuracion: number | null;
  montoTotal: number | null;

  descripcionOferta: string | null;
  tareasEspecificas: string | null;
  habilidades: { id: number; nombre: string }[];

  fechaPublicacion: string | null;
  fechaTerminoPostulacion: string | null;
  estadoOferta: string;
  ofertaVencida: boolean;
  ofertaFinalizada: boolean;

  fechaPostulacion: string;
  estadoPostulacion: string;
  cvDisponible: boolean;
}

export interface PaginaResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiResponse<T> {
  message?: string;
  mensaje?: string;
  data: T;
}

export interface OpcionFiltroPostulante {
  id: number;
  nombre: string;
}

export interface FiltrosPostulantesResponse {
  distritos: OpcionFiltroPostulante[];
  modalidades: OpcionFiltroPostulante[];
  habilidades: OpcionFiltroPostulante[];
  herramientas: OpcionFiltroPostulante[];
}

export interface FiltrosPostulantesRequest {
  estado?: string;
  texto?: string;
  distritoId?: number;
  modalidadId?: number;
  habilidadId?: number;
  herramientaId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class PostulacionService {

  private apiUrl = `${environment.apiUrl}postulaciones`;

  constructor(private http: HttpClient) {}

  postular(idOferta: number, usarCvPerfil: boolean, cv?: File): Observable<any> {
    const formData = new FormData();

    formData.append('usarCvPerfil', String(usarCvPerfil));

    if (!usarCvPerfil && cv) {
      formData.append('cv', cv, cv.name);
    }

    return this.http.post(`${this.apiUrl}/ofertas/${idOferta}`, formData);
  }

  listarPostulantesPorOferta(
    idOferta: number,
    filtros: FiltrosPostulantesRequest = {},
    page = 0,
    size = 8
  ): Observable<ApiResponse<PaginaResponse<PostulacionResponseDTO>>> {
    const params: Record<string, string | number> = {
      page,
      size,
      estado: filtros.estado || 'TODOS',
      texto: filtros.texto || ''
    };

    if (filtros.distritoId) params['distritoId'] = filtros.distritoId;
    if (filtros.modalidadId) params['modalidadId'] = filtros.modalidadId;
    if (filtros.habilidadId) params['habilidadId'] = filtros.habilidadId;
    if (filtros.herramientaId) params['herramientaId'] = filtros.herramientaId;

    return this.http.get<ApiResponse<PaginaResponse<PostulacionResponseDTO>>>(
      `${this.apiUrl}/ofertas/${idOferta}/postulantes`,
      { params }
    );
  }

  listarFiltrosPostulantes(
    idOferta: number
  ): Observable<ApiResponse<FiltrosPostulantesResponse>> {
    return this.http.get<ApiResponse<FiltrosPostulantesResponse>>(
      `${this.apiUrl}/ofertas/${idOferta}/postulantes/filtros`
    );
  }

  listarMisOfertasPostuladas(): Observable<ApiResponse<number[]>> {
    return this.http.get<ApiResponse<number[]>>(`${this.apiUrl}/mis-ofertas`);
  }

  listarMisPostulaciones(
    page = 0,
    size = 6
  ): Observable<ApiResponse<PaginaResponse<MiPostulacionResponseDTO>>> {
    return this.http.get<ApiResponse<PaginaResponse<MiPostulacionResponseDTO>>>(
      `${this.apiUrl}/mis-postulaciones`,
      { params: { page, size } }
    );
  }

  aceptarPostulacion(idPostulacion: number): Observable<ApiResponse<PostulacionResponseDTO>> {
    return this.http.put<ApiResponse<PostulacionResponseDTO>>(
      `${this.apiUrl}/${idPostulacion}/aceptar`,
      {}
    );
  }

  rechazarPostulacion(idPostulacion: number): Observable<ApiResponse<PostulacionResponseDTO>> {
    return this.http.put<ApiResponse<PostulacionResponseDTO>>(
      `${this.apiUrl}/${idPostulacion}/rechazar`,
      {}
    );
  }

  descargarCv(idPostulacion: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${idPostulacion}/cv`,
      { responseType: 'blob' }
    );
  }
}