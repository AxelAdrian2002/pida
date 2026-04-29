import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { GrupoService } from '../../../services/grupo.service';
import { GrupoEmpleado, Grupo } from '../../../models/models';

@Component({
  selector: 'app-reporte-grupo',
  standalone: true,
  imports: [CommonModule],
  template: `
  <div class="p-4">
    <h5 class="mb-4"><i class="fas fa-table me-2 text-primary"></i>Reporte Grupo: <strong>{{ grupo?.nombre }}</strong></h5>

    <div class="card shadow-sm">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span>Empleados asignados ({{ empleados.length }})</span>
        <button class="btn btn-sm btn-outline-success" (click)="exportarCSV()">
          <i class="fas fa-download me-1"></i>Exportar CSV
        </button>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr><th>#</th><th>Núm. Empleado</th><th>Estado</th><th>Fecha Asignación</th><th>Asignó</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let e of empleados; let i = index">
              <td>{{ i + 1 }}</td>
              <td>{{ e.numeroEmpleado }}</td>
              <td><span class="badge" [ngClass]="e.activo ? 'bg-success' : 'bg-secondary'">{{ e.activo ? 'Activo' : 'Inactivo' }}</span></td>
              <td>{{ e.fechaAsignacion | date:'dd/MM/yyyy' }}</td>
              <td>{{ e.usuarioAsigno }}</td>
            </tr>
            <tr *ngIf="empleados.length === 0">
              <td colspan="5" class="text-center text-muted py-3">Sin empleados asignados</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  `
})
export class ReporteGrupoComponent implements OnInit {
  idGrupo = 0;
  grupo: Grupo | null = null;
  empleados: GrupoEmpleado[] = [];

  constructor(private route: ActivatedRoute, private grupoService: GrupoService) {}

  ngOnInit(): void {
    this.idGrupo = Number(this.route.snapshot.paramMap.get('id'));
    this.grupoService.obtener(this.idGrupo).subscribe({ next: res => this.grupo = res.datos });
    this.grupoService.reporte(this.idGrupo).subscribe({ next: res => this.empleados = res.datos || [] });
  }

  exportarCSV(): void {
    const headers = ['Num Empleado', 'Estado', 'Fecha Asignacion', 'Asigno'];
    const rows = this.empleados.map(e => [e.numeroEmpleado, e.activo ? 'Activo' : 'Inactivo', e.fechaAsignacion, e.usuarioAsigno ?? '']);
    const csv = [headers, ...rows].map(r => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `reporte_grupo_${this.idGrupo}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
