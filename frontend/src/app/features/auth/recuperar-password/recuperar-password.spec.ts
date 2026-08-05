import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { RecuperarPasswordComponent } from './recuperar-password';

describe('RecuperarPasswordComponent', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.spyOn(window, 'scrollTo').mockImplementation(() => undefined);
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  function crearComponente() {
    const auth: any = {
      solicitarCodigoRecuperacion: vi.fn(() => of({})),
      verificarCodigoRecuperacion: vi.fn(() => of({})),
      restablecerPassword: vi.fn(() => of({}))
    };
    const router: any = { navigate: vi.fn() };
    const cdr: any = { detectChanges: vi.fn() };
    const component = new RecuperarPasswordComponent(auth, router, cdr);
    return { component, auth, router, cdr };
  }

  it('muestra inmediatamente el paso del código al solicitar recuperación', () => {
    const { component, auth } = crearComponente();
    component.email = ' PERSONA@CORREO.COM ';

    component.solicitarCodigo();

    expect(component.paso).toBe('codigo');
    expect(component.email).toBe('persona@correo.com');
    expect(component.segundosRestantes).toBe(120);
    expect(auth.solicitarCodigoRecuperacion).toHaveBeenCalledWith({ email: 'persona@correo.com' });
    component.ngOnDestroy();
  });

  it('cambia inmediatamente al formulario de contraseña tras verificar el código', () => {
    const { component, auth, cdr } = crearComponente();
    component.email = 'persona@correo.com';
    component.codigo = '123456';
    component.segundosRestantes = 100;
    component.paso = 'codigo';

    component.verificarCodigo();

    expect(auth.verificarCodigoRecuperacion).toHaveBeenCalled();
    expect(component.paso).toBe('password');
    expect(cdr.detectChanges).toHaveBeenCalled();
  });

  it('no envía una contraseña débil', () => {
    const { component, auth } = crearComponente();
    component.nuevaPassword = 'abcdef12';
    component.confirmarPassword = 'abcdef12';

    component.restablecerPassword();

    expect(auth.restablecerPassword).not.toHaveBeenCalled();
    expect(component.error).toContain('al menos 8 caracteres');
  });
});
