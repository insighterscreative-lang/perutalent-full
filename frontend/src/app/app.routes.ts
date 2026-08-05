import { Routes } from '@angular/router';

import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { RecuperarPasswordComponent } from './features/auth/recuperar-password/recuperar-password';

import { AuthGuard } from './core/guards/auth-guard';
import { NoAuthGuard } from './core/guards/no-auth-guard';
import { RoleGuard } from './core/guards/role-guard';

import { OfertasComponent } from './features/ofertas/ofertas';
import { CrearOferta } from './features/ofertas/crear-oferta/crear-oferta';

import { CrearPerfilEmpleadoComponent } from './features/empleado/crear-perfil-empleado/crear-perfil-empleado';
import { PerfilEmpleadoComponent } from './features/empleado/perfil-empleado/perfil-empleado';
import { EditarPerfilEmpleadoComponent } from './features/empleado/editar-perfil-empleado/editar-perfil-empleado';
import { PerfilEmpleadoPublicoComponent } from './features/empleado/perfil-empleado-publico/perfil-empleado-publico';

import { CrearPerfilEmpleadorComponent } from './features/empleador/crear-perfil-empleador/crear-perfil-empleador';
import { PerfilEmpleadorComponent } from './features/empleador/perfil-empleador/perfil-empleador';
import { PerfilEmpleadorPublicoComponent } from './features/empleador/perfil-empleador-publico/perfil-empleador-publico';
import { EditarPerfilEmpleadorComponent } from './features/empleador/editar-perfil-empleador/editar-perfil-empleador';
import { MisOfertasEmpleadorComponent } from './features/empleador/mis-ofertas-empleador/mis-ofertas-empleador';

import { PostularOfertaComponent } from './features/postulaciones/postular-oferta/postular-oferta';
import { PostulantesOfertaComponent } from './features/postulaciones/postulantes-oferta/postulantes-oferta';
import { MisPostulacionesComponent } from './features/postulaciones/mis-postulaciones/mis-postulaciones';

import { SuscripcionesComponent } from './features/suscripciones/suscripciones/suscripciones';
import { EditarCuentaComponent } from './features/cuenta/editar-cuenta/editar-cuenta';

import { TerminosCondicionesComponent } from './features/legal/terminos-condiciones/terminos-condiciones';
import { PoliticaPrivacidadComponent } from './features/legal/politica-privacidad/politica-privacidad';
import { PoliticaDevolucionesComponent } from './features/legal/politica-devoluciones/politica-devoluciones';
import { LibroReclamacionesComponent } from './features/legal/libro-reclamaciones/libro-reclamaciones';
import { ReportarProblemaComponent } from './features/soporte/reportar-problema/reportar-problema';

export const routes: Routes = [

  {
    path: 'login',
    component: Login,
    canActivate: [NoAuthGuard]
  },

  {
    path: 'register',
    component: Register,
    canActivate: [NoAuthGuard]
  },

  {
    path: 'recuperar-password',
    component: RecuperarPasswordComponent,
    canActivate: [NoAuthGuard]
  },

  {
    path: 'empleado/perfil',
    component: PerfilEmpleadoComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO']
    }
  },

  {
    path: 'empleado/perfil-publico/:idEmpleado',
    component: PerfilEmpleadoPublicoComponent,
    canActivate: [AuthGuard]
  },

  {
    path: 'empleado/crear-perfil',
    component: CrearPerfilEmpleadoComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO']
    }
  },

  {
    path: 'empleado/editar-perfil',
    component: EditarPerfilEmpleadoComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO']
    }
  },

  {
    path: 'empleador/perfil',
    component: PerfilEmpleadorComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },


  {
    path: 'empleador/perfil-publico/:idEmpleador',
    component: PerfilEmpleadorPublicoComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO']
    }
  },

  {
    path: 'empleador/crear-perfil',
    component: CrearPerfilEmpleadorComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },

  {
    path: 'empleador/editar-perfil',
    component: EditarPerfilEmpleadorComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },

  {
    path: 'empleador/mis-ofertas',
    component: MisOfertasEmpleadorComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },

  {
    path: 'empleador/ofertas/crear',
    component: CrearOferta,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },

  {
    path: 'empleador/ofertas/editar/:id',
    component: CrearOferta,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },

  {
    path: 'ofertas',
    component: OfertasComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO'],
      modo: 'general'
    }
  },

  {
    path: 'ofertas/para-ti',
    component: OfertasComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO'],
      modo: 'para-ti'
    }
  },

  {
    path: 'empleado/mis-postulaciones',
    component: MisPostulacionesComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO']
    }
  },

  {
    path: 'postulaciones/ofertas/:id/postular',
    component: PostularOfertaComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADO']
    }
  },

  {
    path: 'postulaciones/ofertas/:id/postulantes',
    component: PostulantesOfertaComponent,
    canActivate: [RoleGuard],
    data: {
      roles: ['ROLE_EMPLEADOR']
    }
  },


  {
    path: 'cuenta/editar',
    component: EditarCuentaComponent,
    canActivate: [AuthGuard]
  },

  {
    path: 'suscripciones',
    component: SuscripcionesComponent
  },

  {
    path: 'terminos-condiciones',
    component: TerminosCondicionesComponent
  },

  {
    path: 'politica-privacidad',
    component: PoliticaPrivacidadComponent
  },

  {
    path: 'politica-devoluciones',
    component: PoliticaDevolucionesComponent
  },

  {
    path: 'libro-reclamaciones',
    component: LibroReclamacionesComponent
  },

  {
    path: 'reportar-problema',
    component: ReportarProblemaComponent
  },

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];