import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Empleado, EmpleadoRequest, PageResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EmpleadoService {

  private readonly API = '/api/Actualizar_Datos/datosEmpleado/v1/datosEmpleado';

  constructor(private http: HttpClient) {}

  listar(nombre?: string, departamento?: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<Empleado>>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (nombre) params = params.set('nombre', nombre);
    if (departamento) params = params.set('numeroEmpleado', departamento);
    return this.http.get<ApiResponse<PageResponse<Empleado>>>(`${this.API}/getDataExcel/`, { params });
  }

  exportarExcel(nombre?: string, numeroEmpleado?: string): Observable<Blob> {
    let params = new HttpParams();
    if (nombre) params = params.set('nombre', nombre);
    if (numeroEmpleado) params = params.set('numeroEmpleado', numeroEmpleado);
    return this.http.get(`${this.API}/getExcel`, { params, responseType: 'blob' });
  }

  obtenerCentroCostos(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.API}/centrocostos/`);
  }

  procesarExcel(b64: string): Observable<ApiResponse<any>> {
    const form = new FormData();
    form.append('user', '-user');
    form.append('b64', b64);
    return this.http.post<ApiResponse<any>>(`${this.API}/procesarExcel/`, form);
  }

  obtener(numeroEmpleado: string): Observable<ApiResponse<Empleado>> {
    return this.http.get<ApiResponse<Empleado>>(`${this.API}/${numeroEmpleado}`);
  }

  alta(req: EmpleadoRequest): Observable<ApiResponse<Empleado>> {
    return this.http.post<ApiResponse<Empleado>>(this.API, req);
  }

  actualizar(numeroEmpleado: string, req: EmpleadoRequest): Observable<ApiResponse<Empleado>> {
    return this.http.put<ApiResponse<Empleado>>(`${this.API}/${numeroEmpleado}`, req);
  }
}

