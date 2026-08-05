import { describe, expect, it } from 'vitest';
import { EmpleadoService } from './empleado';

describe('EmpleadoService', () => {
  it('expone el servicio utilizado por la aplicación', () => {
    expect(EmpleadoService).toBeDefined();
  });
});
