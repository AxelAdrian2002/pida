import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CambioPasswordService } from '../../../services/cambio-password.service';

@Component({
  selector: 'app-cambiar-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container-fluid py-4">
      <div class="row justify-content-center">
        <div class="col-lg-6 col-xl-5">
          <div class="card shadow-sm border-0">
            <div class="card-header bg-white">
              <h5 class="mb-0 text-primary"><i class="fas fa-key me-2"></i>Cambiar Contraseña</h5>
            </div>
            <div class="card-body">
              <div *ngIf="mensaje" class="alert" [class.alert-success]="!esError" [class.alert-danger]="esError">
                {{ mensaje }}
              </div>

              <form [formGroup]="form" (ngSubmit)="guardar()" novalidate>
                <div class="mb-3">
                  <label class="form-label">Contraseña Actual</label>
                  <input type="password" class="form-control" formControlName="passwordanterior" autocomplete="current-password">
                </div>

                <div class="mb-3">
                  <label class="form-label">Nueva Contraseña</label>
                  <input type="password" class="form-control" formControlName="nuevoPassword" autocomplete="new-password">
                  <small class="text-muted">Mínimo 6 caracteres.</small>
                </div>

                <div class="mb-3">
                  <label class="form-label">Confirmar Nueva Contraseña</label>
                  <input type="password" class="form-control" formControlName="confirmacion" autocomplete="new-password">
                </div>

                <div class="d-flex gap-2">
                  <button class="btn btn-primary" type="submit" [disabled]="guardando">
                    <span *ngIf="!guardando">Guardar</span>
                    <span *ngIf="guardando">Guardando...</span>
                  </button>
                  <button class="btn btn-outline-secondary" type="button" (click)="regresar()">Cancelar</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class CambiarPasswordComponent {
  guardando = false;
  mensaje = '';
  esError = false;

  form = this.fb.group({
    passwordanterior: ['', [Validators.required]],
    nuevoPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmacion: ['', [Validators.required]]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private cambioPasswordService: CambioPasswordService,
    private router: Router
  ) {}

  guardar(): void {
    this.mensaje = '';
    this.esError = false;

    if (this.form.invalid) {
      this.mostrarError('Completa todos los campos correctamente.');
      this.form.markAllAsTouched();
      return;
    }

    const passwordActual = String(this.form.value.passwordanterior || '');
    const passwordNueva = String(this.form.value.nuevoPassword || '');
    const confirmacion = String(this.form.value.confirmacion || '');

    if (passwordNueva !== confirmacion) {
      this.mostrarError('La confirmación no coincide con la nueva contraseña.');
      return;
    }

    const idUsuario = this.authService.getIdUsuario();
    if (!idUsuario) {
      this.mostrarError('No se identificó al usuario actual.');
      return;
    }

    this.guardando = true;
    this.cambioPasswordService.cambiarPassword({
      id: idUsuario,
      passwordanterior: passwordActual,
      nuevoPassword: passwordNueva
    }).subscribe({
      next: (res) => {
        this.guardando = false;
        this.esError = false;
        this.mensaje = res?.mensaje || 'Contraseña actualizada correctamente.';
        this.form.reset();
      },
      error: (err) => {
        this.guardando = false;
        const backendMessage = err?.error?.mensaje;
        this.mostrarError(backendMessage || 'No fue posible actualizar la contraseña.');
      }
    });
  }

  regresar(): void {
    this.router.navigate(['/dashboard']);
  }

  private mostrarError(texto: string): void {
    this.esError = true;
    this.mensaje = texto;
  }
}

