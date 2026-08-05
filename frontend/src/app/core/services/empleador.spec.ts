import { describe, expect, it } from 'vitest';
import { EmpleadorService } from './empleador';

describe('EmpleadorService', () => {
  it('expone el servicio utilizado por la aplicación', () => {
    expect(EmpleadorService).toBeDefined();
  });
});
