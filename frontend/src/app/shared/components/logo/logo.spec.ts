import { describe, expect, it } from 'vitest';
import { LogoComponent } from './logo';

describe('LogoComponent', () => {
  it('está disponible para las plantillas', () => {
    expect(LogoComponent).toBeDefined();
  });
});
