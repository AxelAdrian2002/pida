import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmpresaAdminService } from '../../services/empresa-admin.service';

@Component({
  selector: 'app-perfil-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="p-4 perfil-page">
    <div class="perfil-container">
      <section class="perfil-aside card shadow-sm">
        <div class="card-body d-flex flex-column align-items-center text-center gap-3">
          <div class="avatar-wrap">
            <img *ngIf="perfil.fotoUrl" [src]="perfil.fotoUrl" alt="Foto de perfil" class="avatar-big">
            <div *ngIf="!perfil.fotoUrl" class="avatar-big avatar-fallback">{{ inicialesPerfil }}</div>
          </div>
          <div>
            <h5 class="mb-1">{{ perfil.nombre || 'Usuario' }}</h5>
            <div class="text-muted small">{{ perfil.email || 'Sin correo' }}</div>
          </div>

          <div class="w-100">
            <label class="form-label fw-semibold text-start d-block">Foto de perfil</label>
            <div class="d-flex flex-column gap-2">
              <input type="file" class="form-control" accept="image/*" (change)="onFotoSelected($event)">
              <button type="button" class="btn btn-outline-primary w-100" (click)="subirFoto()" [disabled]="!fotoFile || subiendoFoto">
                {{ subiendoFoto ? 'Subiendo foto...' : 'Subir nueva foto' }}
              </button>
            </div>
            <small class="text-muted d-block mt-2">Si no hay imagen, se muestran las siglas de tu nombre.</small>
          </div>
        </div>
      </section>

      <section class="perfil-main card shadow-sm">
        <div class="card-header bg-white border-0 pb-0">
          <strong>Configuracion de mi perfil</strong>
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label fw-semibold">Nombre</label>
              <input class="form-control" [(ngModel)]="perfil.nombre" name="nombre" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">Email</label>
              <input class="form-control" [(ngModel)]="perfil.email" name="email" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">CURP</label>
              <input class="form-control" [(ngModel)]="perfil.curp" name="curp" maxlength="18" placeholder="AAAA000000HXXXXX00">
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">RFC</label>
              <input class="form-control" [(ngModel)]="perfil.rfc" name="rfc" maxlength="13" placeholder="AAAA000000XXX">
            </div>
            <div class="col-12">
              <label class="form-label fw-semibold">Foto URL</label>
              <input class="form-control" [(ngModel)]="perfil.fotoUrl" name="fotoUrl" placeholder="https://...">
            </div>
          </div>
        </div>
        <div class="card-footer bg-white d-flex justify-content-end">
          <button class="btn btn-primary" type="button" (click)="guardarPerfil()" [disabled]="guardando">
            {{ guardando ? 'Guardando...' : 'Guardar cambios' }}
          </button>
        </div>
      </section>
    </div>

    <div class="alert alert-success mt-3" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>
  </div>
  `,
  styles: [`
    .perfil-page {
      background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
      min-height: calc(100vh - 64px);
    }

    .perfil-container {
      display: grid;
      grid-template-columns: 320px 1fr;
      gap: 1rem;
      align-items: start;
      max-width: 1160px;
    }

    .perfil-aside,
    .perfil-main {
      border: 1px solid #e9eef5;
      border-radius: 0.85rem;
    }

    .avatar-wrap {
      width: 170px;
      height: 170px;
    }

    .avatar-big {
      width: 170px;
      height: 170px;
      border-radius: 50%;
      object-fit: cover;
      border: 4px solid #dbeafe;
      box-shadow: 0 8px 24px rgba(15, 23, 42, .12);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .avatar-fallback {
      background: linear-gradient(135deg, #0f766e 0%, #2563eb 100%);
      color: #ffffff;
      font-size: 2.4rem;
      font-weight: 800;
      letter-spacing: .04em;
    }

    @media (max-width: 991.98px) {
      .perfil-container {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class PerfilConfigComponent implements OnInit {
  perfil: { nombre?: string; email?: string; curp?: string; rfc?: string; fotoUrl?: string } = {};
  fotoFile: File | null = null;
  subiendoFoto = false;
  guardando = false;
  exitoMsg = '';
  errorMsg = '';

  get inicialesPerfil(): string {
    const nombre = String(this.perfil?.nombre || '').trim();
    if (!nombre) {
      return 'US';
    }

    const partes = nombre.split(/\s+/).filter(Boolean);
    if (partes.length === 1) {
      return partes[0].slice(0, 2).toUpperCase();
    }
    return (partes[0][0] + partes[1][0]).toUpperCase();
  }

  constructor(private empresaAdminService: EmpresaAdminService) {}

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.empresaAdminService.obtenerPerfil().subscribe({
      next: res => {
        this.perfil = { ...(res?.datos || {}) };
        this.empresaAdminService.actualizarFotoPerfil(this.perfil?.fotoUrl);
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible obtener tu perfil';
      }
    });
  }

  onFotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fotoFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  subirFoto(): void {
    if (!this.fotoFile) {
      return;
    }

    this.subiendoFoto = true;
    this.errorMsg = '';

    this.empresaAdminService.subirFotoPerfil(this.fotoFile).subscribe({
      next: res => {
        const url = String(res?.datos?.url || '');
        if (url) {
          this.perfil.fotoUrl = url;
          this.empresaAdminService.actualizarFotoPerfil(url);
          this.exitoMsg = 'Foto actualizada correctamente.';
        }
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible subir la foto';
      },
      complete: () => {
        this.subiendoFoto = false;
      }
    });
  }

  guardarPerfil(): void {
    this.guardando = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.guardarPerfil({
      perfil: {
        curp: this.perfil.curp,
        rfc: this.perfil.rfc,
        fotoUrl: this.perfil.fotoUrl
      }
    }).subscribe({
      next: res => {
        this.perfil = { ...(res?.datos || this.perfil) };
        this.empresaAdminService.actualizarFotoPerfil(this.perfil?.fotoUrl);
        this.exitoMsg = res?.mensaje || 'Perfil guardado correctamente';
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible guardar tu perfil';
      },
      complete: () => {
        this.guardando = false;
      }
    });
  }
}
