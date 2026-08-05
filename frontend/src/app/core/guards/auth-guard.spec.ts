import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { describe, beforeEach, expect, it, vi } from 'vitest';

import { AuthGuard } from './auth-guard';
import { AuthService } from '../services/auth';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let obtenerToken: ReturnType<typeof vi.fn>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    obtenerToken = vi.fn();
    navigate = vi.fn();

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: { obtenerToken } },
        { provide: Router, useValue: { navigate } }
      ]
    });

    guard = TestBed.inject(AuthGuard);
  });

  it('permite el acceso cuando existe token', () => {
    obtenerToken.mockReturnValue('jwt');

    expect(guard.canActivate()).toBe(true);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('redirige al login cuando no existe token', () => {
    obtenerToken.mockReturnValue(null);

    expect(guard.canActivate()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
