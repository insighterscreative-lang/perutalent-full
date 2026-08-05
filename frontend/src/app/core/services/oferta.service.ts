import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/enviroments/enviroment';
import { Oferta, OfertaRequest } from 'src/app/core/models/oferta';

export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface FiltrosOfertas {
  categoria?: number;
  modalidad?: number;
  experiencia?: number;
  montoMin?: number;
  montoMax?: number;
  palabraClave?: string;
  ubicacion?: string;
  sortBy?: string;
  order?: string;
}

export interface SimpleDTO {
  id: number;
  nombre: string;
}

export interface RangoSalarioDTO {
  min: number;
  max: number;
  label: string;
}

export interface FiltrosResponse {
  categorias: SimpleDTO[];
  modalidades: SimpleDTO[];
  experiencias: SimpleDTO[];
  rangosSalario: RangoSalarioDTO[];
}

export interface PaginaResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class OfertaService {

  private apiUrl = `${environment.apiUrl}ofertas-laborales`;
  private filtrosUrl = `${environment.apiUrl}filtros/ofertas`;

  constructor(private http: HttpClient) {}

  crearOferta(dto: OfertaRequest): Observable<ApiResponse<Oferta>> {
    return this.http.post<ApiResponse<Oferta>>(this.apiUrl, dto);
  }

  editarOferta(id: number, dto: OfertaRequest): Observable<ApiResponse<Oferta>> {
    return this.http.put<ApiResponse<Oferta>>(`${this.apiUrl}/${id}`, dto);
  }

  finalizarOferta(id: number): Observable<ApiResponse<Oferta>> {
    return this.http.patch<ApiResponse<Oferta>>(`${this.apiUrl}/${id}/finalizar`, null);
  }

  eliminarOferta(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getOfertas(page = 0, size = 12): Observable<PaginaResponse<Oferta>> {
    return this.http.get<PaginaResponse<Oferta>>(`${this.apiUrl}/paginadas`, {
      params: { page, size }
    });
  }

  getMisOfertas(
    estado: 'ACTIVAS' | 'FINALIZADAS',
    page = 0,
    size = 6
  ): Observable<ApiResponse<PaginaResponse<Oferta>>> {
    return this.http.get<ApiResponse<PaginaResponse<Oferta>>>(`${this.apiUrl}/mias`, {
      params: { estado, page, size }
    });
  }

  getOfertaById(id: number): Observable<Oferta> {
    return this.http.get<Oferta>(`${this.apiUrl}/${id}`);
  }

  getOfertasParaTi(): Observable<ApiResponse<Oferta[]>> {
    return this.http.get<ApiResponse<Oferta[]>>(`${this.apiUrl}/para-ti`);
  }

  getFiltros(): Observable<FiltrosResponse> {
    return this.http.get<FiltrosResponse>(this.filtrosUrl);
  }

  filtrarOfertas(
    filtros: FiltrosOfertas,
    page = 0,
    size = 12
  ): Observable<PaginaResponse<Oferta>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    if (filtros.categoria) params = params.set('categoria', filtros.categoria);
    if (filtros.modalidad) params = params.set('modalidad', filtros.modalidad);
    if (filtros.experiencia) params = params.set('experiencia', filtros.experiencia);
    if (filtros.montoMin !== undefined && filtros.montoMin !== null) params = params.set('montoMin', filtros.montoMin);
    if (filtros.montoMax !== undefined && filtros.montoMax !== null) params = params.set('montoMax', filtros.montoMax);
    if (filtros.palabraClave?.trim()) params = params.set('palabraClave', filtros.palabraClave.trim());
    if (filtros.ubicacion?.trim()) params = params.set('ubicacion', filtros.ubicacion.trim());
    if (filtros.sortBy) {
      params = params.set('sortBy', filtros.sortBy);
      params = params.set('order', filtros.order || 'asc');
    }
    return this.http.get<PaginaResponse<Oferta>>(`${this.apiUrl}/filtrar/paginadas`, { params });
  }
}