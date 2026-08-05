import { describe, expect, it } from 'vitest';
import { CatalogoService } from './catalogo';

describe('CatalogoService', () => {
  it('expone el servicio utilizado por la aplicación', () => {
    expect(CatalogoService).toBeDefined();
  });
});
