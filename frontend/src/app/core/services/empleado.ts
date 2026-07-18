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

  crearPerfil(dto: EmpleadoRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/perfil`, dto);
  }

  obtenerPerfil(): Observable<ApiResponse<EmpleadoResponse>> {
    return this.http.get<ApiResponse<EmpleadoResponse>>(`${this.apiUrl}/perfil`);
  }

  editarPerfil(dto: EmpleadoRequest): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.apiUrl}/perfil`, dto);
  }

  obtenerPerfilPublico(idEmpleado: number) {
    return this.http.get<any>(`${this.apiUrl}/perfil-publico/${idEmpleado}`);
  }
}
