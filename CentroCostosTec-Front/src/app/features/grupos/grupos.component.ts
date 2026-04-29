import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { GrupoService } from '../../services/grupo.service';
import { Grupo } from '../../models/models';

@Component({
  selector: 'app-grupos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <div class="-page p-4">
    <div class="-header mb-4">
      <div>
        <h5 class="mb-1"><i class="fas fa-users me-2"></i>Lista de Direcciones de Entrega</h5>
      </div>
      <div class="d-flex gap-2">
        <span class="badge text-bg-primary align-self-center"></span>
        <button class="btn btn-outline-success btn-sm" (click)="descargarExcel()">
          <i class="fas fa-file-excel me-1"></i>Excel 
        </button>
        <a routerLink="/grupos/registro" class="btn btn-primary btn-sm">
          <i class="fas fa-plus me-1"></i>Registrar nuevo grupo
        </a>
      </div>
    </div>

    <div class="card shadow-sm -card">
      <div class="card-header bg-white border-0 pb-0">
        <strong>Resultados</strong>
      </div>
      <div class="card-body border-bottom">
        <div class="row g-2 align-items-end">
          <div class="col-md-4">
            <label class="form-label fw-semibold">Buscar por grupoID</label>
            <input class="form-control" [(ngModel)]="filtro" (input)="aplicarFiltro()" placeholder="Grupo o descripción">
          </div>
          <div class="col-md-2">
            <button class="btn btn-outline-primary" type="button" (click)="aplicarFiltro()">
              <i class="fas fa-search me-1"></i>Buscar
            </button>
          </div>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0 align-middle">
          <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>Grupo ID</th>
              <th>Descripción Grupo</th>
              <th>Calle</th>
              <th>Número</th>
              <th>Colonia</th>
              <th>C.P.</th>
              <th>Delegación</th>
              <th>Estado</th>
              <th>Contacto</th>
              <th>Teléfono</th>
              <th>Contacto</th>
              <th>Teléfono</th>
              <th>Horario</th>
              <th>Estatus</th>
              <th>Fecha de Modificación</th>
              <th>Observación</th>
              <th class="sticky-actions">Opciones</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let g of gruposPagina">
              <td>{{ g.iddirecciones || g.idGrupo || '-' }}</td>
              <td>{{ g.grupoid || g.nombre || '-' }}</td>
              <td>{{ g.descripcion || '-' }}</td>
              <td>{{ g.calle || '-' }}</td>
              <td>{{ g.numero || '-' }}</td>
              <td>{{ g.colonia || '-' }}</td>
              <td>{{ g.codigopostal || g.codigoPostal || '-' }}</td>
              <td>{{ g.delegacion || '-' }}</td>
              <td>{{ g.estado || '-' }}</td>
              <td>{{ g.nombre || g.nombreContacto1 || '-' }}</td>
              <td>{{ g.telefono || g.telefono1 || '-' }}</td>
              <td>{{ g.nombre2 || g.nombreContacto2 || '-' }}</td>
              <td>{{ g.telefono2 || '-' }}</td>
              <td>{{ g.horario || '-' }}</td>
              <td>
                <span class="badge" [ngClass]="(g.estatus ?? g.activo) ? 'bg-success' : 'bg-warning text-dark'">
                  {{ (g.estatus ?? g.activo) ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td>{{ (g.fecha || g.fechaModificacion || g.fechaAlta) ? ((g.fecha || g.fechaModificacion || g.fechaAlta) | date:'dd/MM/yyyy HH:mm') : '-' }}</td>
              <td>{{ g.observacion || '-' }}</td>
              <td class="text-nowrap sticky-actions">
                <button class="btn btn-sm btn-outline-dark me-1" type="button" (click)="editar(g)">Editar</button>
                <button class="btn btn-sm btn-outline-primary me-1" type="button" (click)="asignar(g)">Asignar</button>
                <button class="btn btn-sm btn-outline-secondary" type="button" (click)="reporte(g)">Reporte</button>
              </td>
            </tr>
            <tr *ngIf="gruposFiltrados.length === 0">
              <td colspan="18" class="text-center text-muted py-3">Sin registros</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer d-flex justify-content-between align-items-center">
        <small>Mostrando {{ gruposPagina.length }} de {{ gruposFiltrados.length }} registros | Página {{ page + 1 }} de {{ totalPages }}</small>
        <div class="btn-group">
          <button class="btn btn-sm btn-outline-secondary" [disabled]="page === 0" (click)="paginaAnterior()">Anterior</button>
          <button class="btn btn-sm btn-outline-secondary" [disabled]="page >= totalPages - 1" (click)="paginaSiguiente()">Siguiente</button>
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

    .sticky-actions {
      position: sticky;
      right: 0;
      z-index: 2;
      background: #ffffff;
      box-shadow: -8px 0 8px -8px rgba(0, 0, 0, 0.2);
    }

    thead .sticky-actions {
      z-index: 3;
      background: #f8f9fa;
    }
  `]
})
export class GruposComponent implements OnInit {
  readonly pageSize = 10;
  page = 0;
  totalPages = 1;
  grupos: Grupo[] = [];
  gruposFiltrados: Grupo[] = [];
  gruposPagina: Grupo[] = [];
  filtro = '';

  constructor(private grupoService: GrupoService, private router: Router) {}
  ngOnInit(): void {
    this.grupoService.listar().subscribe({
      next: res => {
        this.grupos = res.datos || [];
        this.gruposFiltrados = [...this.grupos];
        this.aplicarFiltro();
      }
    });
  }

  aplicarFiltro(): void {
    const criterio = (this.filtro || '').trim().toLowerCase();
    if (!criterio) {
      this.gruposFiltrados = [...this.grupos];
      this.page = 0;
      this.actualizarPagina();
      return;
    }
    this.gruposFiltrados = this.grupos.filter(g => {
      const grupoId = (g.grupoid || g.nombre || '').toLowerCase();
      const descripcion = (g.descripcion || '').toLowerCase();
      return grupoId.includes(criterio) || descripcion.includes(criterio);
    });
    this.page = 0;
    this.actualizarPagina();
  }

  actualizarPagina(): void {
    this.totalPages = Math.max(1, Math.ceil(this.gruposFiltrados.length / this.pageSize));
    if (this.page >= this.totalPages) {
      this.page = this.totalPages - 1;
    }
    const inicio = this.page * this.pageSize;
    this.gruposPagina = this.gruposFiltrados.slice(inicio, inicio + this.pageSize);
  }

  paginaAnterior(): void {
    if (this.page === 0) {
      return;
    }
    this.page--;
    this.actualizarPagina();
  }

  paginaSiguiente(): void {
    if (this.page >= this.totalPages - 1) {
      return;
    }
    this.page++;
    this.actualizarPagina();
  }

  descargarExcel(): void {
    this.grupoService.exportarExcelGrupos().subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'grupos_.csv';
        a.click();
        URL.revokeObjectURL(url);
      }
    });
  }

  editar(g: Grupo): void {
    const id = this.resolveId(g);
    if (!id) {
      return;
    }
    this.router.navigate(['/grupos/registro'], { queryParams: { id } });
  }

  asignar(g: Grupo): void {
    const id = this.resolveId(g);
    if (!id) {
      return;
    }
    this.router.navigate(['/grupos', id, 'asignacion']);
  }

  reporte(g: Grupo): void {
    const id = this.resolveId(g);
    if (!id) {
      return;
    }
    this.router.navigate(['/grupos', id, 'reporte']);
  }

  private resolveId(g: Grupo): number {
    return Number(g.iddirecciones || g.idGrupo || 0);
  }
}

