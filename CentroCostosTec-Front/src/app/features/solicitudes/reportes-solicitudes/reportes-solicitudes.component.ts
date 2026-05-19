import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesService } from '../../../services/reportes.service';

@Component({
  selector: 'app-reportes-solicitudes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes-solicitudes.component.html',
  styleUrls: ['./reportes-solicitudes.component.css']
})
export class ReportesSolicitudesComponent implements OnInit {

  resumen: any = null;
  auditoria: any = null;
  filtros = {
    fechaInicio: this.obtenerFechaHoy(-30),
    fechaFin: this.obtenerFechaHoy(),
    accion: '',
    tipoSolicitud: ''
  };

  cargandoResumen = false;
  cargandoAuditoria = false;
  error: string | null = null;
  activeTab = 'resumen'; // 'resumen' o 'auditoria'
  Object = Object; // Para usar Object.keys() en template

  constructor(private reportesService: ReportesService) {}

  ngOnInit() {
    this.cargarResumen();
    this.cargarAuditoria();
  }

  cargarResumen() {
    this.cargandoResumen = true;
    this.error = null;

    this.reportesService.obtenerResumen().subscribe({
      next: (response) => {
        this.resumen = response.data || {};
        this.cargandoResumen = false;
      },
      error: (err) => {
        console.error('Error cargando resumen:', err);
        this.error = 'Error al cargar el resumen';
        this.cargandoResumen = false;
      }
    });
  }

  cargarAuditoria() {
    this.cargandoAuditoria = true;
    this.error = null;

    this.reportesService.obtenerAuditoria(this.filtros.fechaInicio, this.filtros.fechaFin).subscribe({
      next: (response) => {
        this.auditoria = response.data || {};
        this.cargandoAuditoria = false;
      },
      error: (err) => {
        console.error('Error cargando auditoría:', err);
        this.error = 'Error al cargar la auditoría';
        this.cargandoAuditoria = false;
      }
    });
  }

  aplicarFiltros() {
    if (this.filtros.accion || this.filtros.tipoSolicitud) {
      this.filtrarAuditoria();
    } else {
      this.cargarAuditoria();
    }
  }

  filtrarAuditoria() {
    this.cargandoAuditoria = true;
    this.error = null;

    this.reportesService.filtrarAuditoria(this.filtros).subscribe({
      next: (response) => {
        this.auditoria = {
          registros: response.data || [],
          total_cambios: (response.data || []).length
        };
        this.cargandoAuditoria = false;
      },
      error: (err) => {
        console.error('Error filtrando auditoría:', err);
        this.error = 'Error al filtrar auditoría';
        this.cargandoAuditoria = false;
      }
    });
  }

  limpiarFiltros() {
    this.filtros = {
      fechaInicio: this.obtenerFechaHoy(-30),
      fechaFin: this.obtenerFechaHoy(),
      accion: '',
      tipoSolicitud: ''
    };
    this.cargarAuditoria();
  }

  obtenerFechaHoy(diasAtras: number = 0): string {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + diasAtras);
    return fecha.toISOString().split('T')[0];
  }

  cambiarTab(tab: string) {
    this.activeTab = tab;
  }

  obtenerPorcentajeAutorizacion(): number {
    if (!this.resumen) return 0;
    const total = this.resumen.creaciones || 0;
    const autorizadas = this.resumen.autorizaciones || 0;
    return total > 0 ? Math.round((autorizadas / total) * 100) : 0;
  }

  obtenerPorcentajeRechazo(): number {
    if (!this.resumen) return 0;
    const total = this.resumen.creaciones || 0;
    const rechazadas = this.resumen.rechazos || 0;
    return total > 0 ? Math.round((rechazadas / total) * 100) : 0;
  }
}
