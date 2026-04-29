# Proyecto Integrador de Dominio Autonomo
## Certificacion Full Stack Web Developer

## 1. Area de Aplicacion

**Area de Aplicacion seleccionada:**
- BACK-END
- CLIENT SIDE FRONT-END

**Justificacion:**
El proyecto contempla el desarrollo completo de una solucion web para la gestion de centros de costo de despensa dentro de Efectivale. Incluye la construccion del cliente web (Angular) para operacion diaria y visualizacion de datos, asi como la capa de servicios (Java + Spring) para reglas de negocio, seguridad, integracion y persistencia.

---

## 2. Descripcion del Proyecto

**Nombre del programa o portafolio:**
Sistema Web de Gestion de Centros de Costo de Despensa - Efectivale

**Descripcion:**
El proyecto consiste en disenar e implementar una aplicacion web Full Stack para administrar centros de costo asociados a vales de despensa. La plataforma permitira registrar, consultar, actualizar y validar informacion operativa clave mediante una interfaz centralizada, reduciendo procesos manuales y mejorando la trazabilidad de la informacion.

El sistema se desarrollara con Angular 18 para la capa de presentacion y Java 21 LTS con Spring Framework 6 para la capa de negocio y APIs REST.

---

## 3. Objetivo de Mejora (SMART)

**Objetivo SMART:**
Disenar e implementar, en un plazo de 12 semanas durante 2026, una aplicacion web Full Stack basada en Angular 18 y Java 21 LTS con Spring Framework 6 para la gestion de centros de costo de despensa en Efectivale, que permita operaciones de alta, consulta, actualizacion y validacion de registros, con el fin de reducir en al menos 30% el tiempo operativo de gestion manual y mejorar en 25% la consistencia y trazabilidad de los datos en la fase inicial del proyecto.

**Validacion SMART:**
- **Specific:** Define funcionalidad, tecnologia y proceso objetivo (centros de costo de despensa).
- **Measurable:** Establece metas numericas (30% y 25%).
- **Achievable:** Se enfoca en un modulo operativo delimitado.
- **Realistic:** Replica un flujo real de negocio de la organizacion.
- **Time-bound:** Fija horizonte de implementacion de 12 semanas en 2026.

---

## 4. Matriz de Mejora

| Nombre del programa o portafolio | Descripcion | Indicador de Mejora | Meta |
|---|---|---|---|
| Implementacion de un sistema web para gestion de centros de costo de despensa en Efectivale | Se desarrollara una solucion Full Stack que centralice el alta, consulta y actualizacion de centros de costo, con validaciones de negocio y control de acceso para usuarios internos. | 1) Reduccion del tiempo de captura/actualizacion en 30%. 2) Disminucion de inconsistencias de datos en 20%. 3) Mejora de trazabilidad y consulta en 25%. | Entregar un prototipo funcional en 12 semanas con operaciones CRUD, validaciones, seguridad basica y evidencia de mejora operativa medible al cierre de 2026. |

---

## 5. Seleccion de Competencias y Aplicacion

### 2.1 Programacion en Java 21 LTS
**Aplicacion en el proyecto:**
Se utilizara Java 21 para implementar la logica de dominio del sistema: modelado de entidades, validaciones, flujo de negocio, manejo de errores y estandarizacion de respuestas para APIs.

### 2.2 Desarrollo del Back-End Web con Java Spring Framework 6
**Aplicacion en el proyecto:**
Se construiran APIs REST bajo arquitectura por capas (controller, service, repository) para gestionar centros de costo y sus relaciones. Se usara JPA para persistencia y mecanismos de logging para seguimiento operativo.

### 2.3 Desarrollo Avanzado del Back-End Web con Java Spring Framework 6
**Aplicacion en el proyecto:**
Se implementaran controles de autenticacion y autorizacion por roles para proteger operaciones sensibles, pruebas unitarias en servicios criticos y lineamientos de despliegue en Linux para un entorno de ejecucion estable.

### 3.1 Fundamentos de Programacion Web
**Aplicacion en el proyecto:**
Se aplicaran HTML, CSS y JavaScript para la composicion visual, interaccion en formularios, consumo de JSON y validaciones en cliente que mejoren la experiencia de usuario y reduzcan errores de captura.

### 3.3 Fundamentos de Front-End con Angular 18
**Aplicacion en el proyecto:**
Se desarrollara una SPA modular con componentes reutilizables, formularios reactivos, servicios para consumo de APIs, enrutamiento y manejo de estado de interfaz para optimizar mantenibilidad y escalabilidad.

---

## 6. Enfoque Tecnico para Defensa del Proyecto

## 6.1 Arquitectura propuesta
- **Frontend:** Angular 18 (SPA), modulos funcionales por dominio.
- **Backend:** Java 21 + Spring Boot 3.x/Spring Framework 6 con APIs REST.
- **Persistencia:** Base de datos relacional (definicion de tablas en curso), acceso via Spring Data JPA.
- **Integracion:** Intercambio JSON sobre HTTPS.
- **Observabilidad:** Logging estructurado y trazabilidad de operaciones clave.

**Razon tecnica de esta arquitectura:**
Permite desacoplar presentacion y negocio, facilitando evolucion incremental del modelo de datos mientras las tablas se terminan de definir.

## 6.2 Diseno de capas (Back-End)
- **Controller Layer:** Exposicion de endpoints REST, validacion de entrada basica y codigos HTTP.
- **Service Layer:** Reglas de negocio (validaciones funcionales, consistencia, flujo transaccional).
- **Repository Layer:** Persistencia y consultas optimizadas.
- **DTO/Mapper Layer:** Separacion entre contrato API y entidades internas para evitar acoplamiento.

**Defensa tecnica:**
Esta separacion mejora testabilidad, mantenibilidad y capacidad de cambio sin afectar consumidores.

## 6.3 Seguridad
- Autenticacion de usuarios para acceso al sistema.
- Autorizacion por roles (ejemplo: consulta, captura, administrador).
- Validacion de entradas para prevenir datos inconsistentes.
- Manejo centralizado de errores para evitar exposicion de detalles sensibles.

**Defensa tecnica:**
La seguridad por capas minimiza riesgo operativo y fortalece cumplimiento de buenas practicas para sistemas corporativos.

## 6.4 Calidad y pruebas
- **Pruebas unitarias:** servicios de negocio y validadores.
- **Pruebas de integracion:** endpoints criticos y acceso a datos.
- **Criterios de aceptacion:** casos de uso clave (alta, consulta, actualizacion).

**Indicadores tecnicos sugeridos:**
- Cobertura de pruebas en logica critica >= 70%.
- Tasa de error funcional en pruebas UAT < 5%.
- Tiempo promedio de respuesta API para consultas frecuentes < 500 ms en entorno de pruebas.

## 6.5 Estrategia de implementacion por fases
1. **Fase 1 (Semanas 1-3):** Definicion funcional, modelo inicial de datos, estructura base de proyecto.
2. **Fase 2 (Semanas 4-6):** Implementacion Back-End (CRUD + reglas de negocio).
3. **Fase 3 (Semanas 7-9):** Implementacion Front-End (formularios, tablas, filtros, navegacion).
4. **Fase 4 (Semanas 10-12):** Seguridad, pruebas, ajustes y evidencias de indicadores.

**Defensa tecnica:**
La ejecucion incremental reduce riesgo, permite validar valor temprano y facilita ajustar el modelo de datos sin rehacer toda la solucion.

## 6.6 Riesgos y mitigacion
- **Riesgo:** Cambios en tablas durante el desarrollo.
  - **Mitigacion:** Uso de DTOs y capa de mapeo para aislar cambios de esquema.
- **Riesgo:** Reglas de negocio aun no formalizadas al 100%.
  - **Mitigacion:** Iteraciones cortas con validaciones parciales y backlog priorizado.
- **Riesgo:** Retrabajo por cambios de alcance.
  - **Mitigacion:** Definir MVP funcional con entregables medibles por sprint.

---

## 7. Evidencias para Sustentar la Defensa

Para presentacion academica y tecnica, se recomienda anexar:
- Diagrama de arquitectura (Frontend, Backend, BD).
- Diagrama de entidad-relacion inicial.
- Catalogo de endpoints REST (metodo, ruta, request, response).
- Capturas de interfaz (alta, consulta, actualizacion).
- Evidencia de pruebas (unitarias/integracion) y resultados.
- Tablero simple de indicadores antes vs despues del proceso manual.

---

## 8. Conclusiones Esperadas

La propuesta aporta valor organizacional al transformar un proceso operativo con alta dependencia manual en un flujo digital estandarizado, medible y auditable. A nivel tecnico, el proyecto evidencia competencias Full Stack al integrar desarrollo de interfaz, APIs empresariales, seguridad, persistencia, pruebas y criterios de calidad orientados a un caso de uso real en Efectivale.

---

## 9. Plan de Trabajo (Diagrama de Gantt - 12 Semanas)

## 9.1 Actividades, subactividades y entregable por actividad

| # | Actividad | Subactividades | Semanas | Entregable por actividad |
|---|---|---|---|---|
| 1 | Analisis funcional y diseno tecnico | 1.1 Levantamiento de reglas de negocio. 1.2 Historias de usuario y criterios de aceptacion. 1.3 Modelo de datos inicial. 1.4 Diseno de APIs y arquitectura. | 1-2 | Documento funcional-tecnico (alcance, reglas, modelo inicial y contrato API v1). |
| 2 | Modulo de pedidos (dispercion, stock, autorizacion y pedido de tarjeta) | 2.1 Endpoints de creacion de pedidos. 2.2 Flujo de autorizacion por estado/rol. 2.3 Validaciones de negocio. 2.4 Pantallas Angular para captura y seguimiento. | 3-5 | Modulo de pedidos operando en ambiente de pruebas con evidencia de casos de uso. |
| 3 | Modulo de tarjetas (consulta, activacion y cancelacion) | 3.1 Consulta paginada/filtrada. 3.2 Activacion de tarjeta. 3.3 Cancelacion de tarjeta. 3.4 Registro de bitacora por cambio de estado. | 6-7 | Modulo de tarjetas funcional con historial basico de estados. |
| 4 | Modulo de grupos (asignacion, registro y reporte) | 4.1 Catalogo de grupos. 4.2 Asignacion de empleados a grupos. 4.3 Reporte de grupos (filtros y exportable). | 8-9 | Modulo de grupos con reporte validado por usuario de negocio. |
| 5 | Actualizacion de datos del empleado | 5.1 Formulario de actualizacion. 5.2 Validaciones de campos y reglas. 5.3 Control de cambios en datos sensibles. | 10 | Funcionalidad de actualizacion de empleado con validaciones y trazabilidad minima. |
| 6 | Pruebas integrales, estabilizacion y cierre | 6.1 Pruebas unitarias/integracion. 6.2 Correccion de defectos. 6.3 Hardening de seguridad y rendimiento. 6.4 Evidencias finales de indicadores. | 11-12 | Release candidato (MVP), reporte de pruebas y paquete de evidencias para defensa. |

## 9.2 Gantt (vista resumida)

| Actividad | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1. Analisis y diseno tecnico | X | X |  |  |  |  |  |  |  |  |  |  |
| 2. Modulo de pedidos |  |  | X | X | X |  |  |  |  |  |  |  |
| 3. Modulo de tarjetas |  |  |  |  |  | X | X |  |  |  |  |  |
| 4. Modulo de grupos |  |  |  |  |  |  |  | X | X |  |  |  |
| 5. Actualizacion de empleado |  |  |  |  |  |  |  |  |  | X |  |  |
| 6. Pruebas y cierre |  |  |  |  |  |  |  |  |  |  | X | X |

## 9.3 Factibilidad en 12 semanas

**Dictamen:**
Si, las funcionalidades son factibles en 12 semanas **si el alcance se maneja como MVP** y se priorizan flujos criticos sobre funcionalidades avanzadas.

**No sobran funcionalidades**, pero para cumplir tiempo conviene dejar fuera del MVP inicial:
- Reglas de autorizacion complejas multinivel.
- Reporteria avanzada con multiples formatos.
- Automatizaciones no esenciales (notificaciones, integraciones externas no criticas).

**Si faltan actividades transversales** para que el plan sea realista:
- Analisis/diseno inicial formal.
- Pruebas tecnicas y funcionales.
- Estabilizacion, documentacion y evidencias de entrega.

**Recomendacion para defensa:**
Presentar el proyecto como MVP de 12 semanas con roadmap de fase 2, argumentando que la primera entrega prioriza valor operativo, reduccion de tiempos y calidad de datos con riesgo controlado.
