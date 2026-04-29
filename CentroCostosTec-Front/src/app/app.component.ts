import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { UiLoaderService } from './services/ui-loader.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <router-outlet />

    <div class="global-loader-overlay" *ngIf="loader.loading$ | async">
      <div class="global-loader-content">
        <div class="spinner-border text-light" role="status" aria-label="Cargando"></div>
        <div class="mt-2 text-light fw-semibold">Cargando...</div>
      </div>
    </div>
  `,
  styles: [`
    .global-loader-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      z-index: 2000;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(1px);
    }

    .global-loader-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-width: 140px;
      padding: 14px 18px;
      border-radius: 10px;
      background: rgba(0, 0, 0, 0.25);
    }
  `]
})
export class AppComponent {
  constructor(public loader: UiLoaderService) {}
}
