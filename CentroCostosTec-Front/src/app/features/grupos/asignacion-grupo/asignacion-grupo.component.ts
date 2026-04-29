import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { GrupoService } from '../../../services/grupo.service';
import { EmpleadoService } from '../../../services/empleado.service';
import { AuthService } from '../../../services/auth.service';
import { GrupoEmpleado, Empleado, Grupo } from '../../../models/models';

@Component({
  selector: 'app-asignacion-grupo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
  <div class="p-4">
    <h5 class="mb-4"><i class="fas fa-user-plus me-2 text-primary"></i>Asignación a Grupo: <strong>{{ grupo?.nombre }}</strong></h5>

    <div class="row g-4">
      <div class="col-md-5">
        <div class="card shadow-sm">
          <div class="card-header d-flex justify-content-between align-items-center">
            <span>Lista de Empleados</span>
            <small class="text-muted">{{ totalEmpleados }} registros</small>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-sm align-middle">
                <thead class="table-light">
                  <tr>
                    <th>Núm. Empleado</th>
                    <th>Nombre</th>
                    <th class="text-end">Acción</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let e of empleadosDisponibles">
                    <td>{{ e.numeroEmpleado }}</td>
                    <td>{{ e.nombre }} {{ e.apellidoPaterno || '' }}</td>
                    <td class="text-end">
                      <button class="btn btn-outline-primary btn-sm" (click)="asignar(e)" [disabled]="loading">
                        <i class="fas fa-user-plus me-1"></i>Asignar
                      </button>
                    </td>
                  </tr>
                  <tr *ngIf="empleadosDisponibles.length === 0">
                    <td colspan="3" class="text-center text-muted">Sin empleados disponibles</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="d-flex justify-content-between mt-2">
              <button class="btn btn-sm btn-outline-secondary" (click)="paginaAnterior()" [disabled]="page === 0 || loading">
                Anterior
              </button>
              <small class="text-muted align-self-center">Página {{ page + 1 }} de {{ totalPages }}</small>
              <button class="btn btn-sm btn-outline-secondary" (click)="paginaSiguiente()" [disabled]="(page + 1) >= totalPages || loading">
                Siguiente
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-7">
        <div class="card shadow-sm">
          <div class="card-header">Empleados en el Grupo</div>
          <div class="table-responsive">
            <table class="table table-sm mb-0">
              <thead class="table-light">
                <tr><th>Núm.</th><th>Estado</th><th>Fecha Asignación</th></tr>
              </thead>
              <tbody>
                <tr *ngFor="let e of asignaciones">
                  <td>{{ e.numeroEmpleado }}</td>
                  <td><span class="badge" [ngClass]="e.activo ? 'bg-success' : 'bg-secondary'">{{ e.activo ? 'Activo' : 'Inactivo' }}</span></td>
                  <td>{{ e.fechaAsignacion | date:'dd/MM/yyyy' }}</td>
                </tr>
                <tr *ngIf="asignaciones.length === 0">
                  <td colspan="3" class="text-center text-muted">Sin empleados asignados</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="alert alert-success mt-3" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>
  </div>
  `
})
export class AsignacionGrupoComponent implements OnInit {
  idGrupo = 0;
  grupo: Grupo | null = null;
  asignaciones: GrupoEmpleado[] = [];
  empleadosDisponibles: Empleado[] = [];

  page = 0;
  size = 10;
  totalPages = 1;
  totalEmpleados = 0;

  loading = false;
  exitoMsg = '';
  errorMsg = '';

  constructor(private route: ActivatedRoute,
              private grupoService: GrupoService, private empleadoService: EmpleadoService,
              private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.idGrupo = Number(this.route.snapshot.paramMap.get('id'));
    this.grupoService.obtener(this.idGrupo).subscribe({ next: res => this.grupo = res.datos });
    this.grupoService.reporte(this.idGrupo).subscribe({ next: res => this.asignaciones = res.datos || [] });
    this.cargarEmpleados();
  }

  cargarEmpleados(): void {
    this.loading = true;
    this.empleadoService.listar(undefined, undefined, this.page, this.size).subscribe({
      next: res => {
        const data = res?.datos;
        this.empleadosDisponibles = data?.content || [];
        this.totalPages = Math.max(1, data?.totalPages || 1);
        this.totalEmpleados = data?.totalElements || 0;
        this.errorMsg = '';
      },
      error: () => {
        this.empleadosDisponibles = [];
        this.totalPages = 1;
        this.totalEmpleados = 0;
        this.errorMsg = 'No se pudo cargar la lista de empleados';
      },
      complete: () => (this.loading = false)
    });
  }

  asignar(empleado: Empleado): void {
    this.loading = true;
    const user = this.authService.getUser();
    this.grupoService.asignarEmpleado(this.idGrupo, empleado.idEmpleado,
      empleado.numeroEmpleado, user?.username ?? '').subscribe({
      next: res => {
        this.exitoMsg = 'Empleado asignado correctamente';
        this.asignaciones.unshift(res.datos);
        this.router.navigate(['/grupos']).then(() => window.location.reload());
      },
      error: err => { this.errorMsg = err.error?.mensaje || 'Error al asignar'; this.loading = false; }
    });
  }

  paginaAnterior(): void {
    if (this.page === 0 || this.loading) return;
    this.page -= 1;
    this.cargarEmpleados();
  }

  paginaSiguiente(): void {
    if ((this.page + 1) >= this.totalPages || this.loading) return;
    this.page += 1;
    this.cargarEmpleados();
  }
}
