import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/enviroments/enviroment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthData {
  token: string;
  id: number;
  email: string;
  esEmpleado: boolean;
  esEmpleador: boolean;
}

export interface AuthResponse {
  message: string;
  data: AuthData;
}

export interface CuentaUsuario {
  id: number;
  email: string;
  esEmpleado: boolean;
  esEmpleador: boolean;
  fechaRegistro: string;
}

export interface CuentaUsuarioResponse {
  message: string;
  data: CuentaUsuario;
}

export interface ActualizarEmailRequest {
  nuevoEmail: string;
  passwordActual: string;
}

export interface ActualizarPasswordRequest {
  passwordActual: string;
  nuevaPassword: string;
  confirmarPassword: string;
}

export interface EliminarCuentaRequest {
  passwordActual: string;
  confirmacion: string;
}

export interface SolicitarRecuperacionPasswordRequest {
  email: string;
}

export interface VerificarCodigoRecuperacionRequest {
  email: string;
  codigo: string;
}

export interface RestablecerPasswordRequest {
  email: string;
  codigo: string;
  nuevaPassword: string;
  confirmarPassword: string;
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


  solicitarCodigoRecuperacion(data: SolicitarRecuperacionPasswordRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/password/solicitar-codigo`, data);
  }

  verificarCodigoRecuperacion(data: VerificarCodigoRecuperacionRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/password/verificar-codigo`, data);
  }

  restablecerPassword(data: RestablecerPasswordRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/password/restablecer`, data);
  }

  obtenerCuenta(): Observable<CuentaUsuarioResponse> {
    return this.http.get<CuentaUsuarioResponse>(`${this.apiUrl}/cuenta`);
  }

  actualizarEmail(data: ActualizarEmailRequest): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(`${this.apiUrl}/cuenta/email`, data);
  }

  actualizarPassword(data: ActualizarPasswordRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/cuenta/password`, data);
  }

  eliminarCuenta(data: EliminarCuentaRequest): Observable<any> {
    return this.http.request<any>('delete', `${this.apiUrl}/cuenta`, { body: data });
  }

  guardarToken(token: string): void {
    localStorage.setItem('token', token);
  }

  guardarSesion(data: AuthData): void {
    this.guardarToken(data.token);
    localStorage.setItem('idUsuario', String(data.id));
    localStorage.setItem('email', data.email);
    localStorage.setItem('esEmpleado', String(data.esEmpleado));
    localStorage.setItem('esEmpleador', String(data.esEmpleador));
  }

  obtenerToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  limpiarSesion(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('idUsuario');
    localStorage.removeItem('email');
    localStorage.removeItem('esEmpleado');
    localStorage.removeItem('esEmpleador');
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
