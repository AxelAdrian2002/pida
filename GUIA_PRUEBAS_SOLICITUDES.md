# Guía de Prueba - Sistema de Solicitudes Multi-Empresa

## 📋 Resumen de Cambios Aplicados

### 1. **Correcciones de Seguridad**
- ✅ `guardarSolicitud()` ahora usa `contextProvider.getClienteId()` en lugar de `dto.getClienteId()`
- ✅ Validación de contexto ahora usa los valores autenticados del JWT, no del DTO
- ✅ Previene creación de solicitudes en empresas no autorizadas

### 2. **Endpoints Nuevos Agregados**
```
PUT /api/solicitudes/{id}/autorizar?observaciones=...   → ADMIN/CAPTURA
PUT /api/solicitudes/{id}/rechazar?motivo=...           → ADMIN
PUT /api/solicitudes/{id}/cancelar?motivo=...           → ADMIN
```

### 3. **Correcciones de Errores**
- ✅ Eliminado `setFechaModificacion()` (no existe en Solicitud)
- ✅ Agregado `setEstadoId()` en todos los métodos de cambio de estado
- ✅ Null safety en timestamp parsing
- ✅ Unboxing seguro de valores null

---

## 🧪 Tipos de Solicitud

### 1. DISPERSION (Apoyo Económico)
**Ruta:** `/api/solicitudes/apoyo-economico`
**Uso:** Dispersión de dinero a empleados

```json
{
  "tipoSolicitud": "DISPERSION",
  "descripcion": "Solicitud de apoyo económico",
  "referencia": "REF-001",
  "montoTotal": 5000.00,
  "idUsuario": 1,
  "detalles": [
    {
      "numeroEmpleado": "E001",
      "nombreEmpleado": "Juan Pérez",
      "monto": 2500.00,
      "descripcion": "Apoyo"
    }
  ]
}
```

**Validaciones:**
- `idUsuario` REQUERIDO
- `montoTotal` debe ser > 0
- `detalles` con empleados y montos

---

### 2. STOCK (Reposición)
**Ruta:** `/api/solicitudes/reposicion`
**Uso:** Reposición de tarjetas o recursos

```json
{
  "tipoSolicitud": "STOCK",
  "descripcion": "Solicitud de reposición",
  "referencia": "REF-002",
  "montoTotal": 0.00,
  "idUsuario": 1,
  "detalles": [
    {
      "numeroEmpleado": "E001",
      "nombreEmpleado": "Juan Pérez",
      "descripcion": "Reposición"
    }
  ]
}
```

**Validaciones:**
- `idUsuario` REQUERIDO
- `montoTotal` típicamente 0 (es solo reposición, no dinero)
- `detalles` con empleados

---

### 3. TARJETA (Nueva Asignación)
**Ruta:** `/api/solicitudes/nueva-asignacion`
**Uso:** Emitir nueva tarjeta a empleado

```json
{
  "tipoSolicitud": "TARJETA",
  "descripcion": "Nueva asignación de tarjeta",
  "referencia": "REF-003",
  "montoTotal": 0.00,
  "idUsuario": 1,
  "detalles": [
    {
      "numeroEmpleado": "E003",
      "nombreEmpleado": "Carlos López",
      "descripcion": "Nueva asignación"
    }
  ]
}
```

**Validaciones:**
- `idUsuario` REQUERIDO
- Empleado no debe existir previamente
- `montoTotal` típicamente 0

---

### 4. ADICIONAL (Asignación Adicional)
**Ruta:** `/api/solicitudes/asignacion-adicional`
**Uso:** Agregar tarjeta adicional a empleado existente

```json
{
  "tipoSolicitud": "ADICIONAL",
  "descripcion": "Asignación adicional de tarjeta",
  "referencia": "REF-004",
  "montoTotal": 0.00,
  "idUsuario": 1,
  "detalles": [
    {
      "numeroEmpleado": "E001",
      "nombreEmpleado": "Juan Pérez",
      "descripcion": "Asignación adicional"
    }
  ]
}
```

**Validaciones:**
- `idUsuario` REQUERIDO
- Empleado debe existir previamente
- `montoTotal` típicamente 0

---

## 🔐 Ciclo de Vida de una Solicitud

```
CREAR (EMPLEADO)
    ↓
PENDIENTE → AUTORIZAR (ADMIN/CAPTURA)
    ↓
    ├─ AUTORIZADO (éxito)
    │
    ├─ RECHAZAR (ADMIN) → RECHAZADO
    │   con motivo
    │
    └─ CANCELAR (ADMIN) → CANCELADO
        con motivo
```

---

## 🚀 Flujos de Prueba

### A. Crear → Autorizar
```bash
1. POST /api/solicitudes/apoyo-economico
   → Recibe ID (ej: 1)
   
2. PUT /api/solicitudes/1/autorizar?observaciones=Aprobada
   → Estado: AUTORIZADO
```

### B. Crear → Rechazar
```bash
1. POST /api/solicitudes/apoyo-economico
   → Recibe ID (ej: 2)
   
2. PUT /api/solicitudes/2/rechazar?motivo=Datos%20incompletos
   → Estado: RECHAZADO
```

### C. Crear → Cancelar
```bash
1. POST /api/solicitudes/nueva-asignacion
   → Recibe ID (ej: 3)
   
2. PUT /api/solicitudes/3/cancelar?motivo=Cambio%20de%20planes
   → Estado: CANCELADO
```

---

## 📊 Consultas y Reportes

### Listar Solicitudes
```bash
GET /api/solicitudes?estado=PENDIENTE&page=0&size=10
```

**Estados soportados:**
- `PENDIENTE` (estados internos: NVO, FACA, FACR, PCRE)
- `AUTORIZADO` (estado interno: PLIB)
- `RECHAZADO` (estado interno: RECH)
- `CANCELADO` (estados internos: CANP, CANC)

### Historial de Cambios
```bash
GET /api/reportes/solicitudes/1/historial
```

Devuelve:
- `usuario_id`: Quién hizo el cambio
- `accion`: CREAR, AUTORIZAR, RECHAZAR, CANCELAR
- `estado_anterior`: Estado antes
- `estado_nuevo`: Estado después
- `motivo_cambio`: Razón del cambio
- `fecha_cambio`: Timestamp

### Reporte de Auditoría
```bash
GET /api/reportes/auditoria?fechaInicio=2025-05-01&fechaFin=2025-05-14
```

Devuelve resumen con:
- Total de cambios
- Agrupado por acción
- Agrupado por tipo de solicitud

---

## ❌ Posibles Errores y Soluciones

### Error: "No se pudo obtener el contexto"
**Causa:** JWT no contiene `clienteId` o `consignatarioId`
**Solución:** Verificar que el JWT esté correctamente firmado con esos claims

### Error: "El motivo del rechazo es requerido"
**Causa:** Parámetro `motivo` vacío en PUT /rechazar
**Solución:** Incluir `?motivo=Razón%20válida`

### Error: "Solo se pueden autorizar solicitudes en estado PENDIENTE"
**Causa:** Solicitud ya fue procesada (AUTORIZADA, RECHAZADA, CANCELADA)
**Solución:** Crear nueva solicitud

### Error: "No se pueden rechazar solicitudes en estado..."
**Causa:** Solicitud no está en estado PENDIENTE
**Solución:** Solo se pueden rechazar solicitudes pendientes

### Error: "No se pueden cancelar solicitudes en estado..."
**Causa:** Solicitud está en estado RECHAZADO
**Solución:** Solo se pueden cancelar solicitudes PENDIENTE o AUTORIZADO

---

## 🔧 Pruebas Recomendadas

### Test 1: DISPERSION Completo
```
1. POST /api/solicitudes/apoyo-economico (crear)
2. GET /api/solicitudes/{id} (verificar PENDIENTE)
3. PUT /api/solicitudes/{id}/autorizar (autorizar)
4. GET /api/reportes/solicitudes/{id}/historial (ver auditoría)
```

### Test 2: Stock con Rechazo
```
1. POST /api/solicitudes/reposicion (crear)
2. PUT /api/solicitudes/{id}/rechazar?motivo=Sin%20autorización (rechazar)
3. GET /api/reportes/solicitudes/{id}/historial (verificar cambio)
```

### Test 3: Tarjeta con Cancelación
```
1. POST /api/solicitudes/nueva-asignacion (crear)
2. PUT /api/solicitudes/{id}/autorizar (autorizar)
3. PUT /api/solicitudes/{id}/cancelar?motivo=Cambio%20de%20planes (cancelar)
4. GET /api/reportes/auditoria (ver timeline)
```

---

## 📝 Notas Importantes

1. **ClienteId y ConsignatarioId:**
   - Vienen automáticamente del JWT (contextProvider)
   - NO se pueden pasar en el DTO
   - Previene que usuarios accedan a otros clientes

2. **Permisos:**
   - EMPLEADO: Solo crear solicitudes
   - CAPTURA: Crear y autorizar
   - ADMIN: Crear, autorizar, rechazar, cancelar, reportes

3. **Estados en BD:**
   - Internamente usa códigos: NVO, PLIB, RECH, CANC, etc.
   - La API los mapea a: PENDIENTE, AUTORIZADO, RECHAZADO, CANCELADO

4. **Auditoría:**
   - Automática con cada cambio
   - Registra usuario, acción, estados anterior/nuevo, motivo, timestamp
   - No modificable (tabla de auditoría es read-only para usuarios)
