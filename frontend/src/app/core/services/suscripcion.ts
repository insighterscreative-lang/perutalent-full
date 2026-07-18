import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from 'src/enviroments/enviroment';
import {
  CulqiConfig,
  MiSuscripcion,
  PagoPremiumRequest,
  PagoSuscripcionResponse,
  PlanSuscripcion,
  UsoPlanUsuario
} from '../models/suscripcion';

@Injectable({
  providedIn: 'root'
})
export class SuscripcionService {

  private apiUrl = `${environment.apiUrl}suscripciones`;
  private pagosUrl = `${environment.apiUrl}pagos/culqi`;

  constructor(private http: HttpClient) {}

  listarPlanes(): Observable<PlanSuscripcion[]> {
    return this.http.get<PlanSuscripcion[]>(`${this.apiUrl}/planes`);
  }

  obtenerMiSuscripcion(): Observable<MiSuscripcion> {
    return this.http.get<MiSuscripcion>(`${this.apiUrl}/mi-suscripcion`);
  }

  obtenerMiUso(): Observable<UsoPlanUsuario> {
    return this.http.get<UsoPlanUsuario>(`${this.apiUrl}/mi-uso`);
  }

  cambiarPlan(idPlan: number): Observable<MiSuscripcion> {
    return this.http.post<MiSuscripcion>(`${this.apiUrl}/cambiar-plan/${idPlan}`, {});
  }

  obtenerConfigCulqi(): Observable<CulqiConfig> {
    return this.http.get<CulqiConfig>(`${this.pagosUrl}/config`);
  }

  pagarPremium(request: PagoPremiumRequest): Observable<PagoSuscripcionResponse> {
    return this.http.post<PagoSuscripcionResponse>(`${this.pagosUrl}/premium`, request);
  }
}