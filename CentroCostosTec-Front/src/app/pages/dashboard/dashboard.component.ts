import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
  <div class="p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
      <div>
        <h4 class="mb-1 text-primary">Plataforma de Operación Interna</h4>
        <p class="text-muted mb-0">Bienvenido al sistema de gestión empresarial. Selecciona un módulo para comenzar.</p>
      </div>
      <div class="text-md-end">
        <div class="text-muted small"><i class="fas fa-calendar-alt me-1"></i>{{ fechaActual }}</div>
        <button class="btn btn-sm btn-outline-danger mt-2" (click)="cerrarSesion()">
          <i class="fas fa-sign-out-alt me-1"></i>Cerrar sesión
        </button>
      </div>
    </div>

    <div class="card shadow-sm border-0 mb-4">
      <div class="card-header bg-white fw-semibold">
        <i class="fas fa-building me-2 text-primary"></i>Datos de la Unidad Operativa
      </div>
      <div class="card-body py-3">
        <div class="row g-3">
          <div class="col-6 col-lg-3">
            <small class="text-muted d-block">Corporativo</small>
            <strong>{{ datosCentro.corporativoId || '-' }}</strong>
          </div>
          <div class="col-6 col-lg-3">
            <small class="text-muted d-block">Unidad Operativa</small>
            <strong>{{ datosCentro.centroId || '-' }}</strong>
          </div>
          <div class="col-12 col-lg-6">
            <small class="text-muted d-block">Nombre Unidad</small>
            <strong>{{ datosCentro.centroNombre || '-' }}</strong>
          </div>
          <div class="col-6 col-lg-3">
            <small class="text-muted d-block">Cliente ID</small>
            <strong>{{ datosCentro.clienteId || '-' }}</strong>
          </div>
          <div class="col-6 col-lg-3">
            <small class="text-muted d-block">Consignatario ID</small>
            <strong>{{ datosCentro.consignatarioId || '-' }}</strong>
          </div>
          <div class="col-6 col-lg-2">
            <small class="text-muted d-block">Rol</small>
            <strong>{{ datosCentro.rol || '-' }}</strong>
          </div>
          <div class="col-6 col-lg-2">
            <small class="text-muted d-block">Usuario</small>
            <strong>{{ datosCentro.username || '-' }}</strong>
          </div>
          <div class="col-12 col-lg-2">
            <small class="text-muted d-block">Nombre</small>
            <strong>{{ datosCentro.nombreCompleto || '-' }}</strong>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm border-0">
      <div class="card-header bg-white fw-semibold">
        <i class="fas fa-th-large me-2 text-primary"></i>Accesos rápidos
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-sm-6 col-xl-4" *ngFor="let card of cardsVisibles">
            <div class="card h-100 shadow-sm border-0 bg-light-subtle">
              <div class="card-body text-center p-4">
                <i class="fas fa-2x mb-3" [class]="card.icon + ' text-' + card.color"></i>
                <h6 class="card-title mb-2">{{ card.titulo }}</h6>
                <p class="card-text text-muted small mb-3">{{ card.desc }}</p>
                <a [routerLink]="card.ruta" class="btn btn-sm" [class]="'btn-outline-' + card.color">Ir al módulo</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  `
})
export class DashboardComponent {
  fechaActual = new Date().toLocaleDateString('es-MX', {
    weekday: 'long', year: 'numeric', month: 'long', day: '2-digit'
  });

  datosCentro: {
    corporativoId: string;
    centroId: string;
    centroNombre: string;
    clienteId: string;
    consignatarioId: string;
    username: string;
    nombreCompleto: string;
    rol: string;
  } = {
    corporativoId: '',
    centroId: '',
    centroNombre: '',
    clienteId: '',
    consignatarioId: '',
    username: '',
    nombreCompleto: '',
    rol: ''
  };

  cards = [
    { titulo: 'Solicitudes', desc: 'Gestiona solicitudes operativas internas.', icon: 'fa-file-invoice-dollar', color: 'primary', ruta: '/solicitudes', roles: ['ADMIN', 'CAPTURA', 'AUTORIZADOR', 'CONSULTA'] },
    { titulo: 'Solicitudes Adicionales', desc: 'Registra requerimientos adicionales del equipo.', icon: 'fa-address-card', color: 'primary', ruta: '/solicitudes/asignacion-adicional', roles: ['ADMIN', 'CAPTURA'] },
    { titulo: 'Credenciales', desc: 'Consulta, activa y da de baja credenciales.', icon: 'fa-id-card', color: 'info', ruta: '/credenciales', roles: ['ADMIN', 'CAPTURA', 'CONSULTA'] },
    { titulo: 'Equipos', desc: 'Administra equipos de colaboradores.', icon: 'fa-users', color: 'success', ruta: '/grupos', roles: ['ADMIN', 'CAPTURA', 'CONSULTA'] },
    { titulo: 'Colaboradores', desc: 'Actualiza datos de colaboradores.', icon: 'fa-user-edit', color: 'warning', ruta: '/empleados', roles: ['ADMIN', 'CAPTURA'] },
    { titulo: 'Cambiar Contraseña', desc: 'Actualiza la contraseña del usuario actual.', icon: 'fa-key', color: 'secondary', ruta: '/Administracion_Login/Cambio_Password', roles: ['ADMIN', 'CAPTURA', 'AUTORIZADOR', 'CONSULTA'] }
  ];

  constructor(private authService: AuthService, private router: Router) {
    this.cargarDatosCentro();
  }

  get cardsVisibles() {
    const rol = String(this.authService.getRol() || '').trim().toUpperCase();
    return this.cards.filter(c => c.roles.includes(rol));
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private cargarDatosCentro(): void {
    const user = (this.authService.getUser() || {}) as any;
    this.datosCentro = {
      corporativoId: String(user.corporativoId ?? user.corporativo ?? user.rs_corporativoid ?? ''),
      centroId: String(user.centroId ?? user.centrocostos ?? user.rs_centroid ?? ''),
      centroNombre: String(user.centroNombre ?? user.rs_centronombre ?? ''),
      clienteId: String(user.clienteId ?? user.clienteid ?? ''),
      consignatarioId: String(user.consignatarioId ?? user.consignatarioid ?? ''),
      username: String(user.username ?? user.usuarioUSR ?? ''),
      nombreCompleto: String(user.nombreCompleto ?? user.rs_usuarionombre ?? ''),
      rol: String(user.rol ?? '')
    };
  }
}

