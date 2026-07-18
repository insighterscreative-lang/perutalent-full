import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/enviroments/enviroment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  message: string;
  data: {
    token: string;
    id: number;
    email: string;
    esEmpleado: boolean;
    esEmpleador: boolean;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = `${environment.apiUrl}usuarios`;

  constructor(private http: HttpClient) {}

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data);
  }

  register(data: any) {
    return this.http.post<any>(`${this.apiUrl}/registro`, data);
  }

  guardarToken(token: string): void {
    localStorage.setItem('token', token);
  }

  obtenerToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  estaAutenticado(): boolean {
    return !!this.obtenerToken();
  }

  obtenerPayloadToken(): any | null {
    const token = this.obtenerToken();
    if (!token) return null;
    try {
      const payloadBase64 = token.split('.')[1];
      if (!payloadBase64) return null;
      const payloadNormalizado = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const payloadJson = decodeURIComponent(
        atob(payloadNormalizado).split('').map(char =>
          '%' + ('00' + char.charCodeAt(0).toString(16)).slice(-2)
        ).join('')
      );
      return JSON.parse(payloadJson);
    } catch (error) {
      console.error('Error leyendo token:', error);
      return null;
    }
  }

  obtenerRoles(): string[] {
    const payload = this.obtenerPayloadToken();
    if (!payload) return [];
    if (Array.isArray(payload.roles)) return payload.roles;
    if (typeof payload.roles === 'string') return [payload.roles];
    if (Array.isArray(payload.authorities)) return payload.authorities;
    return [];
  }

  tieneRol(rol: string): boolean {
    return this.obtenerRoles().includes(rol);
  }

  tieneAlgunRol(rolesPermitidos: string[]): boolean {
    const rolesUsuario = this.obtenerRoles();
    return rolesPermitidos.some(rol => rolesUsuario.includes(rol));
  }
}
