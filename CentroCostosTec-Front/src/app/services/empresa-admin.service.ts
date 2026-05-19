import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, EmpresaConfiguracion } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EmpresaAdminService {
  private readonly API = '/api/empresas/v1';
  private readonly BRANDING_KEY = 'cc_branding';

  constructor(private http: HttpClient) {}

  descargarPlantillaEmpleados(): Observable<Blob> {
    return this.http.get(`${this.API}/plantilla-empleados`, { responseType: 'blob' });
  }

  cargarEmpleados(file: File): Observable<ApiResponse<any>> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<any>>(`${this.API}/cargar-empleados`, form);
  }

  descargarPlantillaCredenciales(): Observable<Blob> {
    return this.http.get(`${this.API}/plantilla-credenciales`, { responseType: 'blob' });
  }

  cargarCredenciales(file: File): Observable<ApiResponse<any>> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<any>>(`${this.API}/cargar-credenciales`, form);
  }

  generarLoteInicialCredenciales(cantidad: number, inicio?: number | null): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/credenciales/lote-inicial`, {
      cantidad,
      inicio: inicio ?? null
    });
  }

  asignacionAutomaticaCredenciales(limite = 100): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/credenciales/asignacion-automatica`, {
      limite
    });
  }

  obtenerConfiguracion(): Observable<ApiResponse<EmpresaConfiguracion>> {
    return this.http.get<ApiResponse<EmpresaConfiguracion>>(`${this.API}/configuracion`);
  }

  guardarConfiguracion(payload: EmpresaConfiguracion): Observable<ApiResponse<EmpresaConfiguracion>> {
    return this.http.put<ApiResponse<EmpresaConfiguracion>>(`${this.API}/configuracion`, payload);
  }

  aplicarBranding(configuracion?: EmpresaConfiguracion | null): void {
    const empresa = configuracion?.empresa;
    const root = document.documentElement;

    const colorPrimario = empresa?.colorPrimario?.trim() || '#0f766e';
    const colorSecundario = empresa?.colorSecundario?.trim() || '#334155';
    const logoUrl = empresa?.logoUrl?.trim() || '';
    const nombreEmpresa = empresa?.nombreEmpresa?.trim() || 'Plataforma Interna';

    root.style.setProperty('--brand-primary', colorPrimario);
    root.style.setProperty('--brand-secondary', colorSecundario);
    root.style.setProperty('--efv_rojo', colorPrimario);
    root.style.setProperty('--efv_gris', colorSecundario);
    root.style.setProperty('--efv_gris_oscuro', colorSecundario);

    localStorage.setItem(this.BRANDING_KEY, JSON.stringify({
      colorPrimario,
      colorSecundario,
      logoUrl,
      nombreEmpresa
    }));
  }

  aplicarBrandingGuardado(): { nombreEmpresa: string; logoUrl: string } | null {
    const raw = localStorage.getItem(this.BRANDING_KEY);
    if (!raw) {
      return null;
    }

    try {
      const branding = JSON.parse(raw);
      this.aplicarBranding({
        empresa: {
          colorPrimario: branding.colorPrimario,
          colorSecundario: branding.colorSecundario,
          logoUrl: branding.logoUrl,
          nombreEmpresa: branding.nombreEmpresa
        },
        perfil: {}
      });
      return {
        nombreEmpresa: branding.nombreEmpresa || 'Plataforma Interna',
        logoUrl: branding.logoUrl || ''
      };
    } catch {
      localStorage.removeItem(this.BRANDING_KEY);
      return null;
    }
  }
}
