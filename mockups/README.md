# NOVAHUB - Mockups de Interfaz

Prototipos HTML estáticos de las pantallas principales del sistema de Centro de Costos TEC.

## 📂 Archivos

```
mockups/
├── index.html                  # 📑 Página de inicio (ABRE AQUÍ PRIMERO)
├── dashboard.html              # 📊 Dashboard principal con KPIs
├── crear-solicitud.html        # ✏️ Formulario para crear solicitud
├── listar-solicitudes.html     # 📋 Listado de solicitudes
├── autorizar-solicitud.html    # ✅ Pantalla de aprobación/rechazo
└── README.md                   # Este archivo
```

## 🚀 Cómo Abrir

### Opción 1: Abrir en Navegador (Recomendado)
1. Abre la terminal en este directorio
2. Ejecuta:
   ```bash
   # Windows
   start index.html

   # macOS
   open index.html

   # Linux
   xdg-open index.html
   ```

O simplemente haz doble clic en `index.html`

### Opción 2: Usar Live Server (VS Code)
1. Instala la extensión "Live Server" en VS Code
2. Haz clic derecho en `index.html`
3. Selecciona "Open with Live Server"

## 📱 Pantallas Disponibles

### 1. **Dashboard** (`dashboard.html`)
- Vista general del centro de costos
- 4 tarjetas KPI: Solicitudes Pendientes, Autorizadas, Saldo Monedero, Inversión Total
- Menú rápido para crear solicitudes
- Tabla con últimas solicitudes
- **Características mostradas:**
  - Saldo de monedero: $150,000 (OPER-100)
  - 12 solicitudes pendientes
  - 28 solicitudes autorizadas
  - Botones directos a crear solicitudes

### 2. **Crear Solicitud** (`crear-solicitud.html`)
- Formulario completo para crear solicitud de apoyo económico
- **Información General:** Referencia, Descripción, Centro, Tipo
- **Detalles de Empleados:** Tabla dinámica con empleados, montos, descripción
- **Cálculo Automático:** Precio base = $1,500/empleado × cantidad
- **Ejemplo en mockup:** 2 empleados = $3,000
- **Acciones:** Cancelar, Guardar como borrador, Crear solicitud

### 3. **Listar Solicitudes** (`listar-solicitudes.html`)
- Filtros: Estado, Tipo, Búsqueda
- Tabla con todas las solicitudes
- **Columnas:** ID, Tipo, Descripción, Monto, Precio Base, Estado, Fecha, Usuario, Acciones
- **Acciones por fila:** Ver, Autorizar, Rechazar
- Selección masiva con checkboxes
- Estadísticas de estados: Pendientes (12), Autorizados (28), Rechazados (3), Cancelados (1)

### 4. **Autorizar Solicitud** (`autorizar-solicitud.html`)
- Datos completos de la solicitud
- Detalles de dispersión (empleados y montos)
- Resumen financiero
- **Dos opciones:**
  - ✅ AUTORIZAR: Se debitará del monedero
  - ❌ RECHAZAR: Se mantiene el saldo
- Panel lateral con historial y resumen rápido
- Campos para observaciones/motivo

## 💡 Características Demostradas

### Precios Base Automáticos
```
DISPERSION:     $1,500 MXN por empleado
STOCK:          $120 MXN por tarjeta
TARJETA:        $80 MXN por tarjeta
ADICIONAL:      $150 MXN por tarjeta
```

### Datos de Ejemplo
```
Unidad Operativa: OPER-100
Cliente ID: 1001
Consignatario ID: 2001
Saldo Monedero: $150,000
Saldo Crédito: $50,000
```

### Empleados de Prueba
- Andrea Lopez (#10001)
- Luis Martinez (#10002)
- Carla Hernandez (#10003)

### Estados de Solicitud
- 🟡 PENDIENTE - Requiere aprobación
- 🟢 AUTORIZADO - Aprobada
- 🔴 RECHAZADO - Rechazada
- ⚫ CANCELADO - Cancelada

## 🎨 Diseño

- **Paleta de Colores:** Bootstrap 5 estándar
- **Iconos:** Font Awesome 6.4
- **Responsive:** Adaptado para desktop, tablet y móvil
- **Fuente:** Sistema de fuentes del navegador

## 📝 Notas

- Los datos mostrados son **ejemplos estáticos**
- No hay comunicación con el backend
- Los formularios no guardan datos (solo demostración visual)
- Los botones de acción muestran la estructura pero no ejecutan lógica
- Ideal para validar diseño de interfaz antes de implementación Angular

## 🔄 Próximos Pasos

Cuando integres estos diseños en Angular:
1. Utiliza los mockups como referencia de layout y estilos
2. Reemplaza los datos estáticos con servicios del backend
3. Implementa la lógica de formularios y validaciones
4. Conecta los botones de acción a componentes reales
5. Adapta los estilos a tu diseño corporativo

## 📞 Preguntas?

Consulta la documentación del proyecto:
- Backend: `CentroCostosTec-API/`
- Frontend: `CentroCostosTec-Front/`
- Test HTTP: `test-solicitudes.http`

---

**Última actualización:** 15 de mayo de 2026
**Versión:** 1.0 - Prototipos iniciales
