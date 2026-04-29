import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./shared/layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      // Modulo Solicitudes
      {
        path: 'solicitudes',
        loadComponent: () => import('./features/solicitudes/solicitudes.component').then(m => m.SolicitudesComponent)
      },
      {
        path: 'solicitudes/apoyo-economico',
        loadComponent: () => import('./features/solicitudes/solicitud-dispersion/solicitud-dispersion.component').then(m => m.SolicitudDispersionComponent)
      },
      {
        path: 'solicitudes/reposicion',
        loadComponent: () => import('./features/solicitudes/solicitud-stock/solicitud-stock.component').then(m => m.SolicitudStockComponent)
      },
      {
        path: 'solicitudes/nueva-asignacion',
        loadComponent: () => import('./features/solicitudes/solicitud-asignacion/solicitud-asignacion.component').then(m => m.SolicitudAsignacionComponent)
      },
      {
        path: 'solicitudes/asignacion-adicional',
        loadComponent: () => import('./features/solicitudes/solicitud-adicional/solicitud-adicional.component').then(m => m.SolicitudAdicionalComponent)
      },
      {
        path: 'solicitudes/aprobacion',
        loadComponent: () => import('./features/solicitudes/autorizar-solicitud/autorizar-solicitud.component').then(m => m.AutorizarSolicitudComponent)
      },
      {
        path: 'solicitudes/resumen',
        loadComponent: () => import('./features/solicitudes/prefactura/prefactura.component').then(m => m.PrefacturaComponent)
      },
      {
        path: 'resumen',
        loadComponent: () => import('./features/solicitudes/prefactura/prefactura.component').then(m => m.PrefacturaComponent)
      },
      // Modulo Credenciales
      {
        path: 'credenciales',
        loadComponent: () => import('./features/credenciales/credenciales.component').then(m => m.CredencialesComponent)
      },
      { path: 'tarjetas', redirectTo: 'credenciales', pathMatch: 'full' },
      // Modulo Grupos
      {
        path: 'grupos',
        loadComponent: () => import('./features/grupos/grupos.component').then(m => m.GruposComponent)
      },
      {
        path: 'grupos/registro',
        loadComponent: () => import('./features/grupos/registro-grupo/registro-grupo.component').then(m => m.RegistroGrupoComponent)
      },
      {
        path: 'grupos/:id/asignacion',
        loadComponent: () => import('./features/grupos/asignacion-grupo/asignacion-grupo.component').then(m => m.AsignacionGrupoComponent)
      },
      {
        path: 'grupos/:id/reporte',
        loadComponent: () => import('./features/grupos/reporte-grupo/reporte-grupo.component').then(m => m.ReporteGrupoComponent)
      },
      // Modulo Empleados
      {
        path: 'empleados',
        loadComponent: () => import('./features/empleados/empleados.component').then(m => m.EmpleadosComponent)
      },
      {
        path: 'empleados/:numeroEmpleado/editar',
        loadComponent: () => import('./features/empleados/actualiza-empleado/actualiza-empleado.component').then(m => m.ActualizaEmpleadoComponent)
      },
      {
        path: 'cambio-contrasenia',
        loadComponent: () => import('./features/admin-login/cambiar-password/cambiar-password.component').then(m => m.CambiarPasswordComponent)
      },
      {
        path: 'Cambio_de_contrasenia',
        loadComponent: () => import('./features/admin-login/cambiar-password/cambiar-password.component').then(m => m.CambiarPasswordComponent)
      },
      {
        path: 'Administracion_Login/Cambio_Password',
        loadComponent: () => import('./features/admin-login/cambiar-password/cambiar-password.component').then(m => m.CambiarPasswordComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
