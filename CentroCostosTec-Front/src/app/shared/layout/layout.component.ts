import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { EmpresaAdminService } from '../../services/empresa-admin.service';

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
          <div class="d-flex align-items-center gap-2 overflow-hidden">
            <img *ngIf="logoUrl" [src]="logoUrl" alt="Logo empresa" class="brand-logo sidebar-label">
            <span class="fw-bold fs-6 text-nowrap sidebar-label">{{ nombreEmpresa }}</span>
          </div>
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
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN')">
          <small class="section-label text-uppercase px-3">Empresa</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN')">
          <a class="nav-link" routerLink="/empresa/configuracion" routerLinkActive="active">
            <i class="fas fa-building me-2"></i><span>Configuracion Empresa</span>
          </a>
        </li>
        <li class="nav-item mt-2" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <small class="section-label text-uppercase px-3">Perfil</small>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <a class="nav-link" routerLink="/perfil/configuracion" routerLinkActive="active">
            <i class="fas fa-user-cog me-2"></i><span>Configuracion de datos</span>
          </a>
        </li>
        <li class="nav-item" *ngIf="canAny('ADMIN','CAPTURA','AUTORIZADOR','CONSULTA')">
          <a class="nav-link" routerLink="/Administracion_Login/Cambio_Password" routerLinkActive="active">
            <i class="fas fa-key me-2"></i><span>Cambiar Contrasenia</span>
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
          <small class="d-block opacity-75">Empresa: {{ corporativoActual }}</small>
          <small class="opacity-75">{{ fechaActual }}</small>
        </div>
        <div class="d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-light d-lg-none" type="button" (click)="toggleSidebar()" aria-label="Alternar menú lateral">
            <i class="fas fa-bars"></i>
          </button>
          <div class="user-menu-wrapper">
            <button class="user-menu-btn" type="button" (click)="toggleUserMenu($event)" aria-label="Menú de usuario">
              <img *ngIf="fotoPerfilUrl" [src]="fotoPerfilUrl" alt="Foto de perfil" class="user-avatar">
              <span *ngIf="!fotoPerfilUrl" class="user-avatar user-avatar-fallback">{{ inicialesUsuario }}</span>
              <span class="user-name d-none d-md-inline">{{ user?.nombreCompleto || user?.username }}</span>
              <i class="fas fa-chevron-down small"></i>
            </button>
            <div class="user-menu-dropdown" *ngIf="userMenuOpen">
              <button type="button" class="user-menu-item" (click)="irConfiguracionPerfil()">
                <i class="fas fa-user-cog me-2"></i>Configuracion de datos
              </button>
              <button type="button" class="user-menu-item" (click)="irCambioContrasenia()">
                <i class="fas fa-key me-2"></i>Cambiar contrasenia
              </button>
              <button type="button" class="user-menu-item text-danger" (click)="logout()">
                <i class="fas fa-sign-out-alt me-2"></i>Cerrar sesión
              </button>
            </div>
          </div>
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
      background: var(--brand-secondary) !important;
      height: 100vh;
      overflow: hidden;
    }

    .sidebar .nav {
      min-height: 0;
      overflow-y: auto;
      overflow-x: hidden;
      scrollbar-width: thin;
    }

    .brand-logo {
      width: 34px;
      height: 34px;
      object-fit: contain;
      border-radius: 0;
      background: transparent;
      padding: 0;
      flex: 0 0 auto;
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
      background: var(--brand-primary);
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
      background: var(--brand-primary);
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

    .user-menu-wrapper {
      position: relative;
    }

    .user-menu-btn {
      border: 0;
      background: #ffffff;
      color: #0f172a;
      border-radius: 999px;
      padding: .2rem .7rem .2rem .2rem;
      display: inline-flex;
      align-items: center;
      gap: .5rem;
      font-weight: 600;
    }

    .user-avatar {
      width: 34px;
      height: 34px;
      border-radius: 50%;
      object-fit: cover;
      background: #e2e8f0;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: .8rem;
      font-weight: 700;
    }

    .user-avatar-fallback {
      color: #0f172a;
    }

    .user-name {
      max-width: 220px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .user-menu-dropdown {
      position: absolute;
      right: 0;
      top: calc(100% + 8px);
      min-width: 220px;
      background: #ffffff;
      border-radius: .75rem;
      box-shadow: 0 10px 30px rgba(2, 6, 23, .16);
      border: 1px solid #e2e8f0;
      padding: .35rem;
      z-index: 25;
    }

    .user-menu-item {
      width: 100%;
      border: 0;
      background: transparent;
      text-align: left;
      padding: .55rem .65rem;
      border-radius: .5rem;
      color: #0f172a;
      font-weight: 500;
    }

    .user-menu-item:hover {
      background: #f1f5f9;
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
export class LayoutComponent implements OnInit {
  user = this.authService.getUser() as any;
  sidebarCollapsed = false;
  sidebarHovered = false;
  solicitudesSubmenuOpen = true;
  nombreEmpresa = 'Plataforma Interna';
  logoUrl = '';
  fotoPerfilUrl = '';
  userMenuOpen = false;
  fechaActual = new Date().toLocaleDateString('es-MX', {
    weekday: 'long', year: 'numeric', month: 'long', day: '2-digit'
  });

  get inicialesUsuario(): string {
    const nombre = String(this.user?.nombreCompleto || this.user?.username || 'US').trim();
    const partes = nombre.split(/\s+/).filter(Boolean);
    if (partes.length === 0) {
      return 'US';
    }
    if (partes.length === 1) {
      return partes[0].slice(0, 2).toUpperCase();
    }
    return (partes[0][0] + partes[1][0]).toUpperCase();
  }

  get centroNombre(): string {
    return this.user?.centroNombre || this.user?.rs_centronombre || this.user?.centroId || this.user?.rs_centroid || 'Operaciones Internas';
  }

  get corporativoActual(): string {
    return this.user?.corporativoId || this.user?.corporativoid || 'N/A';
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

  constructor(
    private authService: AuthService,
    private empresaAdminService: EmpresaAdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.empresaAdminService.branding$.subscribe(branding => {
      this.nombreEmpresa = branding?.nombreEmpresa || this.nombreEmpresa;
      this.logoUrl = branding?.logoUrl || this.logoUrl;
    });

    this.empresaAdminService.perfil$.subscribe(perfil => {
      this.fotoPerfilUrl = perfil?.fotoUrl || this.fotoPerfilUrl;
    });

    const branding = this.empresaAdminService.aplicarBrandingGuardado();
    if (branding) {
      this.nombreEmpresa = branding.nombreEmpresa;
      this.logoUrl = branding.logoUrl;
    }

    if (!this.authService.isLoggedIn()) {
      return;
    }

    if (this.canAny('ADMIN')) {
      this.empresaAdminService.obtenerConfiguracion().subscribe({
        next: res => {
          const configuracion = res?.datos;
          this.nombreEmpresa = configuracion?.empresa?.nombreEmpresa || this.nombreEmpresa;
          this.logoUrl = configuracion?.empresa?.logoUrl || '';
          this.fotoPerfilUrl = configuracion?.perfil?.fotoUrl || this.fotoPerfilUrl;
          this.empresaAdminService.aplicarBranding(configuracion);
          this.empresaAdminService.actualizarFotoPerfil(this.fotoPerfilUrl);
        }
      });
    }

    this.empresaAdminService.obtenerPerfil().subscribe({
      next: res => {
        this.fotoPerfilUrl = res?.datos?.fotoUrl || this.fotoPerfilUrl;
        this.empresaAdminService.actualizarFotoPerfil(this.fotoPerfilUrl);
      }
    });
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.userMenuOpen = false;
  }

  toggleUserMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.userMenuOpen = !this.userMenuOpen;
  }

  irConfiguracionPerfil(): void {
    this.userMenuOpen = false;
    this.router.navigate(['/perfil/configuracion']);
  }

  irCambioContrasenia(): void {
    this.userMenuOpen = false;
    this.router.navigate(['/Administracion_Login/Cambio_Password']);
  }

  logout(): void {
    this.userMenuOpen = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
