export interface CatalogoItem {
  id: number;
  nombre: string;
}

export interface ApiResponse<T> {
  message: string;
  data: T;
}