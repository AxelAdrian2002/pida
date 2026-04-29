import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageResponse, Credencial, CredencialBitacora, CredencialOperacionRequest } from '../models/models';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class CredencialService {

  private readonly API = '/api/credenciales';

  constructor(private http: HttpClient, private auth: AuthService) {}

  consultar(estado?: string, idGrupo?: number, numeroEmpleado?: string,
             page = 0, size = 10): Observable<ApiResponse<PageResponse<Credencial>>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (estado)         params = params.set('estado', estado);
    if (idGrupo)        params = params.set('idGrupo', idGrupo);
    if (numeroEmpleado) params = params.set('numeroEmpleado', numeroEmpleado);
    return this.http.get<ApiResponse<PageResponse<Credencial>>>(`${this.API}`, { params });
  }

  obtener(numeroCredencial: string): Observable<ApiResponse<Credencial>> {
    return this.http.get<ApiResponse<Credencial>>(`${this.API}/${numeroCredencial}`);
  }

  bitacora(idCredencial: number): Observable<ApiResponse<CredencialBitacora[]>> {
    return this.http.get<ApiResponse<CredencialBitacora[]>>(`${this.API}/${idCredencial}/bitacora`);
  }

  activar(req: CredencialOperacionRequest): Observable<ApiResponse<Credencial>> {
    return this.http.post<ApiResponse<Credencial>>(`${this.API}/acciones/v1/accion`, this.armarAccion(req, 'Activar'));
  }

  inactivar(req: CredencialOperacionRequest): Observable<ApiResponse<Credencial>> {
    return this.http.post<ApiResponse<Credencial>>(`${this.API}/acciones/v1/accion`, this.armarAccion(req, 'Inactivar'));
  }

  cancelar(req: CredencialOperacionRequest): Observable<ApiResponse<Credencial>> {
    return this.http.post<ApiResponse<Credencial>>(`${this.API}/acciones/v1/accion`, this.armarAccion(req, 'Cancelar'));
  }

  exportarExcel(tipo = 'CREDENCIALES'): Observable<Blob> {
    const payload = JSON.stringify({
      clienteId: this.auth.getClienteId(),
      consignatarioId: this.auth.getConsignatarioId(),
      tipo
    });
    const params = new HttpParams().set('tipo', tipo).set('data', payload);
    return this.http.get(`${this.API}/excel`, { params, responseType: 'blob' });
  }

  cancelacionMasiva(b64: string): Observable<ApiResponse<any>> {
    const form = new FormData();
    form.append('user', JSON.stringify({
      corporativoId: this.auth.getCorporativoId(),
      centroId: this.auth.getCentroId(),
      clienteId: this.auth.getClienteId(),
      consignatarioId: this.auth.getConsignatarioId(),
      idUsuario: this.auth.getIdUsuario()
    }));
    form.append('b64', b64);
    return this.http.post<ApiResponse<any>>(`${this.API}/cancelacion_masiva/v1/cancelacion`, form);
  }

  private armarAccion(req: CredencialOperacionRequest, action: 'Activar' | 'Inactivar' | 'Cancelar'): any {
    return {
      action,
      tarjetaid: req.numeroCredencial,
      usuarioId: req.idUsuario || this.auth.getIdUsuario() || 0,
      bitacoraId: req.bitacoraId ?? -1,
      usuarioOperacion: req.usuarioOperacion || this.auth.getUser()?.username || '',
      observacion: req.observacion,
      clienteid: req.clienteId ?? this.auth.getClienteId() ?? undefined,
      consignatarioid: req.consignatarioId ?? this.auth.getConsignatarioId() ?? undefined
    };
  }
}


