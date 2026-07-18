import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, EmpleadorRequest, EmpleadorResponse } from 'src/app/core/models/empleador';
import { environment } from 'src/enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class EmpleadorService {

  private apiUrl = `${environment.apiUrl}empleadores`;

  constructor(private http: HttpClient) {}

  crearPerfil(dto: EmpleadorRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/perfil`, dto);
  }

  obtenerPerfil(): Observable<ApiResponse<EmpleadorResponse>> {
    return this.http.get<ApiResponse<EmpleadorResponse>>(`${this.apiUrl}/perfil`);
  }

  editarPerfil(dto: EmpleadorRequest): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/perfil`, dto);
  }
}
