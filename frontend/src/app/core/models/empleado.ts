export interface EmpleadoRequest {
  nombre: string;
  apellido: string;
  tipoDoc: string;
  numDoc: string;
  fechaNacimiento: string;
  genero: string;
  telefono: string;
  idDistrito: number;
  nacionalidad: string;
  descripcion?: string;
  curriculum?: string;
  fotoPerfil?: string;
  idiomas?: string[];
  disponibilidadEquipo?: string;
  habilidadesId?: number[];
  categoriasId?: number[];
  herramientasId?: number[];
  modalidadesId?: number[];
}

export interface TrabajoPerfil {
  idOferta: number;
  titulo: string;
  descripcion: string;
  categoria: string;
  modalidad: string;
  estadoOferta: string;
}

export interface EmpleadoResponse {
  idEmpleado: number;
  nombre: string;
  apellido: string;
  tipoDoc: string;
  numDoc: string;
  fechaNacimiento: string;
  genero: string;
  fotoPerfil: string;
  idDepartamento: number;
  idProvincia: number;
  idDistrito: number;
  distrito: string;
  nacionalidad: string;
  telefono: string;
  correo: string;
  descripcion: string;
  curriculum: string;
  idiomas: string[];
  habilidades: string[];
  categorias: string[];
  disponibilidadEquipo: string;
  herramientas: string[];
  modalidades: string[];
  trabajosActivos: number;
  trabajosFinalizados: number;
  trabajosActivosDetalle: TrabajoPerfil[];
  trabajosFinalizadosDetalle: TrabajoPerfil[];
}

export interface ApiResponse<T> {
  message: string;
  data: T;
}