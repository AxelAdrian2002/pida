import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiResponse, LoginRequest, LoginResponse } from '../models/models';

/**
 * Servicio de autenticación.
 * Protocolo JWT Bearer estándar. Storage: localStorage.
 * La respuesta incluye clienteId, consignatarioId, corporativoId, centroId.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly API        = '/api/Login/login/v1';
  private readonly TOKEN_KEY  = 'cc_token';
  private readonly USER_KEY   = 'cc_user';

  constructor(private http: HttpClient) {}

  login(dto: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.API}/login`, dto).pipe(
      tap(res => {
        const token = res.datos?.token || res.datos?.accessToken;
        if (token) {
          sessionStorage.setItem(this.TOKEN_KEY, token);
          sessionStorage.setItem(this.USER_KEY, JSON.stringify(res.datos));
          localStorage.setItem(this.TOKEN_KEY, token);
          localStorage.setItem(this.USER_KEY, JSON.stringify(res.datos));
        }
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY) || localStorage.getItem(this.TOKEN_KEY);
  }

  getUser(): LoginResponse | null {
    const raw = sessionStorage.getItem(this.USER_KEY) || localStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRol(): string | null {
    const u = this.getUser() as any;
    const rolRaw = String(u?.rol ?? '').trim().toUpperCase();
    if (rolRaw) {
      if (rolRaw.startsWith('ADMIN')) return 'ADMIN';
      if (rolRaw.startsWith('CAPTURA') || rolRaw.startsWith('CAPTUR')) return 'CAPTURA';
      if (rolRaw.startsWith('AUTORIZ')) return 'AUTORIZADOR';
      if (rolRaw.startsWith('CONSULT')) return 'CONSULTA';
    }

    const perfilId = Number(u?.perfilId ?? u?.perfilid ?? 0);
    return (perfilId === 1 || perfilId === 4) ? 'ADMIN'
      : perfilId === 2 ? 'CAPTURA'
      : perfilId === 5 ? 'AUTORIZADOR'
      : 'CONSULTA';
  }

  /** clienteid de centrocostos (dbdespensa) - necesario para las llamadas de negocio */
  getClienteId(): number | null {
    const u = this.getUser() as any;
    return u?.clienteId ?? u?.clienteid ?? null;
  }

  /** consignatarioid de centrocostos (dbdespensa) - necesario para las llamadas de negocio */
  getConsignatarioId(): number | null {
    const u = this.getUser() as any;
    return u?.consignatarioId ?? u?.consignatarioid ?? null;
  }

  getIdUsuario(): number | null {
    return this.getUser()?.idUsuario ?? null;
  }

  getCorporativoId(): string | null {
    return this.getUser()?.corporativoId ?? null;
  }

  getCentroId(): string | null {
    return this.getUser()?.centroId ?? null;
  }

  getJerarquia(): string {
    const u = this.getUser() as any;
    return String(u?.jerarquia ?? u?.rs_jerarquia_or ?? u?.rs_jerarquia ?? 'OR');
  }
}


