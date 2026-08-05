import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { environment } from 'src/enviroments/enviroment';
import { EmpleadoService } from './empleado';
import { EmpleadorService } from './empleador';

describe('servicios de perfiles públicos', () => {
  let empleados: EmpleadoService;
  let empleadores: EmpleadorService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    empleados = TestBed.inject(EmpleadoService);
    empleadores = TestBed.inject(EmpleadorService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('consulta el DTO público del empleado', () => {
    empleados.obtenerPerfilPublico(8).subscribe();
    const request = httpTesting.expectOne(`${environment.apiUrl}empleados/perfil-publico/8`);
    expect(request.request.method).toBe('GET');
    request.flush({ message: 'ok', data: {} });
  });

  it('consulta el DTO público del empleador', () => {
    empleadores.obtenerPerfilPublico(9).subscribe();
    const request = httpTesting.expectOne(`${environment.apiUrl}empleadores/perfil-publico/9`);
    expect(request.request.method).toBe('GET');
    request.flush({ message: 'ok', data: {} });
  });

  it('construye URLs públicas protegidas sin exponer claves S3', () => {
    expect(empleados.obtenerUrlFotoPerfilPublica(8, 'imagenes/foto.png'))
      .toContain('/empleados/perfil-publico/8/foto?v=');
    expect(empleadores.obtenerUrlLogoPublico(9, 'imagenes/logo.png'))
      .toContain('/empleadores/perfil-publico/9/logo?v=');
  });
});
