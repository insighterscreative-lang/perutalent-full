import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';

import { AuthService } from '../services/auth';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const token = this.authService.obtenerToken();

    if (!token) {
      this.router.navigate(['/login']);
      return false;
    }

    const rolesPermitidos = route.data['roles'] as string[] | undefined;

    if (!rolesPermitidos || rolesPermitidos.length === 0) {
      return true;
    }

    const tienePermiso = this.authService.tieneAlgunRol(rolesPermitidos);

    if (tienePermiso) {
      return true;
    }

    const rolesUsuario = this.authService.obtenerRoles();

    if (rolesUsuario.includes('ROLE_EMPLEADO')) {
      this.router.navigate(['/empleado/perfil']);
      return false;
    }

    if (rolesUsuario.includes('ROLE_EMPLEADOR')) {
      this.router.navigate(['/empleador/perfil']);
      return false;
    }

    this.router.navigate(['/login']);
    return false;
  }
}