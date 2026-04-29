import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SolicitudService } from '../../../services/solicitud.service';
import { DataSharingService } from '../../../services/data-sharing.service';

@Component({
  selector: 'app-solicitud-stock',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="p-4">
    <h5 class="mb-4"><i class="fas fa-boxes-stacked me-2 text-warning"></i>Crear Solicitud de Reposición de Saldo</h5>
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <label class="form-label">No. de asignaciones</label>
        <input class="form-control mb-3" type="text" [(ngModel)]="numeroTarjetas" (input)="sanitizarNumero($event)" placeholder="No. de asignaciones">
        <div class="d-flex flex-wrap gap-2">
          <button class="btn btn-outline-danger" (click)="cancelar()" [disabled]="loading">Cancelar</button>
          <button class="btn btn-warning" (click)="procesar()" [disabled]="loading || !numeroTarjetas">Procesar</button>
        </div>
      </div>
    </div>
    <div class="alert alert-success mt-3" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>
    <div class="card shadow-sm" *ngIf="pedidoProcesado">
      <div class="card-header">Prefactura</div>
      <div class="card-body">
        <strong>Monto:</strong> {{ resolverMontoPedido(pedidoProcesado) | currency:'MXN' }}
      </div>
    </div>
  </div>
  `
})
export class SolicitudStockComponent {
  loading = false;
  exitoMsg = '';
  errorMsg = '';
  numeroTarjetas = '';
  pedidoProcesado: any = null;

  constructor(
    private SolicitudService: SolicitudService,
    private dataSharingService: DataSharingService,
    private router: Router
  ) {}

  procesar(): void {
    if (!this.numeroTarjetas) {
      this.errorMsg = 'Ingresa el número de asignaciones.';
      return;
    }
    this.loading = true;
    this.exitoMsg = '';
    this.errorMsg = '';
    this.SolicitudService.procesaPedidoStock(this.numeroTarjetas).subscribe({
      next: res => {
        this.pedidoProcesado = this.SolicitudService.normalizarPayloadPrefactura(res.datos as any);
        if (this.pedidoProcesado) {
          this.dataSharingService.enviarRequest(this.pedidoProcesado);
          this.router.navigate(['/solicitudes/resumen'], { queryParams: { tipo: 'stock' } });
        } else {
          this.exitoMsg = 'Proceso ejecutado.';
        }
        this.loading = false;
      },
      error: err => {
        this.errorMsg = err.error?.mensaje || 'Error al procesar la solicitud de reposición.';
        this.loading = false;
      }
    });
  }

  cancelar(): void {
    this.numeroTarjetas = '';
    this.pedidoProcesado = null;
    this.exitoMsg = '';
    this.errorMsg = '';
  }

  sanitizarNumero(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.numeroTarjetas = (input.value || '').replace(/[^0-9]/g, '').slice(0, 5);
  }

  resolverMontoPedido(pedido: any): number {
    const raw =
      pedido?.montoTotal ??
      pedido?.prefactura?.total ??
      pedido?.total ??
      0;

    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}


