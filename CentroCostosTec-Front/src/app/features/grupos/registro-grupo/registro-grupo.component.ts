import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { GrupoService } from '../../../services/grupo.service';
import { AuthService } from '../../../services/auth.service';
import { Grupo } from '../../../models/models';

@Component({
  selector: 'app-registro-grupo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
  <div class="p-4">
    <h5 class="mb-4"><i class="fas fa-users-cog me-2 text-primary"></i>{{ editando ? 'Actualizar grupo' : 'Registrar grupo' }}</h5>
    <div class="card shadow-sm border-0">
      <div class="card-header t2-header text-white fw-semibold">
        {{ editando ? 'Actualizar grupo' : 'Registrar grupo' }}
      </div>
      <div class="card-body">
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="d-flex align-items-center justify-content-center mb-3 gap-2">
            <label class="form-check-label mb-0" for="estatusid">Activar grupo</label>
            <input type="checkbox" class="form-check-input" id="estatusid" formControlName="estatusid">
          </div>

          <div class="row g-3">
            <div class="col-md-4">
              <label class="form-label">Grupo ID <span class="text-danger">*</span></label>
              <input
                class="form-control"
                formControlName="grupoid"
                maxlength="15"
                (input)="onGrupoIdInput()"
                placeholder="SOLOMAYUS"
              >
              <div class="text-danger small" *ngIf="isInvalid('grupoid')">Solo letras mayúsculas (máx. 15)</div>
            </div>

            <div class="col-md-8">
              <label class="form-label">Descripción Grupo <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="descripcion">
              <div class="text-danger small" *ngIf="isInvalid('descripcion')">Campo obligatorio</div>
            </div>
          </div>

          <hr>
          <h6 class="section-title mb-3">Dirección de entrega</h6>

          <div class="row g-3">
            <div class="col-md-4">
              <label class="form-label">Calle <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="calle">
              <div class="text-danger small" *ngIf="isInvalid('calle')">Campo obligatorio</div>
            </div>
            <div class="col-md-2">
              <label class="form-label">Número <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="numero">
              <div class="text-danger small" *ngIf="isInvalid('numero')">Campo obligatorio</div>
            </div>
            <div class="col-md-3">
              <label class="form-label">Colonia <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="colonia">
              <div class="text-danger small" *ngIf="isInvalid('colonia')">Campo obligatorio</div>
            </div>
            <div class="col-md-3">
              <label class="form-label">C.P. <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="codigopostal" maxlength="10" (input)="onOnlyDigits('codigopostal', 10)">
              <div class="text-danger small" *ngIf="isInvalid('codigopostal')">Solo números</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Delegación / Municipio <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="delegacion">
              <div class="text-danger small" *ngIf="isInvalid('delegacion')">Campo obligatorio</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Estado <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="estado">
              <div class="text-danger small" *ngIf="isInvalid('estado')">Campo obligatorio</div>
            </div>
          </div>

          <hr>
          <h6 class="section-title mb-3">Contacto</h6>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Nombre del contacto (1) <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="nombre">
              <div class="text-danger small" *ngIf="isInvalid('nombre')">Campo obligatorio</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Teléfono (1) <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="telefono" maxlength="10" (input)="onOnlyDigits('telefono', 10)">
              <div class="text-danger small" *ngIf="isInvalid('telefono')">Solo números, 10 dígitos</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Nombre del contacto (2) <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="nombre2">
              <div class="text-danger small" *ngIf="isInvalid('nombre2')">Campo obligatorio</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Teléfono (2) <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="telefono2" maxlength="10" (input)="onOnlyDigits('telefono2', 10)">
              <div class="text-danger small" *ngIf="isInvalid('telefono2')">Solo números, 10 dígitos</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Horario de Atención <span class="text-danger">*</span></label>
              <input class="form-control" formControlName="horario">
              <div class="text-danger small" *ngIf="isInvalid('horario')">Campo obligatorio</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Observación <span class="text-danger">*</span></label>
              <textarea class="form-control" rows="2" formControlName="observacion"></textarea>
              <div class="text-danger small" *ngIf="isInvalid('observacion')">Campo obligatorio</div>
            </div>
          </div>

          <div class="mb-3">
            <div class="alert alert-danger py-2 mt-3" *ngIf="errorMsg">{{ errorMsg }}</div>
          </div>

          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-primary" [disabled]="loading">
              <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
              {{ editando ? 'Actualizar' : 'Registrar' }}
            </button>
            <button type="button" class="btn btn-outline-secondary" (click)="limpiar()">Limpiar</button>
            <button type="button" class="btn btn-outline-secondary" (click)="router.navigate(['/grupos'])">
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  `
  ,
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
  `]
})
export class RegistroGrupoComponent {
  form: FormGroup;
  loading = false;
  submitted = false;
  errorMsg = '';
  editando = false;
  idDirecciones = 0;

  constructor(private fb: FormBuilder, public router: Router, private route: ActivatedRoute,
              private grupoService: GrupoService, private authService: AuthService) {
    this.form = this.fb.group({
      estatusid: [true],
      grupoid: ['', [Validators.required, Validators.pattern(/^[A-Z]{1,15}$/)]],
      descripcion: ['', Validators.required],
      calle: ['', Validators.required],
      numero: ['', Validators.required],
      colonia: ['', Validators.required],
      codigopostal: ['', [Validators.required, Validators.pattern(/^[0-9]+$/)]],
      delegacion: ['', Validators.required],
      estado: ['', Validators.required],
      nombre: ['', Validators.required],
      telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      nombre2: ['', Validators.required],
      telefono2: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      horario: ['', Validators.required],
      observacion: ['', Validators.required]
    });

    const id = Number(this.route.snapshot.queryParamMap.get('id'));
    if (!Number.isNaN(id) && id > 0) {
      this.editando = true;
      this.idDirecciones = id;
      this.grupoService.listar().subscribe({
        next: res => {
          const g = (res.datos || []).find((item: Grupo) =>
            Number(item.iddirecciones || item.idGrupo || 0) === id
          );
          if (!g) {
            this.errorMsg = 'No fue posible cargar el grupo para edición';
            return;
          }
          this.form.patchValue({
            estatusid: (g.estatus ?? g.activo) ?? true,
            grupoid: g.grupoid || g.nombre || '',
            descripcion: g.descripcion || '',
            calle: g.calle || '',
            numero: g.numero || '',
            colonia: g.colonia || '',
            codigopostal: g.codigopostal || g.codigoPostal || '',
            delegacion: g.delegacion || '',
            estado: g.estado || '',
            nombre: g.nombre || g.nombreContacto1 || '',
            telefono: g.telefono || g.telefono1 || '',
            nombre2: g.nombre2 || g.nombreContacto2 || '',
            telefono2: g.telefono2 || '',
            horario: g.horario || '',
            observacion: g.observacion || ''
          });
        },
        error: err => {
          this.errorMsg = err.error?.mensaje || 'No fue posible cargar el grupo para edición';
        }
      });
    }
  }

  isInvalid(controlName: string): boolean {
    const c = this.form.get(controlName);
    return !!(c && c.invalid && (c.touched || this.submitted));
  }

  onGrupoIdInput(): void {
    const control = this.form.get('grupoid');
    const value = String(control?.value || '').toUpperCase().replace(/[^A-Z]/g, '').slice(0, 15);
    control?.setValue(value, { emitEvent: false });
  }

  onOnlyDigits(controlName: string, maxLen: number): void {
    const control = this.form.get(controlName);
    const value = String(control?.value || '').replace(/\D/g, '').slice(0, maxLen);
    control?.setValue(value, { emitEvent: false });
  }

  limpiar(): void {
    if (this.editando) {
      return;
    }
    this.form.reset({ estatusid: true });
    this.submitted = false;
    this.errorMsg = '';
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) return;
    this.loading = true;
    const user = this.authService.getUser();
    const request = {
      ...this.form.value,
      usuarioAlta: user?.username,
      action: this.editando ? 'Actualizar' : 'Registrar',
      iddirecciones: this.editando ? this.idDirecciones : undefined
    };
    const op$ = this.editando
      ? this.grupoService.actualizar(this.idDirecciones, request)
      : this.grupoService.registrar(request);

    op$.subscribe({
      next: () => this.router.navigate(['/grupos']),
      error: err => {
        this.errorMsg = err.error?.mensaje || (this.editando ? 'Error al actualizar' : 'Error al registrar');
        this.loading = false;
      }
    });
  }
}
