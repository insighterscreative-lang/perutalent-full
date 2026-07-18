import { TestBed } from '@angular/core/testing';

import { Empleador } from './empleador';

describe('Empleador', () => {
  let service: Empleador;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Empleador);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
