# Despliegue a Ambiente de Pruebas

Este documento describe cómo subir el proyecto a un ambiente de pruebas con:

1. Backend: Render
2. Frontend: Firebase Hosting
3. Base de datos: Render PostgreSQL (recomendado) o Supabase (alternativa)

## 1. Cambios ya aplicados en el proyecto

Se realizaron estos ajustes para soportar deployment:

1. Frontend con base URL configurable por entorno:
   1. [CentroCostosTec-Front/src/environments/environment.ts](CentroCostosTec-Front/src/environments/environment.ts)
   2. [CentroCostosTec-Front/src/environments/environment.prod.ts](CentroCostosTec-Front/src/environments/environment.prod.ts)
   3. [CentroCostosTec-Front/src/app/shared/interceptors/api-base-url.interceptor.ts](CentroCostosTec-Front/src/app/shared/interceptors/api-base-url.interceptor.ts)
   4. Registro del interceptor en [CentroCostosTec-Front/src/app/app.config.ts](CentroCostosTec-Front/src/app/app.config.ts)
   5. Reemplazo de environment para build de producción en [CentroCostosTec-Front/angular.json](CentroCostosTec-Front/angular.json)

2. Backend con variables de entorno para seguridad y CORS:
   1. [CentroCostosTec-API/src/main/resources/application.properties](CentroCostosTec-API/src/main/resources/application.properties)
   2. `app.jwt.secret` ahora usa `APP_JWT_SECRET`
   3. `app.cors.allowed-origins` ahora usa `APP_CORS_ALLOWED_ORIGINS`
   4. SMTP por variables (`APP_MAIL_*`) sin credenciales hardcodeadas

3. Archivos de despliegue:
   1. Render blueprint: [render.yaml](render.yaml)
   2. Docker backend: [CentroCostosTec-API/Dockerfile](CentroCostosTec-API/Dockerfile)
   3. Firebase hosting config: [CentroCostosTec-Front/firebase.json](CentroCostosTec-Front/firebase.json)

## 2. Recomendación de arquitectura para pruebas

## Opción recomendada

1. Backend + DB en Render
2. Frontend en Firebase

Ventajas:

1. Menor complejidad operativa
2. Menos latencia backend-DB
3. Menos puntos de falla

## Opción alternativa

1. Backend en Render
2. Frontend en Firebase
3. DB en Supabase

Úsala si necesitas su consola SQL/UX, pero agrega `sslmode=require` en conexión.

## 3. Paso a paso: Base de datos en Render

1. Crear PostgreSQL en Render (plan dev).
2. Guardar estos datos:
   1. Host
   2. Port
   3. Database
   4. User
   5. Password
   6. Internal/External URL
3. Cargar esquema y datos iniciales (una sola vez):

```powershell
psql "postgresql://USER:PASSWORD@HOST:PORT/DB?sslmode=require" -f CentroCostosTec-API/src/main/resources/schema.sql
psql "postgresql://USER:PASSWORD@HOST:PORT/DB?sslmode=require" -f CentroCostosTec-API/src/main/resources/data.sql
psql "postgresql://USER:PASSWORD@HOST:PORT/DB?sslmode=require" -f CentroCostosTec-API/src/main/resources/verify-init.sql
```

Nota: en el backend la inicialización está en `never`, por eso se carga manualmente.

## 4. Paso a paso: Backend en Render

## Opción A (recomendada): usando render.yaml

1. Subir cambios a tu repositorio Git.
2. En Render: New + Blueprint.
3. Seleccionar el repo y usar [render.yaml](render.yaml).
4. Ajustar variables sensibles en Render Dashboard:
   1. `APP_JWT_SECRET`
   2. `APP_MAIL_USER`
   3. `APP_MAIL_PASS`
   4. `APP_MAIL_FROM`
5. Confirmar `APP_CORS_ALLOWED_ORIGINS` con tus dominios Firebase.

## Opción B: crear Web Service manual

1. New + Web Service.
2. Root directory: `CentroCostosTec-API`.
3. Runtime: Docker.
4. Render detectará [CentroCostosTec-API/Dockerfile](CentroCostosTec-API/Dockerfile).
5. Variables necesarias:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORT/DB?sslmode=require
APP_DB_USER=...
APP_DB_PASSWORD=...
APP_JWT_SECRET=...
APP_CORS_ALLOWED_ORIGINS=https://TU_PROYECTO.web.app,https://TU_PROYECTO.firebaseapp.com,http://localhost:4200
APP_MAIL_HOST=smtp.gmail.com
APP_MAIL_PORT=587
APP_MAIL_USER=...
APP_MAIL_PASS=...
APP_MAIL_FROM=...
```

## Verificación backend

1. Probar endpoint:

```text
https://TU_BACKEND.onrender.com/api/testing/token
```

## Variables recomendadas (copiar/pegar)

Puedes usar como plantilla:

1. [CentroCostosTec-API/.env.render.example](CentroCostosTec-API/.env.render.example)

En Render crea estas variables con esos mismos nombres.

2. Si responde JSON con token, backend está OK.

## 5. Paso a paso: Frontend en Firebase Hosting

1. Editar [CentroCostosTec-Front/src/environments/environment.prod.ts](CentroCostosTec-Front/src/environments/environment.prod.ts)
2. Cambiar:

```ts
apiBaseUrl: 'https://TU_BACKEND_RENDER_URL'
```

Ejemplo:

```ts
apiBaseUrl: 'https://centrocostos-tec-api-dev.onrender.com'
```

3. Build local:

```powershell
cd CentroCostosTec-Front
npm ci
npm run build -- --configuration production
```

4. Inicializar Firebase (si es primera vez):

```powershell
npm install -g firebase-tools
firebase login
firebase init hosting
```

Respuestas sugeridas en init:

1. Seleccionar proyecto Firebase de pruebas
2. Public directory: `dist/centrocostos-tec-front/browser`
3. Single-page app rewrite: `Yes`

5. Deploy:

```powershell
firebase deploy --only hosting
```

La configuración SPA ya está en [CentroCostosTec-Front/firebase.json](CentroCostosTec-Front/firebase.json).

## 6. Opción DB con Supabase (si decides usarla)

1. Crear proyecto en Supabase.
2. Tomar URL de conexión PostgreSQL.
3. Configurar backend:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORT/postgres?sslmode=require
APP_DB_USER=...
APP_DB_PASSWORD=...
```

4. Ejecutar carga de esquema/datos con `psql` igual que Render.
5. Mantener el backend en Render sin otros cambios.

## 7. Checklist rápido de salida a pruebas

1. Backend responde `/api/testing/token`.
2. Front carga en Firebase sin errores de consola.
3. Login funciona desde Firebase contra Render.
4. CORS permite dominio `*.web.app` y `*.firebaseapp.com`.
5. CRUD principal de solicitudes funciona.
6. Reportes cargan datos.
7. Logs en Render sin errores de conexión DB.

## 8. Problemas comunes

1. Error CORS:
   1. Revisar `APP_CORS_ALLOWED_ORIGINS`.
2. Error 401/403:
   1. Verificar JWT secret y token emitido.
3. Front no pega al backend:
   1. Revisar `environment.prod.ts` y rebuild.
4. Error de DB SSL:
   1. Asegurar `?sslmode=require` en URL PostgreSQL.

## 9. Comandos resumidos

## Backend (Render)

No requiere comando local si despliegas desde Git + Dockerfile.

## Frontend (Firebase)

```powershell
cd CentroCostosTec-Front
npm ci
npm run build -- --configuration production
firebase deploy --only hosting
```

## DB seed (una sola vez)

```powershell
psql "postgresql://USER:PASSWORD@HOST:PORT/DB?sslmode=require" -f CentroCostosTec-API/src/main/resources/schema.sql
psql "postgresql://USER:PASSWORD@HOST:PORT/DB?sslmode=require" -f CentroCostosTec-API/src/main/resources/data.sql
```
