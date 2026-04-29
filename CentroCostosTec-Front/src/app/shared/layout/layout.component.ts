import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
  <div class="d-flex vh-100 overflow-hidden layout-shell">
    <div
      *ngIf="sidebarCollapsed && !sidebarHovered"
      class="sidebar-hover-trigger"
      (mouseenter)="sidebarHovered = true"
      aria-hidden="true"
    ></div>
    <!-- Sidebar -->
    <nav
      class="sidebar bg-dark text-white d-flex flex-column"
      [class.is-collapsed]="sidebarCollapsed && !sidebarHovered"
      (mouseenter)="onSidebarMouseEnter()"
      (mouseleave)="onSidebarMouseLeave()"
    >
      <div class="p-3 border-bottom border-secondary">
        <div class="d-flex align-items-center justify-content-between gap-2">
          <span class="fw-bold fs-6 text-nowrap sidebar-label">Plataforma Interna</span>
          <button
            class="btn btn-sm btn-outline-light sidebar-toggle"
            type="button"
            (click)="toggleSidebar()"
            [attr.aria-label]="sidebarCollapsed ? 'Mostrar menú lateral' : 'Ocultar menú lateral'"
          >
            <i class="fas" [ngClass]="sidebarCollapsed ? 'fa-angles-right' : 'fa-angles-left'"></i>
          </button>
        </div>
        <small class="sidebar-subtitle">{{ user?.nombreCompleto }}</small>
      </div>
      <ul class="nav flex-column flex-grow-1 p-2">
        <li class="nav-item">
          <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">
            <i class="fas fa-home me-2"></i><span>Inicio</span>
          </a>
        </li>
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <small class="section-label text-uppercase px-3">Solicitudes</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <a class="nav-link" routerLink="/solicitudes" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
            <i class="fas fa-list me-2"></i><span>Consulta de solicitudes</span>
          </a>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','AUTORIZADOR')">
          <a class="nav-link" routerLink="/solicitudes/aprobacion" routerLinkActive="active">
            <i class="fas fa-check-circle me-2"></i><span>Aprobar solicitudes</span>
          </a>
        </li>
        <li class="nav-item mt-1" *ngIf="canAny('ADMIN','CAPTURA')">
          <button
            class="submenu-toggle btn btn-link w-100 text-start"
            type="button"
            (click)="toggleSolicitudesSubmenu()"
            [attr.aria-expanded]="solicitudesSubmenuOpen"
            [attr.title]="(sidebarCollapsed && !sidebarHovered) ? 'Creación de solicitudes' : null"
          >
            <span class="section-label px-3 sidebar-submenu-title">Creación de solicitudes</span>
            <i class="fas fa-chevron-down submenu-caret" [class.is-open]="solicitudesSubmenuOpen"></i>
          </button>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA') && solicitudesSubmenuOpen && !(sidebarCollapsed && !sidebarHovered)">
          <a class="nav-link submenu-link" routerLink="/solicitudes/apoyo-economico" routerLinkActive="active">
            <i class="fas fa-file-invoice-dollar me-2"></i><span>Apoyo Económico</span>
          </a>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA') && solicitudesSubmenuOpen && !(sidebarCollapsed && !sidebarHovered)">
          <a class="nav-link submenu-link" routerLink="/solicitudes/reposicion" routerLinkActive="active">
            <i class="fas fa-boxes-stacked me-2"></i><span>Reposición de Saldo</span>
          </a>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA') && solicitudesSubmenuOpen && !(sidebarCollapsed && !sidebarHovered)">
          <a class="nav-link submenu-link" routerLink="/solicitudes/nueva-asignacion" routerLinkActive="active">
            <i class="fas fa-credit-card me-2"></i><span>Nueva Asignación</span>
          </a>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA') && solicitudesSubmenuOpen && !(sidebarCollapsed && !sidebarHovered)">
          <a class="nav-link submenu-link" routerLink="/solicitudes/asignacion-adicional" routerLinkActive="active">
            <i class="fas fa-address-card me-2"></i><span>Asignación Adicional</span>
          </a>
        </li>
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN','CAPTURA','CONSULTA')">
          <small class="section-label text-uppercase px-3">Credenciales</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA','CONSULTA')">
          <a class="nav-link" routerLink="/credenciales" routerLinkActive="active">
            <i class="fas fa-id-card me-2"></i><span>Lista de Credenciales</span>
          </a>
        </li>
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN','CAPTURA','CONSULTA')">
          <small class="section-label text-uppercase px-3">Equipos</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA','CONSULTA')">
          <a class="nav-link" routerLink="/grupos" routerLinkActive="active">
            <i class="fas fa-users me-2"></i><span>Equipos</span>
          </a>
        </li>
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN','CAPTURA')">
          <small class="section-label text-uppercase px-3">Colaboradores</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA')">
          <a class="nav-link" routerLink="/empleados" routerLinkActive="active">
            <i class="fas fa-user-edit me-2"></i><span>Actualizar Datos</span>
          </a>
        </li>
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <small class="section-label text-uppercase px-3">Perfil</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <a class="nav-link" routerLink="/Administracion_Login/Cambio_Password" routerLinkActive="active">
            <i class="fas fa-key me-2"></i><span>Cambiar Contraseña</span>
          </a>
        </li>
      </ul>
      <div class="p-3 border-top border-secondary">
        <button
          class="btn btn-sm btn-outline-light w-100 sidebar-logout-btn"
          [class.icon-only]="sidebarCollapsed && !sidebarHovered"
          (click)="logout()"
          [attr.title]="(sidebarCollapsed && !sidebarHovered) ? 'Cerrar sesión' : null"
          [attr.aria-label]="'Cerrar sesión'"
        >
          <i class="fas fa-sign-out-alt" [ngClass]="(sidebarCollapsed && !sidebarHovered) ? 'me-0' : 'me-1'"></i>
          <span class="logout-label">Cerrar sesión</span>
        </button>
      </div>
    </nav>
    <!-- Contenido -->
    <main class="flex-grow-1 overflow-auto bg-light">
      <header class="bg-primary text-white shadow-sm px-4 py-3 d-flex justify-content-between align-items-center">
        <div>
          <div class="fw-semibold">{{ centroNombre }}</div>
          <small class="opacity-75">{{ fechaActual }}</small>
        </div>
        <div class="d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-light d-lg-none" type="button" (click)="toggleSidebar()" aria-label="Alternar menú lateral">
            <i class="fas fa-bars"></i>
          </button>
          <small class="d-none d-md-inline">{{ user?.nombreCompleto || user?.username }}</small>
          <button class="btn btn-sm btn-light" (click)="logout()">
            <i class="fas fa-sign-out-alt me-1"></i>Cerrar sesión
          </button>
        </div>
      </header>
      <router-outlet />
    </main>
  </div>
  `,
  styles: [`
    .layout-shell {
      position: relative;
    }

    .sidebar {
      width: 240px;
      min-width: 240px;
      transition: width .22s ease, min-width .22s ease;
      position: relative;
      z-index: 5;
    }

    .sidebar.is-collapsed {
      width: 72px;
      min-width: 72px;
    }

    .sidebar.is-collapsed .sidebar-label,
    .sidebar.is-collapsed .sidebar-subtitle,
    .sidebar.is-collapsed .section-label,
    .sidebar.is-collapsed .nav-link span {
      display: none;
    }

    .sidebar.is-collapsed .nav-link {
      justify-content: center;
      padding-left: .75rem;
      padding-right: .75rem;
    }

    .sidebar.is-collapsed .nav-link i {
      margin-right: 0 !important;
      font-size: 1.05rem;
    }

    .sidebar-hover-trigger {
      position: fixed;
      left: 0;
      top: 0;
      bottom: 0;
      width: 14px;
      z-index: 15;
    }

    .sidebar-subtitle {
      color: #f0f2f5;
      display: inline-block;
      margin-top: .25rem;
      line-height: 1.2;
    }

    .section-label {
      color: #d4d8de;
      font-weight: 600;
      letter-spacing: .04em;
    }

    .sidebar-submenu-title {
      display: inline-block;
      font-size: .78rem;
      opacity: .92;
    }

    .submenu-toggle {
      color: #d4d8de !important;
      text-decoration: none;
      padding: .2rem 0;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border: 0;
    }

    .submenu-toggle:hover,
    .submenu-toggle:focus {
      color: #ffffff !important;
      background: rgba(255, 255, 255, .08);
      border-radius: .35rem;
    }

    .submenu-caret {
      margin-right: .75rem;
      font-size: .75rem;
      opacity: .9;
      transition: transform .18s ease;
    }

    .submenu-caret.is-open {
      transform: rotate(180deg);
    }

    .sidebar.is-collapsed .submenu-caret,
    .sidebar.is-collapsed .sidebar-submenu-title {
      display: none;
    }

    .sidebar .nav-link {
      color: #f8f9fa;
      font-weight: 500;
      border-radius: .45rem;
      display: flex;
      align-items: center;
      gap: .15rem;
    }

    .sidebar .nav-link.submenu-link {
      padding-left: 1.65rem;
    }

    .sidebar .nav-link:hover {
      color: #ffffff;
      background: rgba(255, 255, 255, .14);
    }

    .sidebar .nav-link.active {
      color: #ffffff;
      background: rgba(13, 110, 253, .9);
      box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .1);
    }

    .sidebar-toggle {
      min-width: 30px;
      padding: .1rem .35rem;
      opacity: .95;
    }

    .sidebar-logout-btn {
      color: #ffffff;
      border-color: rgba(255, 255, 255, .68);
      background: rgba(255, 255, 255, .05);
      font-weight: 600;
    }

    .sidebar-logout-btn:hover,
    .sidebar-logout-btn:focus {
      color: #ffffff;
      border-color: rgba(255, 255, 255, .85);
      background: rgba(13, 110, 253, .9);
      box-shadow: 0 0 0 .2rem rgba(13, 110, 253, .25);
    }

    .sidebar.is-collapsed .sidebar-logout-btn {
      padding-left: .5rem;
      padding-right: .5rem;
    }

    .sidebar.is-collapsed .sidebar-logout-btn.icon-only {
      width: 40px !important;
      min-width: 40px;
      height: 34px;
      margin: 0 auto;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }

    .sidebar.is-collapsed .sidebar-logout-btn .logout-label {
      display: none;
    }

    @media (max-width: 991.98px) {
      .sidebar {
        position: fixed;
        left: 0;
        top: 0;
        bottom: 0;
        transform: translateX(0);
        box-shadow: 0 0 0 rgba(0, 0, 0, 0);
      }

      .sidebar.is-collapsed {
        transform: translateX(-100%);
      }
    }
  `]
})
export class LayoutComponent {
  user = this.authService.getUser() as any;
  sidebarCollapsed = false;
  sidebarHovered = false;
  solicitudesSubmenuOpen = true;
  fechaActual = new Date().toLocaleDateString('es-MX', {
    weekday: 'long', year: 'numeric', month: 'long', day: '2-digit'
  });

  get centroNombre(): string {
    return this.user?.centroNombre || this.user?.rs_centronombre || this.user?.centroId || this.user?.rs_centroid || 'Operaciones Internas';
  }

  canAny(...roles: string[]): boolean {
    const rol = String(this.authService.getRol() || this.user?.rol || '').trim().toUpperCase();
    return roles.includes(rol);
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
    if (!this.sidebarCollapsed) {
      this.sidebarHovered = false;
    }
  }

  onSidebarMouseEnter(): void {
    if (this.sidebarCollapsed) {
      this.sidebarHovered = true;
    }
  }

  onSidebarMouseLeave(): void {
    if (this.sidebarCollapsed) {
      this.sidebarHovered = false;
    }
  }

  toggleSolicitudesSubmenu(): void {
    this.solicitudesSubmenuOpen = !this.solicitudesSubmenuOpen;
  }

  constructor(private authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
