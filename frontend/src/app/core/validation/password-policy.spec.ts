import { describe, expect, it } from 'vitest';

import {
  PASSWORD_MIN_LENGTH,
  PASSWORD_SPECIAL_CHARACTERS,
  cumplePoliticaPassword
} from './password-policy';

describe('política de contraseñas', () => {
  it('acepta una contraseña que cumple todos los requisitos', () => {
    expect(cumplePoliticaPassword('Prueba123!')).toBe(true);
  });

  it('rechaza contraseñas sin mayúscula, minúscula, número o carácter especial', () => {
    expect(cumplePoliticaPassword('prueba123!')).toBe(false);
    expect(cumplePoliticaPassword('PRUEBA123!')).toBe(false);
    expect(cumplePoliticaPassword('PruebaABC!')).toBe(false);
    expect(cumplePoliticaPassword('Prueba1234')).toBe(false);
  });

  it('rechaza contraseñas demasiado cortas', () => {
    expect(PASSWORD_MIN_LENGTH).toBe(8);
    expect(cumplePoliticaPassword('Aa1!')).toBe(false);
  });

  it('mantiene sincronizados los caracteres especiales anunciados', () => {
    for (const caracter of PASSWORD_SPECIAL_CHARACTERS) {
      expect(cumplePoliticaPassword(`Prueba12${caracter}`)).toBe(true);
    }
  });
});
