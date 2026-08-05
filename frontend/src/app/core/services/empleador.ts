import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, EmpleadorPublicoResponse, EmpleadorRequest, EmpleadorResponse } from 'src/app/core/models/empleador';
import { environment } from 'src/enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class EmpleadorService {

  private apiUrl = `${environment.apiUrl}empleadores`;

  constructor(private http: HttpClient) {}

  crearPerfil(dto: EmpleadorRequest, logo?: File | null): Observable<ApiResponse<string>> {
    const formData = this.construirFormDataPerfil(dto, logo);
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/perfil`, formData);
  }

  obtenerPerfil(): Observable<ApiResponse<EmpleadorResponse>> {
    return this.http.get<ApiResponse<EmpleadorResponse>>(`${this.apiUrl}/perfil`);
  }

  obtenerPerfilPublico(idEmpleador: number): Observable<ApiResponse<EmpleadorPublicoResponse>> {
    return this.http.get<ApiResponse<EmpleadorPublicoResponse>>(`${this.apiUrl}/perfil-publico/${idEmpleador}`);
  }

  editarPerfil(dto: EmpleadorRequest, logo?: File | null): Observable<ApiResponse<string>> {
    const formData = this.construirFormDataPerfil(dto, logo);
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/perfil`, formData);
  }

  obtenerUrlLogoPublico(idEmpleador: number, logoEmpleador?: string | null): string {
    if (!logoEmpleador) {
      return '';
    }

    if (/^https?:\/\//i.test(logoEmpleador.trim())) {
      return logoEmpleador;
    }

    return `${this.apiUrl}/perfil-publico/${idEmpleador}/logo?v=${encodeURIComponent(logoEmpleador)}`;
  }

  private construirFormDataPerfil(dto: EmpleadorRequest, logo?: File | null): FormData {
    const formData = new FormData();

    formData.append(
      'perfil',
      new Blob([JSON.stringify(dto)], {
        type: 'application/json'
      })
    );

    if (logo) {
      formData.append('logo', logo, logo.name);
    }

    return formData;
  }
}
