import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SolicitudService } from '../../../services/solicitud.service';

@Component({
  selector: 'app-rechazar-solicitud-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal fade" id="rechazarModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <span *ngIf="operacion === 'RECHAZAR'">Rechazar Solicitud</span>
              <span *ngIf="operacion === 'CANCELAR'">Cancelar Solicitud</span>
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p *ngIf="operacion === 'RECHAZAR'" class="text-muted">
              <strong>ID Solicitud:</strong> {{ solicitudId }}
            </p>
            <p *ngIf="operacion === 'CANCELAR'" class="text-muted">
              <strong>ID Solicitud:</strong> {{ solicitudId }}
            </p>
            <div class="alert alert-warning" *ngIf="operacion === 'RECHAZAR'">
              <strong>Advertencia:</strong> Esta acción rechazará la solicitud y no podrá ser revertida.
            </div>
            <div class="alert alert-warning" *ngIf="operacion === 'CANCELAR'">
              <strong>Advertencia:</strong> Esta acción cancelará la solicitud. Se requiere especificar el motivo.
            </div>
            
            <div class="mb-3">
              <label for="motivo" class="form-label">
                <span *ngIf="operacion === 'RECHAZAR'">Motivo del Rechazo</span>
                <span *ngIf="operacion === 'CANCELAR'">Motivo de la Cancelación</span>
                <span class="text-danger">*</span>
              </label>
              <textarea 
                id="motivo"
                class="form-control"
                [(ngModel)]="motivo"
                rows="4"
                placeholder="Ingrese el motivo..."
                [disabled]="procesando">
              </textarea>
              <small class="text-muted">Máximo 500 caracteres</small>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" [disabled]="procesando">
              Cancelar
            </button>
            <button 
              type="button" 
              class="btn"
              [ngClass]="operacion === 'RECHAZAR' ? 'btn-danger' : 'btn-warning'"
              (click)="confirmar()"
              [disabled]="!motivo?.trim() || procesando">
              <span *ngIf="!procesando">
                <span *ngIf="operacion === 'RECHAZAR'">Rechazar</span>
                <span *ngIf="operacion === 'CANCELAR'">Cancelar</span>
              </span>
              <span *ngIf="procesando">
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Procesando...
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class RechazarSolicitudDialogComponent {
  @Input() solicitudId: number | null = null;
  @Input() operacion: 'RECHAZAR' | 'CANCELAR' = 'RECHAZAR';
  @Output() confirmado = new EventEmitter<{ motivo: string }>();
  
  motivo: string = '';
  procesando = false;

  constructor(private solicitudService: SolicitudService) {}

  confirmar() {
    if (!this.motivo?.trim()) {
      alert('Por favor ingrese un motivo');
      return;
    }

    if (!this.solicitudId) {
      alert('ID de solicitud no especificado');
      return;
    }

    this.procesando = true;

    if (this.operacion === 'RECHAZAR') {
      this.solicitudService.rechazar(this.solicitudId, this.motivo).subscribe({
        next: (response) => {
          this.procesando = false;
          this.confirmado.emit({ motivo: this.motivo });
          this.motivo = '';
          const modal = document.getElementById('rechazarModal');
          if (modal) {
            const bootstrapModal = (window as any).bootstrap?.Modal?.getInstance(modal);
            bootstrapModal?.hide();
          }
        },
        error: (err) => {
          this.procesando = false;
          console.error('Error rechazando solicitud:', err);
          alert('Error al rechazar la solicitud: ' + (err.error?.mensaje || err.message));
        }
      });
    } else {
      this.solicitudService.cancelar(this.solicitudId, this.motivo).subscribe({
        next: (response) => {
          this.procesando = false;
          this.confirmado.emit({ motivo: this.motivo });
          this.motivo = '';
          const modal = document.getElementById('rechazarModal');
          if (modal) {
            const bootstrapModal = (window as any).bootstrap?.Modal?.getInstance(modal);
            bootstrapModal?.hide();
          }
        },
        error: (err) => {
          this.procesando = false;
          console.error('Error cancelando solicitud:', err);
          alert('Error al cancelar la solicitud: ' + (err.error?.mensaje || err.message));
        }
      });
    }
  }
}
