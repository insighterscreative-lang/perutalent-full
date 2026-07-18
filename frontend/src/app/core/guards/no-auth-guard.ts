import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class NoAuthGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(): boolean {

    const token = localStorage.getItem('token');

    // 🔥 Si YA está logueado → lo saco de login/register
    if (token) {
      this.router.navigate(['/ofertas']);
      return false;
    }

    return true;
  }
}