import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, EmpleadoRequest, EmpleadoResponse } from 'src/app/core/models/empleado';
import { environment } from 'src/enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class EmpleadoService {

  private apiUrl = `${environment.apiUrl}empleados`;

  constructor(private http: HttpClient) {}

  crearPerfil(
    dto: EmpleadoRequest,
    cv?: File | null,
    fotoPerfil?: File | null
  ): Observable<ApiResponse<string>> {
    const formData = this.construirFormDataPerfil(dto, cv, fotoPerfil);
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/perfil`, formData);
  }

  obtenerPerfil(): Observable<ApiResponse<EmpleadoResponse>> {
    return this.http.get<ApiResponse<EmpleadoResponse>>(`${this.apiUrl}/perfil`);
  }

  editarPerfil(
    dto: EmpleadoRequest,
    cv?: File | null,
    fotoPerfil?: File | null
  ): Observable<ApiResponse<string>> {
    const formData = this.construirFormDataPerfil(dto, cv, fotoPerfil);
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/perfil`, formData);
  }

  descargarCvPerfil(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/perfil/cv`, {
      responseType: 'blob'
    });
  }

  obtenerPerfilPublico(idEmpleado: number) {
    return this.http.get<any>(`${this.apiUrl}/perfil-publico/${idEmpleado}`);
  }

  obtenerUrlFotoPerfilPublica(idEmpleado: number, fotoPerfil?: string | null): string {
    if (!fotoPerfil) {
      return '';
    }

    if (/^https?:\/\//i.test(fotoPerfil.trim())) {
      return fotoPerfil;
    }

    return `${this.apiUrl}/perfil-publico/${idEmpleado}/foto?v=${encodeURIComponent(fotoPerfil)}`;
  }

  private construirFormDataPerfil(
    dto: EmpleadoRequest,
    cv?: File | null,
    fotoPerfil?: File | null
  ): FormData {
    const formData = new FormData();

    formData.append(
      'perfil',
      new Blob([JSON.stringify(dto)], {
        type: 'application/json'
      })
    );

    if (cv) {
      formData.append('cv', cv, cv.name);
    }

    if (fotoPerfil) {
      formData.append('fotoPerfil', fotoPerfil, fotoPerfil.name);
    }

    return formData;
  }
}
