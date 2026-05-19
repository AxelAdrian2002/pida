import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ReportesService {

  private readonly API = '/api/reportes';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene el resumen rápido de solicitudes (últimos 30 días)
   */
  obtenerResumen(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.API}/solicitudes/resumen`);
  }

  /**
   * Obtiene auditoría por rango de fechas
   */
  obtenerAuditoria(fechaInicio: string, fechaFin: string): Observable<ApiResponse<any>> {
    let params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ApiResponse<any>>(`${this.API}/auditoria`, { params });
  }

  /**
   * Obtiene historial de cambios de una solicitud
   */
  obtenerHistorialSolicitud(solicitudId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.API}/solicitudes/${solicitudId}/historial`);
  }

  /**
   * Filtra auditoría con criterios específicos
   */
  filtrarAuditoria(filtros: {
    fechaInicio?: string;
    fechaFin?: string;
    accion?: string;
    tipoSolicitud?: string;
  }): Observable<ApiResponse<any[]>> {
    return this.http.post<ApiResponse<any[]>>(`${this.API}/auditoria/filtro`, filtros);
  }
}
