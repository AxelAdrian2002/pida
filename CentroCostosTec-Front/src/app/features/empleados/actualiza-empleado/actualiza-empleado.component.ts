import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmpleadoService } from '../../../services/empleado.service';
import { AuthService } from '../../../services/auth.service';
import { UiLoaderService } from '../../../services/ui-loader.service';
import { Empleado } from '../../../models/models';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-actualiza-empleado',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
  <!-- Overlay loader mientras se cargan los datos del empleado -->
  <div class="efv-loader-overlay" *ngIf="loadingData">
    <div class="text-center">
      <div class="spinner-border text-light" style="width:3rem;height:3rem;"></div>
      <p class="text-white mt-3 fw-semibold">Cargando datos...</p>
    </div>
  </div>

  <div class="p-4">
    <h5 class="mb-4">
      <i class="fas fa-user-edit me-2 text-primary"></i>Actualizar Empleado
    </h5>

    <div class="card shadow-sm border-0">

      <!-- Encabezado -->
      <div class="card-header t2-header text-white fw-semibold d-flex align-items-center gap-2">
        <i class="fas fa-id-card me-1"></i>
        Actualizar datos del empleado
      </div>

      <div class="card-body">
        <form [formGroup]="form" (ngSubmit)="onSubmit()">

          <!-- Sección: Identificación -->
          <h6 class="section-title mb-3">
            <i class="fas fa-fingerprint me-1"></i> Identificación
          </h6>
          <div class="row g-3 mb-3">
            <div class="col-md-4">
              <label class="form-label">N° Empleado</label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-hashtag"></i></span>
                <input class="form-control" [value]="numeroEmpleado" readonly>
              </div>
            </div>
            <div class="col-md-8">
              <label class="form-label">Nombre completo <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-user"></i></span>
                <input
                  type="text"
                  class="form-control"
                  formControlName="nombre"
                  maxlength="30"
                  placeholder="Nombre completo"
                  (input)="onInputText('nombre', 30)"
                  [class.is-invalid]="isInvalid('nombre')">
                <div class="invalid-feedback">Nombre es requerido (mín. 3 caracteres).</div>
              </div>
            </div>
          </div>

          <hr>

          <!-- Sección: Datos laborales -->
          <h6 class="section-title mb-3">
            <i class="fas fa-briefcase me-1"></i> Datos laborales
          </h6>
          <div class="row g-3 mb-3">
            <div class="col-md-12">
              <label class="form-label">Puesto <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-tag"></i></span>
                <input
                  type="text"
                  class="form-control"
                  formControlName="puesto"
                  maxlength="30"
                  placeholder="Puesto o cargo"
                  (input)="onInputText('puesto', 30)"
                  [class.is-invalid]="isInvalid('puesto')">
                <div class="invalid-feedback">Puesto es requerido (mín. 3 caracteres).</div>
              </div>
            </div>
          </div>

          <hr>

          <!-- Sección: Contacto -->
          <h6 class="section-title mb-3">
            <i class="fas fa-address-book me-1"></i> Contacto
          </h6>
          <div class="row g-3 mb-3">
            <div class="col-md-12">
              <label class="form-label">Correo electrónico <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-envelope"></i></span>
                <input
                  type="email"
                  class="form-control"
                  formControlName="correo"
                  maxlength="60"
                  placeholder="correo@dominio.com"
                  (input)="onInputCorreo('correo', 60)"
                  [class.is-invalid]="isInvalid('correo')">
                <div class="invalid-feedback">Formato de correo electrónico inválido.</div>
              </div>
            </div>

            <div class="col-md-5">
              <label class="form-label">Teléfono fijo <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-phone"></i></span>
                <input
                  type="text"
                  class="form-control"
                  formControlName="telefonoFijo"
                  maxlength="10"
                  placeholder="10 dígitos"
                  (input)="onOnlyDigits('telefonoFijo', 10)"
                  [class.is-invalid]="isInvalid('telefonoFijo')">
                <div class="invalid-feedback">Teléfono fijo es requerido.</div>
              </div>
            </div>

            <div class="col-md-3">
              <label class="form-label">Extensión</label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-plug"></i></span>
                <input
                  type="text"
                  class="form-control"
                  formControlName="extension"
                  maxlength="5"
                  placeholder="Ext."
                  (input)="onOnlyDigits('extension', 5)">
              </div>
            </div>

            <div class="col-md-4">
              <label class="form-label">Teléfono móvil <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text"><i class="fas fa-mobile-alt"></i></span>
                <input
                  type="text"
                  class="form-control"
                  formControlName="telefonoMovil"
                  maxlength="10"
                  placeholder="10 dígitos"
                  (input)="onOnlyDigits('telefonoMovil', 10)"
                  [class.is-invalid]="isInvalid('telefonoMovil')">
                <div class="invalid-feedback">Teléfono móvil es requerido.</div>
              </div>
            </div>
          </div>

          <!-- Alerta de error en línea -->
          <div class="alert alert-danger d-flex align-items-center gap-2 mt-2" *ngIf="errorMsg">
            <i class="fas fa-exclamation-circle"></i> {{ errorMsg }}
          </div>

          <!-- Botones -->
          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary" [disabled]="loading">
              <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
              <i *ngIf="!loading" class="fas fa-save me-1"></i>
              Actualizar
            </button>
            <button type="button" class="btn btn-outline-secondary" (click)="limpiar()">
              <i class="fas fa-undo me-1"></i>Limpiar
            </button>
            <button type="button" class="btn btn-outline-secondary" (click)="router.navigate(['/empleados'])">
              <i class="fas fa-arrow-left me-1"></i>Cancelar
            </button>
          </div>

        </form>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .t2-header {
      background: var(--efv_rojo) !important;
    }

    .section-title {
      background: var(--efv_rojo);
      color: #fff;
      padding: .45rem .65rem;
      border-radius: .35rem;
      font-weight: 600;
      font-size: 1rem;
    }

    .efv-loader-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.55);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }
  `]
})
export class ActualizaEmpleadoComponent implements OnInit {
  form: FormGroup;
  numeroEmpleado = '';
  loading = false;
  loadingData = false;
  errorMsg = '';
  submitted = false;
  empleadoActual: Empleado | null = null;

  constructor(private fb: FormBuilder, private route: ActivatedRoute,
              public router: Router, private empleadoService: EmpleadoService,
              private authService: AuthService, private uiLoader: UiLoaderService) {
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      correo: ['', [Validators.required, Validators.email]],
      puesto: ['', [Validators.required, Validators.minLength(3)]],
      extension: [''],
      telefonoFijo: ['', [Validators.required, Validators.minLength(1)]],
      telefonoMovil: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  ngOnInit(): void {
    this.numeroEmpleado = this.route.snapshot.paramMap.get('numeroEmpleado') ?? '';
    this.loadingData = true;
    this.uiLoader.show();
    this.empleadoService.obtener(this.numeroEmpleado).subscribe({
      next: res => {
        this.empleadoActual = res.datos;
        this.form.patchValue({
          nombre: res.datos.nombre || '',
          correo: res.datos.email || '',
          puesto: res.datos.apellidoPaterno || '',
          extension: '',
          telefonoFijo: res.datos.telefono || '',
          telefonoMovil: res.datos.telefono || ''
        });
        this.loadingData = false;
        this.uiLoader.hide();
      },
      error: () => {
        this.loadingData = false;
        this.uiLoader.hide();
        this.errorMsg = 'No fue posible cargar los datos del empleado.';
      }
    });
  }

  isInvalid(controlName: string): boolean {
    const c = this.form.get(controlName);
    return !!(c && c.invalid && (c.touched || this.submitted));
  }

  onInputText(controlName: string, maxLen: number): void {
    const control = this.form.get(controlName);
    const value = String(control?.value || '').replace(/\s+/g, ' ').slice(0, maxLen);
    control?.setValue(value, { emitEvent: false });
  }

  onInputCorreo(controlName: string, maxLen: number): void {
    const control = this.form.get(controlName);
    const value = String(control?.value || '').replace(/\s/g, '').slice(0, maxLen);
    control?.setValue(value, { emitEvent: false });
  }

  onOnlyDigits(controlName: string, maxLen: number): void {
    const control = this.form.get(controlName);
    const value = String(control?.value || '').replace(/\D/g, '').slice(0, maxLen);
    control?.setValue(value, { emitEvent: false });
  }

  limpiar(): void {
    if (!this.empleadoActual) {
      this.form.reset();
      return;
    }

    this.form.patchValue({
      nombre: this.empleadoActual.nombre || '',
      correo: this.empleadoActual.email || '',
      puesto: this.empleadoActual.apellidoPaterno || '',
      extension: '',
      telefonoFijo: this.empleadoActual.telefono || '',
      telefonoMovil: this.empleadoActual.telefono || ''
    });
    this.submitted = false;
    this.errorMsg = '';
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true; this.errorMsg = '';
    const user = this.authService.getUser();
    const telefono = this.form.value.telefonoMovil || this.form.value.telefonoFijo;

    this.empleadoService.actualizar(this.numeroEmpleado, {
      numeroEmpleado: this.numeroEmpleado,
      nombre: this.form.value.nombre,
      apellidoPaterno: this.empleadoActual?.apellidoPaterno || 'N/A',
      apellidoMaterno: this.empleadoActual?.apellidoMaterno,
      email: this.form.value.correo,
      telefono,
      usuarioModificacion: user?.username,
      puesto: this.form.value.puesto,
      extension: this.form.value.extension,
      telefonoMovil: this.form.value.telefonoMovil,
      telefonoFijo: this.form.value.telefonoFijo
    }).subscribe({
      next: () => {
        this.loading = false;
        void Swal.fire({
          icon: 'success',
          title: 'Actualización exitosa',
          text: 'Los datos del empleado fueron actualizados correctamente.',
          confirmButtonText: 'Aceptar'
        }).then(() => {
          void this.router.navigate(['/empleados']);
        });
      },
      error: err => {
        this.errorMsg = err.error?.mensaje || 'Error al actualizar';
        this.loading = false;
      }
    });
  }
}

