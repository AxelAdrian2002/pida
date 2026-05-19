import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  DataTableRequest,
  DataTableResponse,
  FillSelectOption,
  PageResponse,
  Solicitud,
  SolicitudPreview,
  SolicitudRequest
} from '../models/models';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class SolicitudService {

  private readonly API = '/api/solicitudes';

  constructor(private http: HttpClient, private auth: AuthService) {}

  listar(estado?: string, page = 0, size = 10): Observable<ApiResponse<PageResponse<Solicitud>>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (estado) params = params.set('estado', estado);
    return this.http.get<ApiResponse<PageResponse<Solicitud>>>(this.API, { params });
  }

  obtener(id: number): Observable<ApiResponse<Solicitud>> {
    return this.http.get<ApiResponse<Solicitud>>(`${this.API}/${id}`);
  }

  private tipoPath(tipo: 'dispersion' | 'asignacion' | 'tarjeta' | 'adicional' | 'stock'): string {
    switch (tipo) {
      case 'dispersion': return 'apoyo-economico';
      case 'asignacion': return 'nueva-asignacion';
      case 'tarjeta': return 'nueva-asignacion';
      case 'adicional': return 'asignacion-adicional';
      case 'stock': return 'reposicion';
      default: return 'apoyo-economico';
    }
  }

  getPlantilla(tipo: 'dispersion' | 'asignacion' | 'tarjeta' | 'adicional' | 'stock'): Observable<Blob> {
    return this.http.get(`${this.API}/${this.tipoPath(tipo)}/plantilla`, {
      params: new HttpParams().set('data', this.contexto()),
      responseType: 'blob'
    });
  }

  procesaPedido(tipo: 'dispersion' | 'asignacion' | 'tarjeta' | 'adicional', b64: string, promociones?: any): Observable<ApiResponse<SolicitudPreview>> {
    const form = new FormData();
    form.append('user', this.contexto());
    form.append('b64', b64);
    if (promociones != null) {
      form.append('promociones', JSON.stringify(promociones));
    }
    return this.http.post<ApiResponse<SolicitudPreview>>(`${this.API}/${this.tipoPath(tipo)}/procesar`, form);
  }

  procesaPedidoStock(data: string): Observable<ApiResponse<any>> {
    const form = new FormData();
    form.append('data', JSON.stringify({
      numeroTarjetas: data,
      clienteId: this.auth.getClienteId(),
      consignatarioId: this.auth.getConsignatarioId(),
      corporativoId: this.auth.getCorporativoId(),
      centroId: this.auth.getCentroId(),
      idUsuario: this.auth.getIdUsuario()
    }));
    return this.http.post<ApiResponse<any>>(`${this.API}/${this.tipoPath('stock')}/procesar`, form);
  }

  guardarSolicitud(tipo: 'dispersion' | 'asignacion' | 'tarjeta' | 'adicional' | 'stock', data: any): Observable<ApiResponse<Solicitud>> {
    const form = new FormData();
    form.append('data', JSON.stringify(data));
    form.append('userId', String(this.auth.getIdUsuario() ?? 0));
    return this.http.post<ApiResponse<Solicitud>>(`${this.API}/${this.tipoPath(tipo)}`, form);
  }

  crearDispersion(req: SolicitudRequest): Observable<ApiResponse<Solicitud>> {
    return this.guardarSolicitud('dispersion', this.enriquecerContexto(req));
  }

  crearStock(req: SolicitudRequest): Observable<ApiResponse<Solicitud>> {
    return this.guardarSolicitud('stock', this.enriquecerContexto(req));
  }

  crearAsignacion(req: SolicitudRequest): Observable<ApiResponse<Solicitud>> {
    return this.guardarSolicitud('asignacion', this.enriquecerContexto(req));
  }

  crearTarjeta(req: SolicitudRequest): Observable<ApiResponse<Solicitud>> {
    return this.crearAsignacion(req);
  }

  crearAdicional(req: SolicitudRequest): Observable<ApiResponse<Solicitud>> {
    return this.guardarSolicitud('adicional', this.enriquecerContexto(req));
  }

  guardarPedido(tipo: 'dispersion' | 'asignacion' | 'tarjeta' | 'adicional' | 'stock', data: any): Observable<ApiResponse<Solicitud>> {
    return this.guardarSolicitud(tipo, data);
  }

  getFillSelect(): Observable<ApiResponse<FillSelectOption[]>> {
    return this.http.get<ApiResponse<FillSelectOption[]>>(`${this.API}/aprobacion/catalogos`, {
      params: new HttpParams().set('data', JSON.stringify({
        corporativoId: this.auth.getCorporativoId(),
        jerarquia: this.auth.getJerarquia()
      }))
    });
  }

  getDataTable(req: DataTableRequest, centroId?: string): Observable<ApiResponse<DataTableResponse>> {
    const payload = {
      ...req,
      params: {
        corporativoid: this.auth.getCorporativoId(),
        centroid: centroId || null,
        ...(req.params || {})
      }
    };
    return this.http.get<ApiResponse<DataTableResponse>>(`${this.API}/aprobacion/listado`, {
      params: new HttpParams().set('data', JSON.stringify(payload))
    });
  }

  sendAuthorizationOrder(order: {
    pedidoid: number | string;
    clienteid: number | string;
    consignatarioid: number | string;
    monto: number | string;
    concepto: string;
  }): Observable<ApiResponse<Solicitud>> {
    return this.autorizar(Number(order.pedidoid), this.auth.getIdUsuario() ?? 0, order.concepto);
  }

  normalizarPayloadPrefactura(datos: any): any {
    if (!datos || typeof datos !== 'object') {
      return datos;
    }

    const pedidoBase = (datos?.solicitud && typeof datos.solicitud === 'object')
      ? datos.solicitud
      : (datos?.pedido && typeof datos.pedido === 'object')
      ? datos.pedido
      : datos;

    const prefactura =
      pedidoBase?.prefactura ??
      datos?.prefactura ??
      null;

    return {
      ...pedidoBase,
      pedido: pedidoBase,
      solicitud: pedidoBase,
      prefactura,
      promociones: pedidoBase?.promociones ?? datos?.promociones ?? null,
      pedidoCliente: pedidoBase?.pedidoCliente ?? datos?.pedidoCliente ?? true,
      referenciasBnacarias: pedidoBase?.referenciasBnacarias ?? datos?.referenciasBnacarias ?? []
    };
  }

  autorizar(id: number, idUsuarioAutoriza: number, observaciones?: string): Observable<ApiResponse<Solicitud>> {
    let params = new HttpParams().set('idPedido', id).set('idUsuarioAutoriza', idUsuarioAutoriza);
    if (observaciones) params = params.set('observaciones', observaciones);
    return this.http.post<ApiResponse<Solicitud>>(`${this.API}/aprobacion`, null, { params });
  }

  // ------- RECHAZAR SOLICITUD -------
  rechazar(id: number, motivo: string, idUsuarioRechaza?: number): Observable<ApiResponse<Solicitud>> {
    let params = new HttpParams().set('motivo', motivo);
    if (idUsuarioRechaza) params = params.set('idUsuarioRechaza', idUsuarioRechaza);
    return this.http.put<ApiResponse<Solicitud>>(`${this.API}/${id}/rechazar`, null, { params });
  }

  // ------- CANCELAR SOLICITUD -------
  cancelar(id: number, motivo: string, idUsuarioCancela?: number): Observable<ApiResponse<Solicitud>> {
    let params = new HttpParams().set('motivo', motivo);
    if (idUsuarioCancela) params = params.set('idUsuarioCancela', idUsuarioCancela);
    return this.http.put<ApiResponse<Solicitud>>(`${this.API}/${id}/cancelar`, null, { params });
  }

  // ------- OBTENER HISTORIAL DE AUDITORÍA -------
  obtenerHistorial(id: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`/api/reportes/solicitudes/${id}/historial`);
  }

  recuperarVistaPrefacturaGuardado(pedidoId?: number, prefacturaId?: number): Observable<ApiResponse<any>> {
    let params = new HttpParams();
    if (pedidoId != null) {
      params = params.set('pedidoId', String(pedidoId));
    }
    if (prefacturaId != null) {
      params = params.set('prefacturaId', String(prefacturaId));
    }
    return this.http.get<ApiResponse<any>>(`${this.API}/resumen/getVista`, { params });
  }

  /**
   * Completa clienteId y consignatarioId desde la sesión del usuario,
   * replicando el comportamiento del  donde estos datos vienen del login.
   */
  private enriquecerContexto(req: SolicitudRequest): SolicitudRequest {
    return {
      ...req,
      clienteId:       req.clienteId       ?? this.auth.getClienteId()       ?? undefined,
      consignatarioId: req.consignatarioId ?? this.auth.getConsignatarioId() ?? undefined,
      idUsuario:       req.idUsuario        || this.auth.getIdUsuario()       || 0
    };
  }

  private contexto(): string {
    return JSON.stringify({
      corporativoId: this.auth.getCorporativoId(),
      centroId: this.auth.getCentroId(),
      jerarquia: this.auth.getJerarquia(),
      clienteId: this.auth.getClienteId(),
      consignatarioId: this.auth.getConsignatarioId(),
      idUsuario: this.auth.getIdUsuario()
    });
  }
}


