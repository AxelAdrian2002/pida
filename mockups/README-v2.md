# Mockups - Centro de Costos NOVAHUB

Prototipos de interfaz de usuario para el sistema de gestión de solicitudes internas.

## 📊 Disponibilidad de Versiones

### ✨ Versión v2 - RECOMENDADA (Alineada con Angular)
Versión **simplificada** que coincide exactamente con la estructura y estilos de tus componentes Angular actuales.

**Características v2:**
- ✅ Estructura simplificada sin navbar fija
- ✅ Cards con `shadow-sm` y `border-0` de Bootstrap 5
- ✅ Tabla con `table-hover` para solicitudes
- ✅ Badges de colores por estado (warning/success/danger/secondary)
- ✅ Formularios con `form-control`, `form-select`, `form-label`
- ✅ Layout fluido con padding `p-4`
- ✅ Grid de Bootstrap 5 puro (row/col)
- ✅ Incluye precio base en tablas y resúmenes

**Archivos v2:**
- `index-v2.html` - **Índice de navegación principal**
- `dashboard-v2.html` - Pantalla principal con datos y módulos
- `listar-solicitudes-v2.html` - Tabla de solicitudes con filtros
- `crear-solicitud-v2.html` - Formulario para cargar Excel
- `autorizar-solicitud-v2.html` - Panel de aprobación

### 📦 Versión Original v1 (Diseño Detallado)
Versión con diseño más complejo, KPIs, gráficos y estadísticas detalladas.

**Archivos v1:**
- `dashboard.html` - Dashboard con KPIs y estadísticas
- `listar-solicitudes.html` - Tabla con acciones y detalles
- `crear-solicitud.html` - Formulario detallado con empleados
- `autorizar-solicitud.html` - Panel con historial y análisis

---

## 🎨 Características Principales

### Dashboard
- **v2**: Card simple con 8 campos (corporativoId, centroId, clienteId, consignatarioId, username, rol, nombreCompleto, centroNombre)
- **v1**: KPIs con números grandes y gráficos

### Listar Solicitudes
- **v2**: Tabla simple [ID, Tipo, Descripción, Monto, Precio Base, Estado, Fecha]
- **v1**: Tabla con acciones detalladas, estadísticas y búsqueda avanzada

### Crear Solicitud
- **v2**: Botones para descargar plantilla y cargar archivo Excel
- **v1**: Formulario con tabla de empleados y cálculo manual

### Autorizar Solicitud
- **v2**: Radio buttons para autorizar/rechazar con textarea condicional
- **v1**: Gráficos con detalles históricos

---

## 📋 Detalles de Datos

### Tipos de Solicitud
- **DISPERSION**: $1,500 × cantidad de empleados
- **STOCK**: $120 × cantidad de items
- **TARJETA**: $80 × cantidad de tarjetas
- **ADICIONAL**: $150 × cantidad de asignaciones

### Estados
- **PENDIENTE**: `badge bg-warning text-dark`
- **AUTORIZADO**: `badge bg-success`
- **RECHAZADO**: `badge bg-danger`
- **CANCELADO**: `badge bg-secondary`

### Datos de Prueba
- Corporativo: NOVA01
- Unidades: OPER-100 (1001/2001), OPER-200 (1002/2002)
- Empleados: 10001 (Andrea), 10002 (Luis), 10003 (Carla)
- Saldo disponible: $150,000.00

---

## 🚀 Cómo Usar

### Para Revisar los Mockups
1. Abre `index-v2.html` para ver el índice con navegación
2. Las versiones **v2 están destacadas en verde** (RECOMENDADAS)
3. Haz clic en "Ver Mockup" para abrir cada pantalla

### Para Implementar en Angular
1. Usa los mockups v2 como referencia visual
2. Copia las clases Bootstrap: `p-4`, `card shadow-sm`, `table-hover`, `form-label`, etc.
3. Mantén la estructura simplificada sin navbar fija
4. Usa badges con colores definidos por estado

### Para Comparar Versiones
1. Abre un mockup v1 y v2 en pestañas diferentes
2. La v2 tiene estructura más limpia y alineada con Angular
3. La v1 muestra opciones de diseño más elaboradas

---

## 🔄 Características Incluidas

### Común en Ambas Versiones
- ✅ Tabla de solicitudes con precio base
- ✅ Sistema de badges por estado
- ✅ Filtros por estado
- ✅ Botones de acción (Autorizar, Rechazar, etc.)
- ✅ Cálculo automático de precio base
- ✅ Datos multiempresa (OPER-100 y OPER-200)
- ✅ Moneda MXN
- ✅ Iconos FontAwesome

### Solo en v1
- Gráficos y estadísticas KPI
- Análisis financiero
- Historial detallado
- Búsqueda avanzada

### Solo en v2
- Estructura simplificada
- Directo a componentes Angular
- Sin complejidad innecesaria

---

## 📝 Notas de Desarrollo

### Próximos Pasos
1. ✅ Crear mockups v2 alineados con Angular
2. ⏳ Implementar tablas con `*ngFor` y price binding
3. ⏳ Agregar validaciones de formularios
4. ⏳ Conectar endpoints API reales
5. ⏳ Implementar flujo de aprobación con modales

### Estructura Bootstrap Utilizada
```html
<!-- Card -->
<div class="card shadow-sm border-0">
  <div class="card-header">Título</div>
  <div class="card-body">Contenido</div>
</div>

<!-- Tabla -->
<table class="table table-hover">
  <!-- headers y rows -->
</table>

<!-- Badge por estado -->
<span class="badge bg-warning text-dark">PENDIENTE</span>
<span class="badge bg-success">AUTORIZADO</span>
<span class="badge bg-danger">RECHAZADO</span>
```

---

## 📚 Recursos

- **Bootstrap 5**: https://getbootstrap.com/
- **FontAwesome 6**: https://fontawesome.com/
- **Angular 17+**: https://angular.io/

---

**Versión del Documento**: 2.0 - Alineación con Componentes Angular
**Última Actualización**: Mayo 2026
