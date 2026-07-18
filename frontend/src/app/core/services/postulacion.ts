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

export interface ApiResponse<T> {
  message?: string;
  mensaje?: string;
  data: T;
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

  listarPostulantesPorOferta(idOferta: number): Observable<ApiResponse<PostulacionResponseDTO[]>> {
    return this.http.get<ApiResponse<PostulacionResponseDTO[]>>(
      `${this.apiUrl}/ofertas/${idOferta}/postulantes`
    );
  }

  listarMisOfertasPostuladas(): Observable<ApiResponse<number[]>> {
    return this.http.get<ApiResponse<number[]>>(`${this.apiUrl}/mis-ofertas`);
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