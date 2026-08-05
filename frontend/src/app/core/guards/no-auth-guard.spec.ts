import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { describe, beforeEach, afterEach, expect, it, vi } from 'vitest';

import { NoAuthGuard } from './no-auth-guard';

describe('NoAuthGuard', () => {
  let guard: NoAuthGuard;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    localStorage.clear();
    navigate = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        NoAuthGuard,
        { provide: Router, useValue: { navigate } }
      ]
    });
    guard = TestBed.inject(NoAuthGuard);
  });

  afterEach(() => localStorage.clear());

  it('permite acceder al login cuando no existe sesión', () => {
    expect(guard.canActivate()).toBe(true);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('redirige a ofertas cuando el usuario ya inició sesión', () => {
    localStorage.setItem('token', 'jwt');

    expect(guard.canActivate()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/ofertas']);
  });
});
