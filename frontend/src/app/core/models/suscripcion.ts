export interface PlanSuscripcion {
  id: number;
  nombrePlan: string;
  descripcion: string;
  precioCentimos: number;
  moneda: string;
  duracionDias: number;
  maxPostulacionesMes: number | null;
  maxRecomendaciones: number | null;
  maxOfertasActivas: number | null;
  prioridadPostulante: boolean;
  ofertasDestacadas: boolean;
  activo: boolean;
}

export interface MiSuscripcion {
  idSuscripcion: number;
  idPlan: number;
  nombrePlan: string;
  estadoSuscripcion: string;
  fechaInicio: string;
  fechaFin: string | null;
  esPremium: boolean;
  maxPostulacionesMes: number | null;
  maxRecomendaciones: number | null;
  maxOfertasActivas: number | null;
  prioridadPostulante: boolean;
  ofertasDestacadas: boolean;
  renovacionAutomatica: boolean;
  fechaProximoCobro: string | null;
  fechaUltimoCobro: string | null;
  fechaCancelacion: string | null;
}

export interface UsoPlanUsuario {
  id: number;
  periodo: string;
  postulacionesUsadas: number;
  ofertasPublicadas: number;
  recomendacionesVistas: number;
  maxPostulacionesMes: number | null;
  maxOfertasActivas: number | null;
  maxRecomendaciones: number | null;
  postulacionesRestantes: number | null;
  ofertasRestantes: number | null;
  recomendacionesRestantes: number | null;
}

export interface CulqiConfig {
  publicKey: string;
  testMode: boolean;
}

export interface PagoPremiumRequest {
  idPlan: number;
  tokenId: string;
}

export interface PagoSuscripcionResponse {
  idPago: number;
  estadoPago: string;
  mensaje: string;

  idPlan: number;
  nombrePlan: string;

  montoCentimos: number;
  moneda: string;

  culqiChargeId: string | null;
  culqiSubscriptionId: string;

  renovacionAutomatica: boolean;

  fechaInicio: string;
  fechaFin: string | null;
  fechaProximoCobro: string | null;
}
