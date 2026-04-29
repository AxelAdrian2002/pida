import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/models';

export interface CambioPasswordRequest {
  id: number;
  passwordanterior: string;
  nuevoPassword: string;
}

@Injectable({ providedIn: 'root' })
export class CambioPasswordService {
  private readonly API = '/api/Administracion_Login/cambio_contrasenia/v1/cambio_contrasenia/';

  constructor(private http: HttpClient) {}

  cambiarPassword(dto: CambioPasswordRequest): Observable<ApiResponse<{ actualizado: boolean; mensaje: string }>> {
    return this.http.put<ApiResponse<{ actualizado: boolean; mensaje: string }>>(this.API, dto);
  }
}
