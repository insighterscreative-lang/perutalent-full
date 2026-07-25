import { Routes } from '@angular/router';

import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';

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
import { EditarPerfilEmpleadorComponent } from './features/empleador/editar-perfil-empleador/editar-perfil-empleador';
import { MisOfertasEmpleadorComponent } from './features/empleador/mis-ofertas-empleador/mis-ofertas-empleador';

import { PostularOfertaComponent } from './features/postulaciones/postular-oferta/postular-oferta';
import { PostulantesOfertaComponent } from './features/postulaciones/postulantes-oferta/postulantes-oferta';

import { SuscripcionesComponent } from './features/suscripciones/suscripciones/suscripciones';

import { TerminosCondicionesComponent } from './features/legal/terminos-condiciones/terminos-condiciones';
import { PoliticaPrivacidadComponent } from './features/legal/politica-privacidad/politica-privacidad';
import { PoliticaDevolucionesComponent } from './features/legal/politica-devoluciones/politica-devoluciones';
import { LibroReclamacionesComponent } from './features/legal/libro-reclamaciones/libro-reclamaciones';

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
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];