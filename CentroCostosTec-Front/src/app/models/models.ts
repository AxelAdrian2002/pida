// Models del sistema Centro de Costos TEC

export interface ApiResponse<T> {
  codigo: number;
  mensaje: string;
  datos: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface LoginRequest {
  /** Equivalente a ModelLogin.corporativo del  */
  corporativo: string;
  username: string;
  password: string;
  /** Contador de intentos fallidos (para bloqueo tras 3 intentos, igual que ) */
  intentos?: number;
}

export interface LoginResponse {
  token?: string;
  accessToken?: string;
  refreshToken?: string;
  expiresIn?: number;
  username: string;
  nombreCompleto: string;
  rol: string;
  idUsuario: number;
  /** clienteid de la tabla centrocostos (dbdespensa) */
  clienteId?: number;
  clienteid?: number;
  /** consignatarioid de la tabla centrocostos (dbdespensa) */
  consignatarioId?: number;
  consignatarioid?: number;
  /** Mismo que corporativoID en corpusuarios */
  corporativoId?: string;
  /** Mismo que centroID en centrocostos */
  centroId?: string;
  requiereCambioPassword?: boolean;
}

export interface EmpresaConfiguracion {
  empresa: {
    codigoEmpresa?: string;
    nombreEmpresa?: string;
    colorPrimario?: string;
    colorSecundario?: string;
    logoUrl?: string;
    razonSocial?: string;
    rfc?: string;
    emailContacto?: string;
    telefonoContacto?: string;
    sitioWeb?: string;
    direccion?: {
      calle?: string;
      numeroExterior?: string;
      numeroInterior?: string;
      colonia?: string;
      municipio?: string;
      estado?: string;
      pais?: string;
      codigoPostal?: string;
    };
  };
  perfil: {
    nombre?: string;
    email?: string;
    curp?: string;
    rfc?: string;
    fotoUrl?: string;
  };
}

export interface Solicitud {
  idSolicitud: number;
  idPedido?: number;
  tipoSolicitud: 'DISPERSION' | 'STOCK' | 'TARJETA' | 'ADICIONAL';
  tipoPedido?: 'DISPERSION' | 'STOCK' | 'TARJETA' | 'ADICIONAL';
  estado: 'PENDIENTE' | 'AUTORIZADO' | 'RECHAZADO' | 'CANCELADO';
  montoTotal: number;
  idUsuario: number;
  descripcion: string;
  referencia: string;
  fechaCreacion: string;
  fechaAutorizacion?: string;
  usuarioAutorizo?: string;
  observaciones?: string;
  detalles?: SolicitudDetalle[];
}

export interface SolicitudDetalle {
  idDetalle: number;
  idEmpleado?: number;
  numeroEmpleado?: string;
  nombreEmpleado?: string;
  monto?: number;
  descripcion?: string;
  numeroCredencial?: string;
  numeroTarjeta?: string;
}

export interface SolicitudRequest {
  tipoSolicitud: string;
  tipoPedido?: string;
  descripcion?: string;
  referencia?: string;
  montoTotal?: number;
  idUsuario: number;
  clienteId?: number;
  consignatarioId?: number;
  detalles?: SolicitudDetalle[];
}

export interface SolicitudPreview {
  respuesta?: boolean | string;
  solicitud?: any;
  pedido?: any;
  object?: string;
}

export interface FillSelectOption {
  id: string;
  nombre: string;
}

export interface DataTableRequest {
  start: number;
  length: number;
  draw: number;
  search?: string;
  params?: Record<string, any>;
}

export interface DataTableResponse<T = any> {
  draw: number;
  recordsTotal: number;
  recordsFiltered: number;
  data: T[];
  monederoSaldo?: number;
  creditoSaldo?: number;
}

export interface Credencial {
  idCredencial: number;
  idTarjeta?: number;
  numeroCredencial: string;
  numeroTarjeta?: string;
  idEmpleado: number;
  numeroEmpleado: string;
  nombreEmpleado: string;
  estado: 'ACTIVA' | 'INACTIVA' | 'CANCELADA';
  idGrupo?: number;
  cuentaId?: string;
  tipoCredencial?: string;
  tipoTarjeta?: string;
  cuentaSaldo?: number;
  estatusId?: string;
  credencialCancelada?: boolean;
  tarjetaCancelada?: boolean;
  fechaEmision?: string;
  fechaActivacion?: string;
  fechaCancelacion?: string;
  motivoCancelacion?: string;
  usuarioOperacion?: string;
}

export interface CredencialBitacora {
  idBitacora: number;
  idCredencial: number;
  idTarjeta?: number;
  numeroCredencial: string;
  numeroTarjeta?: string;
  estadoAnterior: string;
  estadoNuevo: string;
  usuarioOperacion: string;
  observacion?: string;
  fechaOperacion: string;
}

export interface CredencialOperacionRequest {
  numeroCredencial: string;
  numeroTarjeta?: string;
  observacion?: string;
  idUsuario: number;
  bitacoraId?: number;
  usuarioOperacion: string;
  clienteId?: number;
  consignatarioId?: number;
}

// Alias de compatibilidad temporal
export type Pedido = Solicitud;
export type PedidoDetalle = SolicitudDetalle;
export type PedidoRequest = SolicitudRequest;
export type PedidoPreview = SolicitudPreview;
export type Tarjeta = Credencial;
export type TarjetaBitacora = CredencialBitacora;
export type TarjetaOperacionRequest = CredencialOperacionRequest;

export interface Grupo {
  idGrupo: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
  fechaAlta: string;
  fechaModificacion?: string;
  iddirecciones?: number;
  grupoid?: string;
  calle?: string;
  numero?: string;
  colonia?: string;
  codigoPostal?: string;
  codigopostal?: string;
  delegacion?: string;
  estado?: string;
  telefono?: string;
  nombre2?: string;
  nombreContacto1?: string;
  telefono1?: string;
  nombreContacto2?: string;
  telefono2?: string;
  horario?: string;
  estatus?: boolean;
  fecha?: string;
  observacion?: string;
  usuarioAlta?: string;
  empleados?: GrupoEmpleado[];
}

export interface GrupoEmpleado {
  idAsignacion?: number;
  idGrupo?: number;
  nombreGrupo?: string;
  idEmpleado?: number;
  numeroEmpleado: string;
  activo: boolean;
  fechaAsignacion?: string;
  usuarioAsigno?: string;
}

export interface GrupoRequest {
  nombre?: string;
  grupoid?: string;
  descripcion?: string;
  calle?: string;
  numero?: string;
  colonia?: string;
  codigopostal?: string;
  delegacion?: string;
  estado?: string;
  estatusid?: boolean;
  nombre2?: string;
  telefono?: string;
  telefono2?: string;
  horario?: string;
  observacion?: string;
  action?: 'Registrar' | 'Actualizar' | string;
  iddirecciones?: number;
  usuarioAlta?: string;
  clienteId?: number;
  consignatarioId?: number;
}

export interface Empleado {
  idEmpleado: number;
  numeroEmpleado: string;
  nombre: string;
  apellidoPaterno: string;
  apellidoMaterno?: string;
  email?: string;
  telefono?: string;
  clienteId?: number;
  consignatarioId?: number;
  estatusId?: string;
  activo: boolean;
  fechaAlta: string;
}

export interface EmpleadoRequest {
  numeroEmpleado: string;
  nombre: string;
  apellidoPaterno: string;
  apellidoMaterno?: string;
  email?: string;
  telefono?: string;
  usuarioModificacion?: string;
  clienteId?: number;
  consignatarioId?: number;
  puesto?: string;
  extension?: string;
  telefonoFijo?: string;
  telefonoMovil?: string;
}

export interface DatabaseStatus {
  base: string;
  motor: string;
  url: string;
  conectada: boolean;
  detalle: string;
}

export interface ModuloCobertura {
  modulo: string;
  bases: string[];
  descripcion: string;
}

export interface DatabaseStatusPayload {
  bases: DatabaseStatus[];
  cobertura: ModuloCobertura[];
}

