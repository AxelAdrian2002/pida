# Mockups Completos - NOVAHUB Centro de Costos

## 📋 Descripción General
Conjunto completo de mockups HTML v2 para el sistema de gestión de solicitudes NOVAHUB. Todos los mockups siguen un diseño consistente con Bootstrap 5.3.0 y FontAwesome 6.4.0.

---

## ✅ Mockups Creados (20 Total)

### **Gestión de Solicitudes (7 mockups)**
1. **dashboard-v2.html** - Panel principal con KPIs y acceso rápido a módulos
2. **listar-solicitudes-v2.html** - Tabla de solicitudes con filtros y estado
3. **crear-solicitud-v2.html** - Selector de tipo de solicitud
4. **solicitud-stock-v2.html** - Crear solicitud de reposición STOCK ($120/item)
5. **solicitud-tarjeta-v2.html** - Crear solicitud de TARJETA/CREDENCIAL ($80/item)
6. **solicitud-adicional-v2.html** - Crear solicitud ADICIONAL ($150/item)
7. **autorizar-solicitud-v2.html** - Panel de aprobación con resumen financiero
8. **prefactura-v2.html** - Resumen de solicitud antes de autorizar
9. **rechazar-solicitud-v2.html** - Modal para rechazar con motivo

### **Auditoría y Reportes (3 mockups)**
10. **historial-solicitud-v2.html** - Timeline de cambios y auditoría
11. **reportes-v2.html** - Análisis estadístico y filtros avanzados
12. **reporte-grupo-v2.html** - (Integrado en grupos) Reporte de empleados por grupo

### **Gestión de Credenciales (2 mockups)**
13. **credenciales.html** - Tabla de credenciales con activación/baja
14. (Gestión completa en empresa-config)

### **Gestión de Empleados (3 mockups)**
15. **empleados-v2.html** - Listado de empleados activos
16. **actualiza-empleado-v2.html** - Formulario de edición multiasección
17. **cambiar-password-v2.html** - Cambio de contraseña

### **Gestión de Equipos/Grupos (3 mockups)**
18. **grupos-v2.html** - Tabla de 18 columnas de grupos/direcciones
19. **registro-grupo-v2.html** - Crear nuevo grupo con domicilio y contactos
20. **asignacion-grupo-v2.html** - Asignar empleados a grupos (2 columnas)

### **Gestión de Empresa (2 mockups)**
21. **empresa-config-v2.html** - 3 pestañas: Carga masiva, Credenciales, Branding
22. **registro-empresa-v2.html** - (Puede agregarse) Registro de nueva empresa

### **Navegación**
23. **index-completo.html** - Índice maestro con tarjetas de acceso a todos los mockups

---

## 🎨 Características Técnicas

### Bootstrap 5.3.0 - Clases Utilizadas
- **Layout**: `container-lg`, `container-fluid`, `row g-3`, `col-md-*`, `col-lg-*`
- **Cards**: `card`, `card-header`, `card-body`, `card-footer`, `shadow-sm`, `shadow-lg`
- **Tablas**: `table table-hover`, `table-light`, `table-responsive`, `table-sm`
- **Botones**: `btn btn-primary`, `btn-outline-*`, `btn-sm`, `btn-lg`
- **Formularios**: `form-control`, `form-select`, `form-label`, `input-group`, `form-check`
- **Badges**: `badge bg-success`, `badge bg-warning`, `badge bg-danger`
- **Alertas**: `alert alert-success`, `alert-info`, `alert-warning`, `alert-danger`
- **Utilities**: `p-4`, `mb-3`, `mt-4`, `d-flex`, `justify-content-between`, `align-items-center`

### FontAwesome 6.4.0 - Iconos Utilizados
- `fas fa-*` - Iconos sólidos del sistema
- Ejemplos: `fa-search`, `fa-download`, `fa-check`, `fa-trash`, `fa-edit`, `fa-users`, etc.

### Diseño Responsivo
- Mobile-first approach
- Breakpoints: `col-md-*`, `col-lg-*` para tablets y desktops
- Tablas con scroll horizontal en móvil
- Modales centrados en todos los dispositivos

---

## 📊 Estructuras de Datos

### Solicitudes
- **ID**: SOL-XXXXX
- **Tipos**: DISPERSION ($1,500/item), STOCK ($120/item), TARJETA ($80/item), ADICIONAL ($150/item)
- **Estados**: CREAR, PENDIENTE, REVISIÓN, AUTORIZADO, RECHAZADO, CANCELADO
- **Campos**: ID, Tipo, Descripción, Monto, Estado, Fecha

### Empleados
- **ID**: EMP-XXXXX (ej: 10001)
- **Campos**: Nombre, RFC, Puesto, Email, Teléfono, Extensión, Centro de Costos
- **Estado**: Activo, Inactivo, Baja

### Grupos/Equipos
- **ID**: GRP-XXXXX
- **18 Columnas**: Grupo ID, Descripción, Calle, Número, Colonia, C.P., Delegación, Estado, Contacto 1, Tel 1, Contacto 2, Tel 2, Horario, Estatus, Fecha, Observación
- **Contactos**: Nombre, Puesto, Teléfono, Email

### Credenciales
- **Tipos**: Tarjeta Débito, Tarjeta Crédito, Tarjeta de Acceso
- **Estados**: ACTIVA, INACTIVA, CANCELADA
- **Campos**: Cuenta, Número (enmascarado), Empleado, Vigencia, Estatus

---

## 🎯 Funcionalidades por Módulo

### **Solicitudes**
- ✅ Listar con filtros por estado
- ✅ Crear 4 tipos diferentes
- ✅ Autorizar con análisis financiero
- ✅ Rechazar con motivo
- ✅ Ver prefactura/resumen
- ✅ Historial completo con auditoría
- ✅ Reportes estadísticos

### **Empleados**
- ✅ Listado con búsqueda y filtros
- ✅ Actualizar datos (Identificación, Laboral, Contacto)
- ✅ Exportar a Excel
- ✅ Cambiar contraseña

### **Grupos/Equipos**
- ✅ Listado con 18 columnas
- ✅ Crear nuevo grupo
- ✅ Asignar empleados (interfaz de 2 columnas)
- ✅ Reporte de empleados

### **Empresa**
- ✅ Carga masiva de empleados (Excel)
- ✅ Generación de lotes de credenciales
- ✅ Asignación automática de credenciales
- ✅ Branding (colores, logo, nombre)

### **Seguridad**
- ✅ Cambiar contraseña
- ✅ Auditoría completa
- ✅ Notificaciones
- ✅ Validaciones

---

## 🚀 Cómo Usar

### Abrir en Navegador
1. Guarda los archivos en: `c:\Users\axela\Desktop\Escritorio\pida\mockups\`
2. Abre `index-completo.html` en tu navegador (Chrome, Firefox, Edge)
3. Navega usando las tarjetas de acceso a cada mockup

### Integración con Angular
Los mockups están diseñados para ser reemplazados por componentes Angular:
1. Copiar estructura HTML de cada mockup
2. Convertir a template Angular (ng-if, *ngFor, etc.)
3. Conectar servicios (SolicitudService, EmpleadoService, etc.)
4. Aplicar mismos estilos CSS

### Personalización
- **Colores**: Actualizar en estilos inline o CSS global
- **Datos**: Todos los datos son ejemplos - reemplazar con API
- **Iconos**: FontAwesome permite 6,000+ iconos adicionales
- **Responsive**: Funciona en móvil, tablet y desktop

---

## 📱 Dispositivos Soportados
- ✅ Desktop (1920x1080+)
- ✅ Tablet (768px+)
- ✅ Mobile (320px+)

---

## 🔗 Archivos Individuales

```
mockups/
├── index-completo.html                 # Índice maestro ⭐
├── dashboard-v2.html                   # Dashboard
├── listar-solicitudes-v2.html          # Listado de solicitudes
├── crear-solicitud-v2.html             # Selector de tipo
├── solicitud-stock-v2.html             # Stock
├── solicitud-tarjeta-v2.html           # Tarjeta
├── solicitud-adicional-v2.html         # Adicional
├── autorizar-solicitud-v2.html         # Autorizar
├── prefactura-v2.html                  # Resumen
├── rechazar-solicitud-v2.html          # Rechazar (Modal)
├── historial-solicitud-v2.html         # Historial
├── reportes-v2.html                    # Reportes
├── empleados-v2.html                   # Listado empleados
├── actualiza-empleado-v2.html          # Editar empleado
├── cambiar-password-v2.html            # Contraseña
├── grupos-v2.html                      # Listado grupos
├── registro-grupo-v2.html              # Crear grupo
├── asignacion-grupo-v2.html            # Asignar empleados
├── empresa-config-v2.html              # Configuración empresa
├── credenciales.html                   # Gestión credenciales
└── README-v2.md                        # Este archivo
```

---

## ✨ Notas de Diseño

### Paleta de Colores
- **Primario**: #667eea (Azul Púrpura)
- **Secundario**: #764ba2 (Púrpura)
- **Success**: #28a745 (Verde)
- **Warning**: #ffc107 (Amarillo)
- **Danger**: #dc3545 (Rojo)
- **Info**: #17a2b8 (Cian)

### Tipografía
- Fuente: Sistema de Bootstrap (sans-serif)
- Pesos: Regular (400), Semibold (600), Bold (700)

### Espaciado
- Padding estándar: 0.5rem (p-1) a 3rem (p-5)
- Margin gap: 0.5rem (g-1) a 3rem (g-5)
- Altura de componentes: Flexible por contenido

---

## 📞 Soporte

Para preguntas o mejoras en los mockups, contactar al equipo de desarrollo.

**Versión**: v2 (Simplificada y Alineada)
**Última Actualización**: Enero 2024
**Estado**: ✅ COMPLETO

