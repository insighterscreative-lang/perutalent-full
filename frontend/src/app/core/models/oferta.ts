export interface HabilidadOferta {
  id: number;
  nombre: string;
}

export interface Oferta {
  id: number;
  titulo: string;
  codigoInterno: string;
  empleador: string;
  idEmpleador: number;
  distrito: string;
  idDistrito: number;
  montoTotal: number;
  tipoDuracion: string;
  idDuracion: number;
  categoria: string;
  idCategoria: number;
  modalidad: string;
  idMod: number;
  experiencia: string;
  idExperienciaRequerida: number;
  descripcion: string;
  tareasEspecificas: string;
  cantidadDuracion: number;
  fechaTerminoPostulacion: string;
  estadoOferta: string;
  habilidades: HabilidadOferta[];
  fechaPublicacion: string;
}

export interface OfertaRequest {
  titulo: string;
  codigoInterno: string;
  descripcion: string;
  tareasEspecificas: string;
  cantidadDuracion: number | null;
  montoTotal: number | null;
  fechaTerminoPostulacion: string;
  idCategoria: number;
  idMod: number;
  idDistrito: number;
  idExperienciaRequerida: number;
  idDuracion: number;
  habilidadesId: number[];
}