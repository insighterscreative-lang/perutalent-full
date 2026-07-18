import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, CatalogoItem } from '../models/catalogo';
import { environment } from 'src/enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class CatalogoService {

  private apiUrl = `${environment.apiUrl}catalogos`;

  constructor(private http: HttpClient) {}

  listarDepartamentos(): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(`${this.apiUrl}/departamentos`);
  }

  listarProvincias(departamentoId: number): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(
      `${this.apiUrl}/provincias?departamentoId=${departamentoId}`
    );
  }

  listarDistritos(provinciaId: number): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(
      `${this.apiUrl}/distritos?provinciaId=${provinciaId}`
    );
  }

  listarCategorias(): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(`${this.apiUrl}/categorias`);
  }

  listarHabilidades(): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(`${this.apiUrl}/habilidades`);
  }

  listarHerramientas(): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(`${this.apiUrl}/herramientas`);
  }

  listarModalidades(): Observable<ApiResponse<CatalogoItem[]>> {
    return this.http.get<ApiResponse<CatalogoItem[]>>(`${this.apiUrl}/modalidades`);
  }
}
