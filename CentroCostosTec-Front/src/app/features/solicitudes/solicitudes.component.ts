import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SolicitudService } from '../../services/solicitud.service';
import { AuthService } from '../../services/auth.service';
import { Pedido } from '../../models/models';

@Component({
  selector: 'app-solicitudes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="p-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h5 class="mb-0"><i class="fas fa-list me-2 text-primary"></i>Consulta de Solicitudes Internas</h5>
      <button class="btn btn-success btn-sm" type="button" *ngIf="puedeAutorizar()" (click)="irAutorizar()">
        <i class="fas fa-check-circle me-1"></i>Aprobar solicitudes
      </button>
    </div>

    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label">Estado</label>
            <select class="form-select" [(ngModel)]="filtroEstado" (change)="cargar()">
              <option value="">Todos</option>
              <option>PENDIENTE</option>
              <option>AUTORIZADO</option>
              <option>RECHAZADO</option>
              <option>CANCELADO</option>
            </select>
          </div>
          <div class="col-auto">
            <button class="btn btn-outline-primary" (click)="cargar()">
              <i class="fas fa-search me-1"></i>Buscar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr><th>ID</th><th>Tipo</th><th>Descripción</th><th>Monto</th><th>Estado</th><th>Fecha</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let p of pedidos">
              <td>{{ p.idPedido }}</td>
              <td><span class="badge bg-secondary">{{ p.tipoPedido }}</span></td>
              <td>{{ p.descripcion }}</td>
              <td>{{ p.montoTotal | currency:'MXN' }}</td>
              <td><span class="badge" [ngClass]="getBadge(p.estado)">{{ p.estado }}</span></td>
              <td>{{ p.fechaCreacion | date:'dd/MM/yyyy' }}</td>
            </tr>
            <tr *ngIf="pedidos.length === 0">
              <td colspan="6" class="text-center text-muted py-3">Sin resultados</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-between align-items-center">
        <small>Página {{ page + 1 }} de {{ totalPages }}</small>
        <div class="btn-group">
          <button class="btn btn-sm btn-outline-secondary" [disabled]="page === 0" (click)="page = page - 1; cargar()">Anterior</button>
          <button class="btn btn-sm btn-outline-secondary" [disabled]="page >= totalPages - 1" (click)="page = page + 1; cargar()">Siguiente</button>
        </div>
      </div>
    </div>
  </div>
  `
})
export class SolicitudesComponent implements OnInit {
  pedidos: Pedido[] = [];
  filtroEstado = '';
  page = 0;
  totalPages = 1;

  constructor(
    private SolicitudService: SolicitudService,
    private authService: AuthService,
    private router: Router
  ) {}
  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.SolicitudService.listar(this.filtroEstado || undefined, this.page).subscribe({
      next: (res: any) => {
        const data = res.datos as any;
        this.pedidos = data.content || [];
        this.totalPages = data.totalPages || 1;
      }
    });
  }

  getBadge(estado: string): object {
    return { 'bg-warning text-dark': estado === 'PENDIENTE', 'bg-success': estado === 'AUTORIZADO', 'bg-danger': estado === 'RECHAZADO', 'bg-secondary': estado === 'CANCELADO' };
  }

  puedeAutorizar(): boolean {
    const rol = String(this.authService.getRol() || '').trim().toUpperCase();
    return rol === 'ADMIN' || rol === 'AUTORIZADOR';
  }

  irAutorizar(idPedido?: number): void {
    if (!this.puedeAutorizar()) {
      return;
    }
    const queryParams = idPedido ? { pedido: idPedido } : undefined;
    this.router.navigate(['/solicitudes/aprobacion'], { queryParams });
  }
}


