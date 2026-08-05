import { describe, expect, it } from 'vitest';
import { SuscripcionService } from './suscripcion';

describe('SuscripcionService', () => {
  it('expone el servicio utilizado por la aplicación', () => {
    expect(SuscripcionService).toBeDefined();
  });
});
