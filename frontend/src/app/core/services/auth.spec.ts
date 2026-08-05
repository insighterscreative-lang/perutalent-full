import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { environment } from 'src/enviroments/enviroment';
import { AuthService } from './auth';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('envía login al endpoint correcto', () => {
    const payload = { email: 'persona@correo.com', password: 'Prueba123!' };
    service.login(payload).subscribe();

    const request = httpTesting.expectOne(`${environment.apiUrl}usuarios/login`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ message: 'ok', data: {} });
  });

  it('usa los tres endpoints públicos de recuperación', () => {
    service.solicitarCodigoRecuperacion({ email: 'persona@correo.com' }).subscribe();
    let request = httpTesting.expectOne(`${environment.apiUrl}usuarios/password/solicitar-codigo`);
    expect(request.request.method).toBe('POST');
    request.flush({});

    service.verificarCodigoRecuperacion({ email: 'persona@correo.com', codigo: '123456' }).subscribe();
    request = httpTesting.expectOne(`${environment.apiUrl}usuarios/password/verificar-codigo`);
    expect(request.request.method).toBe('POST');
    request.flush({});

    service.restablecerPassword({
      email: 'persona@correo.com', codigo: '123456',
      nuevaPassword: 'Nueva123!', confirmarPassword: 'Nueva123!'
    }).subscribe();
    request = httpTesting.expectOne(`${environment.apiUrl}usuarios/password/restablecer`);
    expect(request.request.method).toBe('POST');
    request.flush({});
  });

  it('guarda y limpia toda la sesión sin borrar otras preferencias', () => {
    localStorage.setItem('preferencia-ui', 'claro');
    service.guardarSesion({
      token: 'jwt', id: 9, email: 'persona@correo.com',
      esEmpleado: true, esEmpleador: false
    });

    expect(service.estaAutenticado()).toBe(true);
    expect(localStorage.getItem('idUsuario')).toBe('9');

    service.limpiarSesion();

    expect(service.estaAutenticado()).toBe(false);
    expect(localStorage.getItem('idUsuario')).toBeNull();
    expect(localStorage.getItem('preferencia-ui')).toBe('claro');
  });

  it('lee roles desde el payload JWT', () => {
    const payload = btoa(JSON.stringify({ roles: ['ROLE_EMPLEADO'] }))
      .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    localStorage.setItem('token', `cabecera.${payload}.firma`);

    expect(service.obtenerRoles()).toEqual(['ROLE_EMPLEADO']);
    expect(service.tieneRol('ROLE_EMPLEADO')).toBe(true);
    expect(service.tieneAlgunRol(['ROLE_EMPLEADOR', 'ROLE_EMPLEADO'])).toBe(true);
  });

  it('devuelve roles vacíos ante un token inválido', () => {
    localStorage.setItem('token', 'token-invalido');
    expect(service.obtenerRoles()).toEqual([]);
  });
});
