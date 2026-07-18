export interface EmpleadorRequest {
  tipoEmpleador: string;
  nombreComercial: string;
  razonSocial: string;
  tipoDoc: string;
  numDoc: string;
  logoEmpleador?: string;
  descripcionNegocio?: string;
  aniosOperacion: number;

  sitioWeb?: string;
  correoContacto?: string;
  telefonoContacto?: string;

  categoriasId?: number[];
}

export interface HabilidadTrabajo {
  id?: number;
  nombre: string;
}

export interface TrabajoPerfil {
  idOferta: number;
  titulo: string;
  descripcion: string;
  categoria: string;
  modalidad: string;
  estadoOferta: string;

  empleador?: string;
  distrito?: string;
  montoTotal?: number;
  experiencia?: string;
  tipoDuracion?: string;
  fechaPublicacion?: string;

  habilidades?: HabilidadTrabajo[];
}

export interface EmpleadorResponse {
  idEmpleador: number;
  tipoEmpleador: string;
  nombreComercial: string;
  razonSocial: string;
  tipoDoc: string;
  numDoc: string;
  logoEmpleador: string;
  descripcionNegocio: string;
  aniosOperacion: number;
  correo: string;

  sitioWeb: string;
  correoContacto: string;
  telefonoContacto: string;

  categorias: string[];
  modalidadesContratacion: string[];

  trabajosActivos: number;
  trabajosFinalizados: number;

  trabajosActivosDetalle: TrabajoPerfil[];
  trabajosFinalizadosDetalle: TrabajoPerfil[];
}

export interface ApiResponse<T> {
  message: string;
  data: T;
}