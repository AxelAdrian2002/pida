import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Grupo, GrupoEmpleado, GrupoRequest } from '../models/models';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class GrupoService {

  private readonly API = '/api/Administracion_de_grupos';

  constructor(private http: HttpClient, private auth: AuthService) {}

  listar(): Observable<ApiResponse<Grupo[]>> {
    return this.http.get<ApiResponse<Grupo[]>>(`${this.API}/Registrar_grupos/v1/reg_grupo/table/`);
  }

  obtener(id: number): Observable<ApiResponse<Grupo>> {
    return this.http.get<ApiResponse<Grupo>>(`${this.API}/${id}`);
  }

  registrar(req: GrupoRequest): Observable<ApiResponse<Grupo>> {
    return this.http.post<ApiResponse<Grupo>>(
      `${this.API}/Registrar_grupos/v1/reg_grupo/modificar/`,
      this.normalizarRegistro(req, 'Registrar')
    );
  }

  actualizar(idDirecciones: number, req: GrupoRequest): Observable<ApiResponse<Grupo>> {
    return this.http.put<ApiResponse<Grupo>>(
      `${this.API}/Registrar_grupos/v1/reg_grupo/modificar/`,
      this.normalizarRegistro(req, 'Actualizar', idDirecciones)
    );
  }

  asignarEmpleado(idGrupo: number, idEmpleado: number,
                   numeroEmpleado: string, usuarioAsigno: string): Observable<ApiResponse<GrupoEmpleado>> {
    return this.http.post<ApiResponse<GrupoEmpleado>>(
      `${this.API}/Asignar_grupos/v1/asig_grupo/asignar/`,
      {
        idGrupo,
        idEmpleado,
        numeroEmpleado,
        usuarioAsigno: usuarioAsigno || this.auth.getUser()?.username || ''
      }
    );
  }

  reporte(idGrupo: number): Observable<ApiResponse<GrupoEmpleado[]>> {
    return this.http.get<ApiResponse<GrupoEmpleado[]>>(`${this.API}/Reporte_grupos/v1/reporte_grupo/getListChildCC/`, {
      params: { idGrupo }
    });
  }

  exportarExcelGrupos(): Observable<Blob> {
    return this.http.get(`${this.API}/Registrar_grupos/v1/reg_grupo/getExcel`, { responseType: 'blob' });
  }

  exportarExcelPlantilla(tipo: string): Observable<Blob> {
    const params = new HttpParams().set('tipo', tipo);
    return this.http.get(`${this.API}/Asignar_grupos/v1/asig_grupo/getExcel`, { params, responseType: 'blob' });
  }

  procesarExcelAsignacion(b64: string): Observable<ApiResponse<any>> {
    const form = new FormData();
    form.append('user', '-user');
    form.append('b64', b64);
    return this.http.post<ApiResponse<any>>(`${this.API}/Asignar_grupos/v1/asig_grupo/procesarExcel`, form);
  }

  exportarExcelReporte(idGrupo: number): Observable<Blob> {
    const params = new HttpParams().set('idGrupo', idGrupo);
    return this.http.get(`${this.API}/Reporte_grupos/v1/reporte_grupo/getExcel`, { params, responseType: 'blob' });
  }

  private enriquecerContexto(req: GrupoRequest): GrupoRequest {
    return {
      ...req,
      clienteId:       req.clienteId       ?? this.auth.getClienteId()       ?? undefined,
      consignatarioId: req.consignatarioId ?? this.auth.getConsignatarioId() ?? undefined,
      usuarioAlta:     req.usuarioAlta     ?? this.auth.getUser()?.username  ?? ''
    };
  }

  private normalizarRegistro(req: GrupoRequest, action: 'Registrar' | 'Actualizar', idDirecciones?: number): GrupoRequest {
    const base = this.enriquecerContexto(req) as GrupoRequest & {
      grupoid?: string;
      action?: string;
      iddirecciones?: number;
    };

    const grupoId = (base.grupoid || '').toString().trim().toUpperCase();

    return {
      ...base,
      action,
      grupoid: grupoId,
      iddirecciones: action === 'Actualizar' ? idDirecciones : undefined
    } as GrupoRequest;
  }
}


