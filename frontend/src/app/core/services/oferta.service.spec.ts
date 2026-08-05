import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { environment } from 'src/enviroments/enviroment';
import { OfertaService } from './oferta.service';

describe('OfertaService', () => {
  let service: OfertaService;
  let httpTesting: HttpTestingController;
  const base = `${environment.apiUrl}ofertas-laborales`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OfertaService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('solicita ofertas paginadas', () => {
    service.getOfertas(2, 12).subscribe();
    const request = httpTesting.expectOne(req =>
      req.url === `${base}/paginadas` &&
      req.params.get('page') === '2' && req.params.get('size') === '12'
    );
    expect(request.request.method).toBe('GET');
    request.flush({ content: [], page: 2, size: 12, totalElements: 0, totalPages: 0, first: false, last: true });
  });

  it('envía filtros limpios y paginación al backend', () => {
    service.filtrarOfertas({
      categoria: 3,
      modalidad: 2,
      palabraClave: '  java  ',
      ubicacion: '  Lima ',
      sortBy: 'fechaPublicacion'
    }, 1, 12).subscribe();

    const request = httpTesting.expectOne(req => req.url === `${base}/filtrar/paginadas`);
    expect(request.request.params.get('categoria')).toBe('3');
    expect(request.request.params.get('modalidad')).toBe('2');
    expect(request.request.params.get('palabraClave')).toBe('java');
    expect(request.request.params.get('ubicacion')).toBe('Lima');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('order')).toBe('asc');
    request.flush({ content: [], page: 1, size: 12, totalElements: 0, totalPages: 0, first: false, last: true });
  });

  it('separa Mis ofertas por estado y página', () => {
    service.getMisOfertas('FINALIZADAS', 3, 6).subscribe();
    const request = httpTesting.expectOne(req => req.url === `${base}/mias`);
    expect(request.request.params.get('estado')).toBe('FINALIZADAS');
    expect(request.request.params.get('page')).toBe('3');
    expect(request.request.params.get('size')).toBe('6');
    request.flush({ message: 'ok', data: { content: [] } });
  });
});
