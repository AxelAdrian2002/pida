import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EmpleadoService } from '../../services/empleado.service';
import { Empleado } from '../../models/models';

@Component({
  selector: 'app-empleados',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <div class="-page p-4">
    <div class="-header mb-3">
      <div>
        <h5 class="mb-1"><i class="fas fa-user-edit me-2"></i>Administración de Empleados</h5>
        <small class="text-muted">Consulta y mantenimiento de catálogo en modo </small>
      </div>
      <span class="badge text-bg-primary"></span>
    </div>

    <div class="card shadow-sm mb-3 -card">
      <div class="card-body">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label fw-semibold">Nombre</label>
            <input class="form-control" [(ngModel)]="filtroNombre" placeholder="Buscar por nombre">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Núm. Empleado</label>
            <input class="form-control" [(ngModel)]="filtroDept" placeholder="Número de empleado">
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
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr><th>Núm.</th><th>Nombre Completo</th><th>Teléfono</th><th>Email</th><th>Estatus</th><th>Acción</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let e of empleados">
              <td>{{ e.numeroEmpleado }}</td>
              <td>{{ e.nombre }} {{ e.apellidoPaterno }}</td>
              <td>{{ e.telefono || '-' }}</td>
              <td>{{ e.email || '-' }}</td>
              <td>
                <span class="badge" [ngClass]="e.activo ? 'bg-success' : 'bg-secondary'">{{ e.activo ? 'Activo' : 'Inactivo' }}</span>
              </td>
              <td>
                <a [routerLink]="['/empleados', e.numeroEmpleado, 'editar']" class="btn btn-sm btn-outline-primary">
                  <i class="fas fa-edit"></i>
                </a>
              </td>
            </tr>
            <tr *ngIf="empleados.length === 0">
              <td colspan="6" class="text-center text-muted py-3">Sin resultados</td>
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
export class EmpleadosComponent implements OnInit {
  empleados: Empleado[] = [];
  filtroNombre = '';
  filtroDept = '';
  page = 0;
  totalPages = 1;

  constructor(private empleadoService: EmpleadoService) {}
  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.empleadoService.listar(this.filtroNombre || undefined, this.filtroDept || undefined, this.page).subscribe({
      next: res => {
        const data = res.datos as any;
        this.empleados = data.content || [];
        this.totalPages = data.totalPages || 1;
      }
    });
  }

  descargarExcel(): void {
    this.empleadoService.exportarExcel(this.filtroNombre || undefined, this.filtroDept || undefined).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'datos_empleados_.xlsx';
        a.click();
        URL.revokeObjectURL(url);
      }
    });
  }
}

