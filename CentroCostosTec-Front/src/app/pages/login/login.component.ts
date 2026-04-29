import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
  <div class="login-container d-flex justify-content-center align-items-center vh-100 bg-light">
    <div class="card shadow p-4" style="width: 420px;">
      <div class="text-center mb-4">
        <h4 class="fw-bold text-primary">Plataforma Interna</h4>
        <small class="text-muted">Gestión Operativa</small>
      </div>
      <form [formGroup]="loginForm" (ngSubmit)="onLogin()">

        <div class="mb-3">
          <label class="form-label">Corporativo <span class="text-danger">*</span></label>
          <input type="text" class="form-control" formControlName="corporativo"
                 placeholder="Corporativo" autocomplete="organization">
          <div class="text-danger small" *ngIf="loginForm.get('corporativo')?.invalid && submitted">
            El corporativo es obligatorio
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">Unidad Operativa <span class="text-danger">*</span></label>
          <input type="text" class="form-control" formControlName="centrocostos"
                 placeholder="Unidad operativa" autocomplete="off">
          <div class="text-danger small" *ngIf="loginForm.get('centrocostos')?.invalid && submitted">
            La unidad operativa es obligatoria
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">Usuario <span class="text-danger">*</span></label>
          <input type="text" class="form-control" formControlName="username"
                 placeholder="Usuario" autocomplete="username">
          <div class="text-danger small" *ngIf="loginForm.get('username')?.invalid && submitted">
            El usuario es obligatorio
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label">Contraseña <span class="text-danger">*</span></label>
          <input type="password" class="form-control" formControlName="password"
                 placeholder="Contraseña" autocomplete="current-password">
          <div class="text-danger small" *ngIf="loginForm.get('password')?.invalid && submitted">
            La contraseña es obligatoria
          </div>
        </div>

        <div class="alert alert-danger py-2" *ngIf="errorMsg">{{ errorMsg }}</div>

        <button type="submit" class="btn btn-primary w-100" [disabled]="loading">
          <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
          Iniciar sesión
        </button>
      </form>
    </div>
  </div>
  `
})
export class LoginComponent {
  loginForm: FormGroup;
  loading  = false;
  submitted = false;
  errorMsg  = '';
  /** Contador de intentos fallidos (igual que  ModelLogin.intentos) */
  private intentos = 0;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      corporativo:  ['', Validators.required],
      centrocostos: ['', Validators.required],
      username:     ['', Validators.required],
      password:     ['', Validators.required]
    });
  }

  onLogin(): void {
    this.submitted = true;
    this.errorMsg  = '';
    if (this.loginForm.invalid) return;
    this.loading = true;

    const payload = {
      ...this.loginForm.value,
      intentos: this.intentos
    };

    this.authService.login(payload).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.intentos++;
        const msg = err?.error?.mensaje ?? err?.error?.message ?? 'Usuario o contraseña incorrectos';
        this.errorMsg = this.intentos >= 3
          ? 'Acceso bloqueado por exceder el número de intentos'
          : msg;
        this.loading = false;
      }
    });
  }
}


