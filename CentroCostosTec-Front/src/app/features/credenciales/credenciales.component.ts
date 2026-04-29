import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { CredencialService } from '../../services/credencial.service';
import { AuthService } from '../../services/auth.service';
import { Credencial } from '../../models/models';

@Component({
  selector: 'app-credenciales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="-page p-4">
    <div class="-header mb-3">
      <div>
        <h5 class="mb-1 text-danger"><i class="fas fa-id-card me-2"></i>Gestión de Credenciales Internas</h5>
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-primary btn-sm" type="button" [disabled]="true" title="Pendiente de habilitar en este front">
          Ir a cancelación masiva
        </button>
        <span class="badge text-bg-primary align-self-center"></span>
      </div>
    </div>

    <div class="card shadow-sm mb-3 -card">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-md-2">
            <label class="form-label fw-semibold">Estatus</label>
            <select class="form-select" [(ngModel)]="filtroEstado" (change)="cargar()">
              <option value="">Todos</option>
              <option>ACTIVA</option><option>INACTIVA</option><option>CANCELADA</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">N° Empleado</label>
            <input class="form-control" [(ngModel)]="filtroNumEmp" placeholder="EMP-001">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Buscar credencial/cuenta</label>
            <input class="form-control" [(ngModel)]="filtroTexto" placeholder="Credencial o cuenta" (input)="aplicarFiltroLocal()">
          </div>
          <div class="col-auto">
            <button class="btn btn-primary" (click)="page=0;cargar()">
              <i class="fas fa-search me-1"></i>Buscar
            </button>
          </div>
          <div class="col-auto">
            <button class="btn btn-outline-success" (click)="descargarExcel()">
              <i class="fas fa-file-excel me-1"></i>Excel 
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm -card">
      <div class="card-header bg-white border-0 pb-0">
        <strong>Resultados</strong>
      </div>
      <div class="table-responsive">
        <table class="table table-striped table-sm mb-0 align-middle">
          <thead class="table-light">
            <tr>
              <th>Cuenta</th>
              <th>Credencial</th>
              <th>Tipo de Credencial</th>
              <th>N° Empleado</th>
              <th>Empleado Nombre</th>
              <th>Vigencia</th>
              <th>Saldo</th>
              <th>Estatus</th>
              <th></th>
              <th></th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let t of tarjetasFiltradas">
              <td>{{ t.cuentaId || '-' }}</td>
              <td>{{ t.numeroCredencial || '-' }}</td>
              <td>{{ t.tipoCredencial || '-' }}</td>
              <td>{{ t.numeroEmpleado || '-' }}</td>
              <td>{{ t.nombreEmpleado || '-' }}</td>
              <td>{{ formatVigencia(t) }}</td>
              <td>{{ formatSaldo(t) }}</td>
              <td><span class="badge" [ngClass]="getBadge(t.estado)">{{ getEstadoLabel(t) }}</span></td>
              <td>
                <button class="btn btn-sm" [ngClass]="isTarjetaActiva(t) ? 'btn-outline-secondary' : 'btn-outline-primary'" (click)="toggleActivacion(t)"
                        [disabled]="t.estado === 'CANCELADA'"
                        [title]="isTarjetaActiva(t) ? 'Inactivar credencial' : 'Activar credencial'">
                  <i class="fas" [ngClass]="isTarjetaActiva(t) ? 'fa-lock' : 'fa-lock-open'"></i>
                </button>
              </td>
              <td>
                <button class="btn btn-sm btn-danger" (click)="cancelar(t)"
                        [disabled]="t.estado === 'CANCELADA'" title="Dar de baja credencial definitivamente">
                  <i class="fas fa-ban"></i>
                </button>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-secondary" type="button" [disabled]="true"
                        title="Reposición se habilitará con flujo completo de stock">
                  Reponer
                </button>
              </td>
            </tr>
            <tr *ngIf="tarjetasFiltradas.length === 0">
              <td colspan="11" class="text-center text-muted py-3">Sin resultados</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-between">
        <small>Página {{ page + 1 }} de {{ totalPages }}</small>
        <div class="btn-group">
          <button class="btn btn-sm btn-outline-secondary" [disabled]="page===0" (click)="page=page-1;cargar()">Anterior</button>
          <button class="btn btn-sm btn-outline-secondary" [disabled]="page>=totalPages-1" (click)="page=page+1;cargar()">Siguiente</button>
        </div>
      </div>
    </div>

    <div class="alert alert-success mt-3" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>
  </div>
  `,
  styles: [`
    .-page {
      background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
      min-height: calc(100vh - 64px);
    }

    .-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 2px solid #e9eef5;
      padding-bottom: 0.75rem;
    }

    .-card {
      border: 1px solid #e9eef5;
      border-radius: 0.75rem;
    }
  `]
})
export class CredencialesComponent implements OnInit {
  tarjetas: Credencial[] = [];
  tarjetasFiltradas: Credencial[] = [];
  filtroEstado = '';
  filtroNumEmp = '';
  filtroTexto = '';
  page = 0;
  totalPages = 1;
  exitoMsg = '';
  errorMsg = '';

  constructor(private credencialService: CredencialService, private authService: AuthService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.credencialService.consultar(this.filtroEstado || undefined, undefined, this.filtroNumEmp || undefined, this.page).subscribe({
      next: res => {
        const data = res.datos as any;
        this.tarjetas = data.content || [];
        this.tarjetasFiltradas = [...this.tarjetas];
        this.totalPages = data.totalPages || 1;
        this.aplicarFiltroLocal();
      }
    });
  }

  aplicarFiltroLocal(): void {
    const criterio = (this.filtroTexto || '').trim().toLowerCase();
    if (!criterio) {
      this.tarjetasFiltradas = [...this.tarjetas];
      return;
    }
    this.tarjetasFiltradas = this.tarjetas.filter(t => {
      const credencial = (t.numeroCredencial || '').toLowerCase();
      const cuenta = (t.cuentaId || '').toLowerCase();
      const empleado = (t.nombreEmpleado || '').toLowerCase();
      return credencial.includes(criterio) || cuenta.includes(criterio) || empleado.includes(criterio);
    });
  }

  toggleActivacion(t: Credencial): void {
    const user = this.authService.getUser();
    const request = { numeroCredencial: t.numeroCredencial, idUsuario: user?.idUsuario ?? 0, usuarioOperacion: user?.username ?? '' };
    const accion$ = this.isTarjetaActiva(t)
      ? this.credencialService.inactivar(request)
      : this.credencialService.activar(request);

    accion$.subscribe({
      next: () => {
        this.exitoMsg = `Credencial ${t.numeroCredencial} ${this.isTarjetaActiva(t) ? 'inactivada' : 'activada'}`;
        this.errorMsg = '';
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.exitoMsg = '';
        this.errorMsg = (err.error as any)?.mensaje || (this.isTarjetaActiva(t) ? 'Error al inactivar' : 'Error al activar');
      }
    });
  }

  isTarjetaActiva(t: Credencial): boolean {
    return (t.estado || '').toUpperCase() === 'ACTIVA';
  }

  cancelar(t: Credencial): void {
    const motivo = prompt('Motivo de baja:');
    if (!motivo) return;
    const user = this.authService.getUser();
    this.credencialService.cancelar({ numeroCredencial: t.numeroCredencial, idUsuario: user?.idUsuario ?? 0, usuarioOperacion: user?.username ?? '', observacion: motivo }).subscribe({
      next: () => { this.exitoMsg = `Credencial ${t.numeroCredencial} dada de baja`; this.cargar(); },
      error: (err: HttpErrorResponse) => this.errorMsg = (err.error as any)?.mensaje || 'Error al cancelar'
    });
  }

  getBadge(estado: string): object {
    return { 'bg-success': estado === 'ACTIVA', 'bg-warning text-dark': estado === 'INACTIVA', 'bg-danger': estado === 'CANCELADA' };
  }

  getEstadoLabel(t: Credencial): string {
    if (t.credencialCancelada) {
      return 'Cancelada';
    }
    return (t.estado || '').charAt(0).toUpperCase() + (t.estado || '').slice(1).toLowerCase();
  }

  formatVigencia(t: Credencial): string {
    if (!t.fechaEmision) {
      return '-';
    }
    const fecha = new Date(t.fechaEmision);
    if (Number.isNaN(fecha.getTime())) {
      return '-';
    }
    return `${fecha.getFullYear()}/${String(fecha.getMonth() + 1).padStart(2, '0')}`;
  }

  formatSaldo(t: Credencial): string {
    const saldo = t.cuentaSaldo ?? 0;
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(saldo);
  }

  descargarExcel(): void {
    this.credencialService.exportarExcel().subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'credenciales.csv';
        a.click();
        URL.revokeObjectURL(url);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMsg = (err.error as any)?.mensaje || 'Error al descargar excel ';
      }
    });
  }
}


