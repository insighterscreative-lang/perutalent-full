import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { environment } from 'src/enviroments/enviroment';
import { OperacionInicialService } from './operacion-inicial.service';

describe('OperacionInicialService', () => {
  let service: OperacionInicialService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(OperacionInicialService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('registra reclamos en el endpoint público', () => {
    const payload: any = { nombreCompleto: 'Persona', email: 'p@correo.com' };
    service.registrarReclamo(payload).subscribe();
    const request = httpTesting.expectOne(`${environment.apiUrl}reclamos`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBe(payload);
    request.flush({ message: 'ok', data: {} });
  });

  it('reporta una oferta específica', () => {
    const payload: any = { motivo: 'POSIBLE_ESTAFA', descripcion: 'Detalle' };
    service.reportarOferta(25, payload).subscribe();
    const request = httpTesting.expectOne(`${environment.apiUrl}reportes/ofertas/25`);
    expect(request.request.method).toBe('POST');
    request.flush({ message: 'ok', data: {} });
  });

  it('registra problemas técnicos', () => {
    const payload: any = { tipoProblema: 'OTRO', descripcion: 'Detalle' };
    service.registrarProblemaTecnico(payload).subscribe();
    const request = httpTesting.expectOne(`${environment.apiUrl}reportes-problemas`);
    expect(request.request.method).toBe('POST');
    request.flush({ message: 'ok', data: {} });
  });
});
