import { describe, expect, it } from 'vitest';
import { Login } from './login';

describe('Login', () => {
  it('expone el componente de inicio de sesión', () => {
    expect(Login).toBeDefined();
  });
});
