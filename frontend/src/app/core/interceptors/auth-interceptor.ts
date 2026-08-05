import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const esRutaPublica =
    req.url.includes('/usuarios/login') ||
    req.url.includes('/usuarios/registro') ||
    req.url.includes('/usuarios/password/') ||
    req.url.includes('/reclamos') ||
    req.url.includes('/catalogos/') ||
    req.url.includes('/filtros/ofertas');

  if (esRutaPublica) {
    return next(req);
  }

  const token = localStorage.getItem('token');

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next(cloned);
  }

  return next(req);
};