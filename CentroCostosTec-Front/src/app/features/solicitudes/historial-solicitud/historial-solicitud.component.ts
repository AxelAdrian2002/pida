import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SolicitudService } from '../../../services/solicitud.service';

@Component({
  selector: 'app-historial-solicitud',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './historial-solicitud.component.html',
  styleUrls: ['./historial-solicitud.component.css']
})
export class HistorialSolicitudComponent implements OnInit {

  @Input() solicitudId: number = 0;
  
  historial: any[] = [];
  cargando = false;
  error: string | null = null;

  constructor(private solicitudService: SolicitudService) {}

  ngOnInit() {
    if (this.solicitudId > 0) {
      this.cargarHistorial();
    }
  }

  cargarHistorial() {
    this.cargando = true;
    this.error = null;

    this.solicitudService.obtenerHistorial(this.solicitudId).subscribe({
      next: (response) => {
        this.historial = response.data || [];
        this.cargando = false;
      },
      error: (err) => {
        console.error('Error cargando historial:', err);
        this.error = 'Error cargando el historial de cambios';
        this.cargando = false;
      }
    });
  }

  obtenerIconoAccion(accion: string): string {
    switch (accion) {
      case 'CREAR': return '➕';
      case 'AUTORIZAR': return '✅';
      case 'RECHAZAR': return '❌';
      case 'CANCELAR': return '⛔';
      default: return '📝';
    }
  }

  obtenerColorAccion(accion: string): string {
    switch (accion) {
      case 'CREAR': return 'info';
      case 'AUTORIZAR': return 'success';
      case 'RECHAZAR': return 'danger';
      case 'CANCELAR': return 'warning';
      default: return 'secondary';
    }
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '-';
    try {
      const date = new Date(fecha);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return fecha;
    }
  }
}
