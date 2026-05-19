import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EmpresaConfiguracion } from '../../models/models';
import { AuthService } from '../../services/auth.service';
import { EmpresaAdminService } from '../../services/empresa-admin.service';

interface CargaResumen {
  insertados: number;
  actualizados: number;
  rechazados: number;
  usuariosGenerados: number;
  credencialesAsignadas: number;
  credencialesGeneradas: number;
}

interface CargaCredencialesResumen {
  insertados: number;
  actualizados: number;
  rechazados: number;
}

interface AsignacionAutomaticaResumen {
  asignadas: number;
  sinStock: number;
  sinEmpleado: number;
  candidatosEmpleados: number;
  stockDisponible: number;
}

@Component({
  selector: 'app-empresa-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="-page p-4">
    <div class="-header mb-3">
      <div>
        <h5 class="mb-1"><i class="fas fa-building me-2"></i>Configuracion de empresa</h5>
        <small class="text-muted">Administra branding, perfil y carga de colaboradores para tu empresa.</small>
      </div>
      <span class="badge text-bg-primary">ADMIN</span>
    </div>

    <div class="card shadow-sm mb-3 -card">
      <div class="card-header bg-white border-0 pb-0">
        <strong>Carga masiva de empleados</strong>
      </div>
      <div class="card-body">
        <div class="d-flex flex-wrap gap-2 align-items-center mb-3">
          <button class="btn btn-outline-primary" type="button" (click)="descargarPlantilla()" [disabled]="descargandoPlantilla">
            <i class="fas fa-download me-1"></i>{{ descargandoPlantilla ? 'Descargando...' : 'Descargar plantilla CSV' }}
          </button>

          <input
            #fileInput
            type="file"
            accept=".csv,text/csv"
            class="form-control"
            style="max-width: 360px;"
            (change)="onFileSelected($event)"
          >

          <button class="btn btn-primary" type="button" (click)="subirEmpleados()" [disabled]="!archivoSeleccionado || cargandoEmpleados">
            <i class="fas fa-upload me-1"></i>{{ cargandoEmpleados ? 'Procesando...' : 'Cargar empleados' }}
          </button>
        </div>

        <div class="row g-2" *ngIf="resumenCarga">
          <div class="col-md-4">
            <div class="alert alert-success mb-0"><strong>{{ resumenCarga.insertados }}</strong> insertados</div>
          </div>
          <div class="col-md-4">
            <div class="alert alert-info mb-0"><strong>{{ resumenCarga.actualizados }}</strong> actualizados</div>
          </div>
          <div class="col-md-4">
            <div class="alert alert-warning mb-0"><strong>{{ resumenCarga.rechazados }}</strong> rechazados</div>
          </div>
          <div class="col-md-12">
            <div class="alert alert-primary mb-0"><strong>{{ resumenCarga.usuariosGenerados }}</strong> usuarios creados y enviados por correo con credenciales de acceso</div>
          </div>
          <div class="col-md-12">
            <div class="alert alert-secondary mb-0">
              <strong>{{ resumenCarga.credencialesAsignadas }}</strong> credenciales asignadas automáticamente
              (<strong>{{ resumenCarga.credencialesGeneradas }}</strong> generadas nuevas)
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm mb-3 -card">
      <div class="card-header bg-white border-0 pb-0">
        <strong>Credenciales (stock inicial y carga masiva)</strong>
      </div>
      <div class="card-body">
        <div class="row g-3 align-items-end mb-3">
          <div class="col-md-3">
            <label class="form-label fw-semibold">Cantidad inicial</label>
            <input type="number" min="1" max="5000" class="form-control" [(ngModel)]="loteCantidad" name="loteCantidad">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Número inicial (opcional)</label>
            <input type="number" min="1" class="form-control" [(ngModel)]="loteInicio" name="loteInicio">
          </div>
          <div class="col-md-6 d-flex gap-2 flex-wrap">
            <button class="btn btn-primary" type="button" (click)="generarLoteInicialCredenciales()" [disabled]="generandoLoteCredenciales">
              <i class="fas fa-magic me-1"></i>{{ generandoLoteCredenciales ? 'Generando...' : 'Generar lote inicial' }}
            </button>
            <button class="btn btn-outline-secondary" type="button" (click)="asignarCredencialesAutomaticamente()" [disabled]="asignandoAutomaticoCredenciales">
              <i class="fas fa-random me-1"></i>{{ asignandoAutomaticoCredenciales ? 'Asignando...' : 'Asignación automática' }}
            </button>
            <button class="btn btn-outline-primary" type="button" (click)="descargarPlantillaCredenciales()" [disabled]="descargandoPlantillaCredenciales">
              <i class="fas fa-download me-1"></i>{{ descargandoPlantillaCredenciales ? 'Descargando...' : 'Descargar plantilla credenciales' }}
            </button>
          </div>
        </div>

        <div class="d-flex flex-wrap gap-2 align-items-center mb-3">
          <input
            type="file"
            accept=".csv,text/csv"
            class="form-control"
            style="max-width: 360px;"
            (change)="onCredencialesFileSelected($event)"
          >

          <button class="btn btn-primary" type="button" (click)="subirCredenciales()" [disabled]="!archivoCredencialesSeleccionado || cargandoCredenciales">
            <i class="fas fa-upload me-1"></i>{{ cargandoCredenciales ? 'Procesando...' : 'Cargar credenciales' }}
          </button>
        </div>

        <div class="row g-2" *ngIf="resumenCredenciales">
          <div class="col-md-4">
            <div class="alert alert-success mb-0"><strong>{{ resumenCredenciales.insertados }}</strong> insertadas</div>
          </div>
          <div class="col-md-4">
            <div class="alert alert-info mb-0"><strong>{{ resumenCredenciales.actualizados }}</strong> actualizadas</div>
          </div>
          <div class="col-md-4">
            <div class="alert alert-warning mb-0"><strong>{{ resumenCredenciales.rechazados }}</strong> rechazadas</div>
          </div>
        </div>

        <div class="row g-2 mt-2" *ngIf="resumenAsignacionAutomatica">
          <div class="col-md-3">
            <div class="alert alert-success mb-0"><strong>{{ resumenAsignacionAutomatica.asignadas }}</strong> asignadas</div>
          </div>
          <div class="col-md-3">
            <div class="alert alert-warning mb-0"><strong>{{ resumenAsignacionAutomatica.sinStock }}</strong> sin stock</div>
          </div>
          <div class="col-md-3">
            <div class="alert alert-info mb-0"><strong>{{ resumenAsignacionAutomatica.sinEmpleado }}</strong> stock sin empleado</div>
          </div>
          <div class="col-md-3">
            <div class="alert alert-secondary mb-0">Candidatos: <strong>{{ resumenAsignacionAutomatica.candidatosEmpleados }}</strong></div>
          </div>
        </div>
      </div>
    </div>

    <form class="card shadow-sm mb-3 -card" (ngSubmit)="guardarConfiguracion()">
      <div class="card-header bg-white border-0 pb-0">
        <strong>Branding de empresa</strong>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label fw-semibold">Codigo empresa</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.codigoEmpresa" name="codigoEmpresa" readonly>
          </div>
          <div class="col-md-8">
            <label class="form-label fw-semibold">Nombre empresa</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.nombreEmpresa" name="nombreEmpresa" readonly>
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Color primario</label>
            <div class="d-flex gap-2 align-items-center">
              <input type="color" class="form-control form-control-color" [(ngModel)]="configuracion.empresa.colorPrimario" name="colorPrimarioPicker">
              <input class="form-control" [(ngModel)]="configuracion.empresa.colorPrimario" name="colorPrimario" placeholder="#0d6efd">
            </div>
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Color secundario</label>
            <div class="d-flex gap-2 align-items-center">
              <input type="color" class="form-control form-control-color" [(ngModel)]="configuracion.empresa.colorSecundario" name="colorSecundarioPicker">
              <input class="form-control" [(ngModel)]="configuracion.empresa.colorSecundario" name="colorSecundario" placeholder="#198754">
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold">Logo URL</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.logoUrl" name="logoUrl" placeholder="https://...">
          </div>
          <div class="col-12">
            <div class="brand-preview p-3 rounded-4 d-flex align-items-center justify-content-between flex-wrap gap-3"
                 [style.background]="configuracion.empresa.colorSecundario || '#334155'">
              <div class="d-flex align-items-center gap-3">
                <img *ngIf="configuracion.empresa.logoUrl" [src]="configuracion.empresa.logoUrl" alt="Logo empresa" class="brand-preview-logo">
                <div>
                  <div class="fw-bold text-white">{{ configuracion.empresa.nombreEmpresa || 'Vista previa de tu empresa' }}</div>
                  <small class="text-white-50">Así se verá tu branding en la plataforma</small>
                </div>
              </div>
              <button type="button" class="btn preview-action-btn"
                      [style.background]="configuracion.empresa.colorPrimario || '#0f766e'"
                      [style.borderColor]="configuracion.empresa.colorPrimario || '#0f766e'"
                      [style.color]="'#ffffff'">
                Acción principal
              </button>
            </div>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold">Razon social</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.razonSocial" name="razonSocial" placeholder="Nombre legal">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">RFC empresa</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.rfc" name="rfcEmpresa" placeholder="AAAA000000XXX">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Email contacto</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.emailContacto" name="emailContacto" placeholder="contacto@empresa.com">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Telefono contacto</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.telefonoContacto" name="telefonoContacto" placeholder="5511223344">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Sitio web</label>
            <input class="form-control" [(ngModel)]="configuracion.empresa.sitioWeb" name="sitioWeb" placeholder="https://...">
          </div>

          <div class="col-12 mt-2">
            <strong>Direccion fiscal / principal</strong>
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">Calle</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.calle" name="calleEmpresa" placeholder="Calle">
          </div>
          <div class="col-md-2">
            <label class="form-label fw-semibold">No. exterior</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.numeroExterior" name="numeroExteriorEmpresa" placeholder="123">
          </div>
          <div class="col-md-2">
            <label class="form-label fw-semibold">No. interior</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.numeroInterior" name="numeroInteriorEmpresa" placeholder="A">
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">Colonia</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.colonia" name="coloniaEmpresa" placeholder="Colonia">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Municipio / Delegacion</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.municipio" name="municipioEmpresa" placeholder="Municipio">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Estado</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.estado" name="estadoEmpresa" placeholder="Estado">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Pais</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.pais" name="paisEmpresa" placeholder="Pais">
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">Codigo postal</label>
            <input class="form-control" [(ngModel)]="empresaDireccion.codigoPostal" name="cpEmpresa" placeholder="01000">
          </div>
        </div>
      </div>

      <div class="card-header bg-white border-0 pb-0">
        <strong>Perfil administrador</strong>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label fw-semibold">Nombre</label>
            <input class="form-control" [(ngModel)]="configuracion.perfil.nombre" name="nombre" readonly>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold">Email</label>
            <input class="form-control" [(ngModel)]="configuracion.perfil.email" name="email" readonly>
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">CURP</label>
            <input class="form-control" [(ngModel)]="configuracion.perfil.curp" name="curp" maxlength="18" placeholder="AAAA000000HXXXXX00">
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">RFC</label>
            <input class="form-control" [(ngModel)]="configuracion.perfil.rfc" name="rfc" maxlength="13" placeholder="AAAA000000XXX">
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">Foto URL</label>
            <input class="form-control" [(ngModel)]="configuracion.perfil.fotoUrl" name="fotoUrl" placeholder="https://...">
          </div>
        </div>
      </div>

      <div class="card-footer bg-white d-flex justify-content-end">
        <button class="btn btn-primary" type="submit" [disabled]="guardandoConfiguracion">
          <i class="fas fa-save me-1"></i>{{ guardandoConfiguracion ? 'Guardando...' : 'Guardar configuracion' }}
        </button>
      </div>
    </form>

    <div class="alert alert-success" *ngIf="exitoMsg">{{ exitoMsg }}</div>
    <div class="alert alert-danger" *ngIf="errorMsg">{{ errorMsg }}</div>
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

    .brand-preview {
      min-height: 90px;
      box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .08);
    }

    .brand-preview-logo {
      width: 52px;
      height: 52px;
      object-fit: contain;
      border-radius: 0;
      background: transparent;
      padding: 0;
    }

    .preview-action-btn {
      font-weight: 700;
      border-width: 1px;
    }
  `]
})
export class EmpresaAdminComponent implements OnInit {
  configuracion: EmpresaConfiguracion = {
    empresa: { direccion: {} },
    perfil: {}
  };

  archivoSeleccionado: File | null = null;
  archivoCredencialesSeleccionado: File | null = null;
  resumenCarga: CargaResumen | null = null;
  resumenCredenciales: CargaCredencialesResumen | null = null;
  resumenAsignacionAutomatica: AsignacionAutomaticaResumen | null = null;
  descargandoPlantilla = false;
  descargandoPlantillaCredenciales = false;
  cargandoEmpleados = false;
  cargandoCredenciales = false;
  generandoLoteCredenciales = false;
  asignandoAutomaticoCredenciales = false;
  loteCantidad = 100;
  loteInicio: number | null = null;
  guardandoConfiguracion = false;
  exitoMsg = '';
  errorMsg = '';

  get empresaDireccion(): NonNullable<EmpresaConfiguracion['empresa']['direccion']> {
    if (!this.configuracion.empresa.direccion) {
      this.configuracion.empresa.direccion = {};
    }
    return this.configuracion.empresa.direccion;
  }

  constructor(
    private authService: AuthService,
    private empresaAdminService: EmpresaAdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.authService.getRol() !== 'ADMIN') {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.cargarConfiguracion();
  }

  cargarConfiguracion(): void {
    this.empresaAdminService.obtenerConfiguracion().subscribe({
      next: res => {
        const datos = res.datos || { empresa: { direccion: {} }, perfil: {} };
        this.configuracion = {
          ...datos,
          empresa: {
            ...(datos.empresa || {}),
            direccion: { ...(datos.empresa?.direccion || {}) }
          },
          perfil: { ...(datos.perfil || {}) }
        };
        this.empresaAdminService.aplicarBranding(this.configuracion);
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible obtener la configuracion';
      }
    });
  }

  descargarPlantilla(): void {
    this.descargandoPlantilla = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.descargarPlantillaEmpleados().subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'plantilla_empleados.csv';
        a.click();
        URL.revokeObjectURL(url);
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible descargar la plantilla';
      },
      complete: () => {
        this.descargandoPlantilla = false;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.archivoSeleccionado = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  onCredencialesFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.archivoCredencialesSeleccionado = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  subirEmpleados(): void {
    if (!this.archivoSeleccionado) {
      return;
    }

    this.cargandoEmpleados = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.cargarEmpleados(this.archivoSeleccionado).subscribe({
      next: res => {
        const datos = (res?.datos || {}) as Partial<CargaResumen>;
        this.resumenCarga = {
          insertados: Number(datos.insertados || 0),
          actualizados: Number(datos.actualizados || 0),
          rechazados: Number(datos.rechazados || 0),
          usuariosGenerados: Number(datos.usuariosGenerados || 0),
          credencialesAsignadas: Number(datos.credencialesAsignadas || 0),
          credencialesGeneradas: Number(datos.credencialesGeneradas || 0)
        };
        this.exitoMsg = res?.mensaje || 'Carga masiva procesada';
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible procesar el CSV';
      },
      complete: () => {
        this.cargandoEmpleados = false;
      }
    });
  }

  descargarPlantillaCredenciales(): void {
    this.descargandoPlantillaCredenciales = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.descargarPlantillaCredenciales().subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'plantilla_credenciales.csv';
        a.click();
        URL.revokeObjectURL(url);
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible descargar la plantilla de credenciales';
      },
      complete: () => {
        this.descargandoPlantillaCredenciales = false;
      }
    });
  }

  subirCredenciales(): void {
    if (!this.archivoCredencialesSeleccionado) {
      return;
    }

    this.cargandoCredenciales = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.cargarCredenciales(this.archivoCredencialesSeleccionado).subscribe({
      next: res => {
        const datos = (res?.datos || {}) as Partial<CargaCredencialesResumen>;
        this.resumenCredenciales = {
          insertados: Number(datos.insertados || 0),
          actualizados: Number(datos.actualizados || 0),
          rechazados: Number(datos.rechazados || 0)
        };
        this.exitoMsg = res?.mensaje || 'Carga de credenciales procesada';
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible procesar el CSV de credenciales';
      },
      complete: () => {
        this.cargandoCredenciales = false;
      }
    });
  }

  generarLoteInicialCredenciales(): void {
    this.generandoLoteCredenciales = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.generarLoteInicialCredenciales(this.loteCantidad, this.loteInicio).subscribe({
      next: res => {
        const datos = res?.datos || {};
        this.exitoMsg = `${res?.mensaje || 'Lote generado'}: ${datos.insertados || 0} insertadas, ${datos.rechazados || 0} rechazadas (rango ${datos.inicio || '-'} - ${datos.fin || '-'})`;
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible generar el lote inicial';
      },
      complete: () => {
        this.generandoLoteCredenciales = false;
      }
    });
  }

  asignarCredencialesAutomaticamente(): void {
    this.asignandoAutomaticoCredenciales = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.asignacionAutomaticaCredenciales(1000).subscribe({
      next: res => {
        const datos = (res?.datos || {}) as Partial<AsignacionAutomaticaResumen>;
        this.resumenAsignacionAutomatica = {
          asignadas: Number(datos.asignadas || 0),
          sinStock: Number(datos.sinStock || 0),
          sinEmpleado: Number(datos.sinEmpleado || 0),
          candidatosEmpleados: Number(datos.candidatosEmpleados || 0),
          stockDisponible: Number(datos.stockDisponible || 0)
        };
        this.exitoMsg = res?.mensaje || 'Asignación automática completada';
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible ejecutar la asignación automática';
      },
      complete: () => {
        this.asignandoAutomaticoCredenciales = false;
      }
    });
  }

  guardarConfiguracion(): void {
    this.guardandoConfiguracion = true;
    this.errorMsg = '';
    this.exitoMsg = '';

    this.empresaAdminService.guardarConfiguracion(this.configuracion).subscribe({
      next: res => {
        this.configuracion = res.datos || this.configuracion;
        this.empresaAdminService.aplicarBranding(this.configuracion);
        this.exitoMsg = res?.mensaje || 'Configuracion guardada';
      },
      error: err => {
        this.errorMsg = err?.error?.mensaje || 'No fue posible guardar la configuracion';
      },
      complete: () => {
        this.guardandoConfiguracion = false;
      }
    });
  }
}
