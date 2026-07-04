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
    <div class="card shadow-sm perfil-card">
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
          <div class="col-md-4">
            <label class="form-label fw-semibold">CURP</label>
            <input class="form-control" [(ngModel)]="perfil.curp" name="curp" maxlength="18" placeholder="AAAA000000HXXXXX00">
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">RFC</label>
            <input class="form-control" [(ngModel)]="perfil.rfc" name="rfc" maxlength="13" placeholder="AAAA000000XXX">
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">Foto URL</label>
            <input class="form-control mb-2" [(ngModel)]="perfil.fotoUrl" name="fotoUrl" placeholder="https://...">
            <div class="d-flex gap-2 align-items-center">
              <input type="file" class="form-control" accept="image/*" (change)="onFotoSelected($event)">
              <button type="button" class="btn btn-outline-primary" (click)="subirFoto()" [disabled]="!fotoFile || subiendoFoto">
                {{ subiendoFoto ? 'Subiendo...' : 'Subir foto' }}
              </button>
            </div>
          </div>
          <div class="col-12" *ngIf="perfil.fotoUrl">
            <img [src]="perfil.fotoUrl" alt="Foto de perfil" class="foto-preview">
          </div>
        </div>
      </div>
      <div class="card-footer bg-white d-flex justify-content-end">
        <button class="btn btn-primary" type="button" (click)="guardarPerfil()" [disabled]="guardando">
          {{ guardando ? 'Guardando...' : 'Guardar cambios' }}
        </button>
      </div>
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

    .perfil-card {
      border: 1px solid #e9eef5;
      border-radius: 0.75rem;
      max-width: 980px;
    }

    .foto-preview {
      width: 96px;
      height: 96px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid #e2e8f0;
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

  constructor(private empresaAdminService: EmpresaAdminService) {}

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.empresaAdminService.obtenerPerfil().subscribe({
      next: res => {
        this.perfil = { ...(res?.datos || {}) };
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
