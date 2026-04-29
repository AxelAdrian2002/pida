import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SolicitudService } from '../../../services/solicitud.service';
import { DataSharingService } from '../../../services/data-sharing.service';

@Component({
  selector: 'app-solicitud-dispersion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
  <div class="p-4">
    <h5 class="mb-4"><i class="fas fa-file-invoice-dollar me-2 text-primary"></i>Crear Solicitud de Apoyo Económico</h5>

    <div class="card shadow-sm mb-3">
      <div class="card-body d-flex flex-wrap gap-2">
        <button class="btn btn-outline-primary" (click)="descargarPlantilla()" [disabled]="loading">
          Descargar Plantilla
        </button>
        <label class="btn btn-outline-secondary mb-0">
          Cargar archivo
          <input type="file" accept=".xlsx" hidden (change)="onFileSelected($event)">
        </label>
        <button class="btn btn-primary" (click)="procesar()" [disabled]="loading || !archivoSeleccionado">
          Procesar
        </button>
        <button class="btn btn-outline-danger" (click)="cancelar()" [disabled]="loading">
          Cancelar
        </button>
      </div>
    </div>

    <div class="alert alert-info" *ngIf="archivoNombre">Archivo: {{ archivoNombre }}</div>
    <div class="alert alert-success" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger" *ngIf="errorMsg">{{ errorMsg }}</div>

    <div class="card border-danger mb-3">
      <div class="card-body py-3">
        <div class="text-danger fw-semibold small">Favor de considerar los siguientes puntos para procesar correctamente la solicitud.</div>
        <ul class="small mb-0 mt-2">
          <li>Para generar esta solicitud, verifica que el archivo corresponda al periodo actual.</li>
          <li>El campo número de empleado es opcional en la plantilla.</li>
        </ul>
      </div>
    </div>

    <div class="card shadow-sm" *ngIf="pedidoProcesado">
      <div class="card-header">Prefactura</div>
      <div class="card-body">
        <div class="row g-2">
          <div class="col-md-4"><strong>Tipo:</strong> {{ pedidoProcesado.tipoPedido || 'DISPERSION' }}</div>
          <div class="col-md-4"><strong>Cliente:</strong> {{ pedidoProcesado.clienteId || '-' }}</div>
          <div class="col-md-4"><strong>Consignatario:</strong> {{ pedidoProcesado.consignatarioId || '-' }}</div>
          <div class="col-md-4"><strong>Monto:</strong> {{ pedidoProcesado.montoTotal || 0 | currency:'MXN' }}</div>
          <div class="col-md-8"><strong>Descripción:</strong> {{ pedidoProcesado.descripcion || 'Solicitud procesada' }}</div>
        </div>
      </div>
    </div>
  </div>
  `
})
export class SolicitudDispersionComponent {
  loading = false;
  exitoMsg = '';
  errorMsg = '';
  archivoNombre = '';
  archivoB64 = '';
  archivoSeleccionado = false;
  pedidoProcesado: any = null;

  constructor(
    private SolicitudService: SolicitudService,
    private dataSharingService: DataSharingService,
    private router: Router
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.archivoNombre = file.name;
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      this.errorMsg = 'Solo se permite archivo .xlsx';
      this.archivoSeleccionado = false;
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      this.archivoB64 = result.includes(',') ? result.split(',')[1] : result;
      this.archivoSeleccionado = true;
      this.errorMsg = '';
    };
    reader.onerror = () => {
      this.errorMsg = 'No fue posible leer el archivo.';
      this.archivoSeleccionado = false;
    };
    reader.readAsDataURL(file);
  }

  descargarPlantilla(): void {
    this.loading = true;
    this.exitoMsg = '';
    this.errorMsg = '';
    this.SolicitudService.getPlantilla('dispersion').subscribe({
      next: (blob) => {
        this.descargarBlob(blob, 'plantilla_dispersion.xlsx');
        this.exitoMsg = 'Plantilla solicitada correctamente.';
        this.loading = false;
      },
      error: err => {
        this.errorMsg = err.error?.mensaje || 'No fue posible obtener la plantilla.';
        this.loading = false;
      }
    });
  }

  procesar(): void {
    if (!this.archivoB64) {
      this.errorMsg = 'Selecciona un archivo para procesar.';
      return;
    }
    this.loading = true;
    this.exitoMsg = '';
    this.errorMsg = '';
    this.SolicitudService.procesaPedido('dispersion', this.archivoB64).subscribe({
      next: res => {
        const payload = res.datos as any;
        const ok = payload?.respuesta === true || payload?.respuesta === 'true';

        if (!ok && payload?.object) {
          this.descargarBase64(payload.object, this.archivoNombre || 'plantilla_con_errores.xlsx');
          this.exitoMsg = 'Se encontraron observaciones en el archivo. Revisa el archivo descargado.';
          this.loading = false;
          return;
        }

        this.pedidoProcesado = this.SolicitudService.normalizarPayloadPrefactura(payload);
        if (ok && this.pedidoProcesado) {
          this.dataSharingService.enviarRequest(this.pedidoProcesado);
          this.router.navigate(['/solicitudes/resumen'], { queryParams: { tipo: 'dispersion' } });
        } else {
          this.exitoMsg = 'Archivo procesado sin prefactura.';
        }
        this.loading = false;
      },
      error: err => {
        this.errorMsg = err.error?.mensaje || 'Error al procesar archivo.';
        this.loading = false;
      }
    });
  }

  cancelar(): void {
    this.archivoNombre = '';
    this.archivoB64 = '';
    this.archivoSeleccionado = false;
    this.pedidoProcesado = null;
    this.exitoMsg = '';
    this.errorMsg = '';
  }

  private descargarBlob(blob: Blob, nombre: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private descargarBase64(base64: string, nombre: string): void {
    const raw = base64.includes(',') ? base64.split(',')[1] : base64;
    const bytes = atob(raw);
    const buffer = new Uint8Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) {
      buffer[i] = bytes.charCodeAt(i);
    }
    const blob = new Blob([buffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    this.descargarBlob(blob, nombre);
  }
}


