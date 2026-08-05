import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { authInterceptor } from './auth-interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('agrega Bearer token a rutas protegidas', () => {
    localStorage.setItem('token', 'jwt-prueba');
    http.get('/usuarios/cuenta').subscribe();

    const request = httpTesting.expectOne('/usuarios/cuenta');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-prueba');
    request.flush({});
  });

  it('no agrega token a login ni recuperación de contraseña', () => {
    localStorage.setItem('token', 'jwt-prueba');
    http.post('/usuarios/password/solicitar-codigo', {}).subscribe();

    const request = httpTesting.expectOne('/usuarios/password/solicitar-codigo');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('mantiene público el Libro de Reclamaciones', () => {
    localStorage.setItem('token', 'jwt-prueba');
    http.post('/reclamos', {}).subscribe();

    const request = httpTesting.expectOne('/reclamos');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });
});
