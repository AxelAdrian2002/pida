import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SolicitudService } from '../../../services/solicitud.service';

@Component({
  selector: 'app-autorizar-solicitud',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="p-4">
    <h5 class="mb-4"><i class="fas fa-check-circle me-2 text-success"></i>Aprobación de Solicitudes</h5>

    <div class="card shadow-sm mb-3">
      <div class="card-body row g-2 align-items-end">
        <div class="col-md-3">
          <button class="btn btn-outline-secondary" (click)="cargarTabla(true)" [disabled]="cargandoTabla">
            <span *ngIf="cargandoTabla" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
            Actualizar
          </button>
        </div>
        <div class="col-md-9 text-md-end">
          <div><strong>Saldo monedero:</strong> {{ saldoMonedero | currency:'MXN' }}</div>
          <div><strong>Adeudo:</strong> {{ adeudo | currency:'MXN' }}</div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr><th>Pedido</th><th>Cliente</th><th>Fecha</th><th>Servicio</th><th>Importe</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            <tr *ngIf="cargandoTabla">
              <td colspan="6" class="text-center py-4">
                <span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                Cargando solicitudes por aprobar...
              </td>
            </tr>
            <tr *ngFor="let row of filas">
              <td>{{ row.pedidoid }}</td>
              <td>{{ row.cliente || (row.clienteid + '-' + row.consignatarioid) }}</td>
              <td>{{ row.fechacreacion || '-' }}</td>
              <td>{{ row.descripcion || row.concepto || '-' }}</td>
              <td>{{ (row.total ?? row.monto ?? 0) | currency:'MXN' }}</td>
              <td>
                <button class="btn btn-sm btn-outline-primary" (click)="seleccionar(row)">Detalles</button>
              </td>
            </tr>
            <tr *ngIf="!cargandoTabla && filas.length === 0">
              <td colspan="6" class="text-center text-muted py-3">No hay solicitudes pendientes</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-between align-items-center">
        <small>Mostrando {{ filas.length }} de {{ totalRecords }} registros | Página {{ page + 1 }} de {{ totalPages }}</small>
        <div class="btn-group">
          <button class="btn btn-sm btn-outline-secondary" [disabled]="cargandoTabla || page === 0" (click)="paginaAnterior()">Anterior</button>
          <button class="btn btn-sm btn-outline-secondary" [disabled]="cargandoTabla || page >= totalPages - 1" (click)="paginaSiguiente()">Siguiente</button>
        </div>
      </div>
    </div>

    <div class="card shadow-sm mt-3" *ngIf="pedidoSeleccionado" #panelDetalle>
      <div class="card-header">Detalles de la solicitud a aprobar</div>
      <div class="card-body">
        <div class="row g-2 mb-2">
          <div class="col-md-4"><strong>Cliente-Consignatario:</strong> {{ pedidoSeleccionado.clienteid }}-{{ pedidoSeleccionado.consignatarioid }}</div>
          <div class="col-md-4"><strong>Pedido:</strong> {{ pedidoSeleccionado.pedidoid }}</div>
          <div class="col-md-4"><strong>Monto de la solicitud:</strong> {{ (pedidoSeleccionado.total ?? pedidoSeleccionado.monto ?? 0) | currency:'MXN' }}</div>
        </div>
        <div class="mb-2">
          <label class="form-label">(Opcional) Concepto</label>
          <input class="form-control" [(ngModel)]="pedidoSeleccionado.concepto">
        </div>
        <div class="form-check mb-3">
          <input class="form-check-input" type="checkbox" id="checkAuth" [(ngModel)]="confirmarAutorizacion">
          <label class="form-check-label" for="checkAuth">Aprobar la solicitud {{ pedidoSeleccionado.pedidoid }}</label>
        </div>
        <div class="d-flex gap-2">
          <button class="btn btn-outline-secondary" (click)="cancelarSeleccion()" [disabled]="procesandoId === pedidoSeleccionado.pedidoid">Cancelar</button>
          <button class="btn btn-primary" (click)="autorizarSeleccionado()" [disabled]="!confirmarAutorizacion || procesandoId === pedidoSeleccionado.pedidoid">
            <span *ngIf="procesandoId === pedidoSeleccionado.pedidoid" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
            Aprobar solicitud
          </button>
        </div>
      </div>
    </div>

    <div class="alert alert-success mt-3" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>
  </div>
  `
})
export class AutorizarSolicitudComponent implements OnInit {
  @ViewChild('panelDetalle') panelDetalle?: ElementRef<HTMLDivElement>;

  readonly pageSize = 10;
  page = 0;
  totalRecords = 0;
  totalPages = 1;
  filas: any[] = [];
  cargandoTabla = false;
  procesandoId: number | null = null;
  pedidoSeleccionado: any = null;
  confirmarAutorizacion = false;
  saldoMonedero = 0;
  adeudo = 0;
  exitoMsg = '';
  errorMsg = '';

  constructor(private SolicitudService: SolicitudService) {}

  ngOnInit(): void {
    this.cargarTabla(true);
  }

  cargarTabla(resetPage = false): void {
    if (resetPage) {
      this.page = 0;
    }
    this.cargandoTabla = true;
    this.errorMsg = '';
    this.SolicitudService.getDataTable({ start: this.page * this.pageSize, length: this.pageSize, draw: this.page + 1, search: '' }).subscribe({
      next: res => {
        const dt = res.datos;
        this.filas = (res.datos?.data || []) as any[];
        this.totalRecords = Number(dt?.recordsFiltered ?? dt?.recordsTotal ?? this.filas.length ?? 0);
        this.totalPages = Math.max(1, Math.ceil(this.totalRecords / this.pageSize));
        this.saldoMonedero = Number(dt?.monederoSaldo ?? 0);
        this.adeudo = Number(dt?.creditoSaldo ?? 0);
        this.cargandoTabla = false;
      },
      error: err => {
        this.errorMsg = err.error?.mensaje || 'No fue posible cargar pedidos por autorizar.';
        this.cargandoTabla = false;
      }
    });
  }

  seleccionar(row: any): void {
    const cliente = row.cliente || `${row.clienteid}-${row.consignatarioid}`;
    const [clienteidRaw, consignatarioidRaw] = String(cliente).split('-').map((v: string) => v?.trim());
    this.pedidoSeleccionado = {
      ...row,
      clienteid: Number(clienteidRaw || row.clienteid || 0),
      consignatarioid: Number(consignatarioidRaw || row.consignatarioid || 0),
      concepto: row.concepto || `Aprobacion de solicitud ${row.pedidoid}`
    };
    this.confirmarAutorizacion = false;

    // El panel queda debajo de la tabla; movemos la vista para que el usuario lo vea.
    setTimeout(() => {
      this.panelDetalle?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 0);
  }

  autorizarSeleccionado(): void {
    if (!this.pedidoSeleccionado) {
      return;
    }
    const row = this.pedidoSeleccionado;
    this.procesandoId = Number(row.pedidoid);
    this.exitoMsg = '';
    this.errorMsg = '';
    this.SolicitudService.sendAuthorizationOrder({
      pedidoid: row.pedidoid,
      clienteid: row.clienteid,
      consignatarioid: row.consignatarioid,
      monto: row.total ?? row.monto ?? 0,
      concepto: row.concepto || 'Aprobacion de solicitud'
    }).subscribe({
      next: () => {
        this.exitoMsg = `Solicitud ${row.pedidoid} aprobada correctamente.`;
        this.filas = this.filas.filter(x => Number(x.pedidoid) !== Number(row.pedidoid));
        this.pedidoSeleccionado = null;
        this.confirmarAutorizacion = false;
        this.procesandoId = null;
        if (this.filas.length === 0 && this.page > 0) {
          this.page--;
        }
        this.cargarTabla();
      },
      error: err => {
        this.errorMsg = err.error?.mensaje || 'Error al aprobar la solicitud.';
        this.procesandoId = null;
      }
    });
  }

  cancelarSeleccion(): void {
    this.pedidoSeleccionado = null;
    this.confirmarAutorizacion = false;
  }

  paginaAnterior(): void {
    if (this.page === 0) {
      return;
    }
    this.page--;
    this.cargarTabla();
  }

  paginaSiguiente(): void {
    if (this.page >= this.totalPages - 1) {
      return;
    }
    this.page++;
    this.cargarTabla();
  }
}


