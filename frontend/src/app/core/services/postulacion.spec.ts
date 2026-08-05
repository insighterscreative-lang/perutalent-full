import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { environment } from 'src/enviroments/enviroment';
import { PostulacionService } from './postulacion';

describe('PostulacionService', () => {
  let service: PostulacionService;
  let httpTesting: HttpTestingController;
  const base = `${environment.apiUrl}postulaciones`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PostulacionService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('pagina Mis postulaciones', () => {
    service.listarMisPostulaciones(2, 6).subscribe();
    const request = httpTesting.expectOne(req => req.url === `${base}/mis-postulaciones`);
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('6');
    request.flush({ data: { content: [] } });
  });

  it('pagina y filtra postulantes recibidos', () => {
    service.listarPostulantesPorOferta(50, {
      estado: 'PENDIENTE', texto: 'Ana', distritoId: 7, habilidadId: 3
    }, 1, 8).subscribe();

    const request = httpTesting.expectOne(req => req.url === `${base}/ofertas/50/postulantes`);
    expect(request.request.params.get('estado')).toBe('PENDIENTE');
    expect(request.request.params.get('texto')).toBe('Ana');
    expect(request.request.params.get('distritoId')).toBe('7');
    expect(request.request.params.get('habilidadId')).toBe('3');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('8');
    request.flush({ data: { content: [] } });
  });

  it('postula usando el CV del perfil', () => {
    service.postular(10, true).subscribe();
    const request = httpTesting.expectOne(`${base}/ofertas/10`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBe(true);
    expect((request.request.body as FormData).get('usarCvPerfil')).toBe('true');
    request.flush({});
  });

  it('descarga el CV como blob desde el endpoint protegido', () => {
    service.descargarCv(99).subscribe();
    const request = httpTesting.expectOne(`${base}/99/cv`);
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob(['pdf']));
  });
});
