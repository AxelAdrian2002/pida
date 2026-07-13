# Fase de Pruebas - PIDA Centro Costos

## Objetivo
Elevar la evidencia de calidad para sustentar el estatus de competencia aplicada en forma excelente, con pruebas ejecutables en frontend y backend.

## Alcance
- Pruebas unitarias backend (Spring Boot / Java)
- Pruebas unitarias frontend (Angular 18)
- Evidencia de ejecucion para anexar al documento de evaluacion

## Archivos de Prueba Creados

### Backend
- `CentroCostosTec-API/src/test/java/com/efectivale/centrocostos/security/JwtUtilTest.java`
- `CentroCostosTec-API/src/test/java/com/efectivale/centrocostos/service/CloudinaryStorageServiceTest.java`

### Frontend
- `CentroCostosTec-Front/src/app/services/auth.service.spec.ts`
- `CentroCostosTec-Front/src/app/features/registro-empresa/registro-empresa.form.spec.ts`

## Casos Cubiertos

### Backend
1. `JwtUtilTest`
- Generacion y lectura de claims extendidos (usuario, rol, cliente, permisos)
- Validacion de token alterado

2. `CloudinaryStorageServiceTest`
- Rechazo de archivo nulo
- Rechazo de archivo no imagen
- Rechazo cuando faltan credenciales de Cloudinary

### Frontend
1. `AuthService`
- Deteccion de sesion activa por token
- Resolucion de rol por prefijo
- Limpieza de credenciales en logout

2. `RegistroEmpresaComponent`
- Validacion obligatoria de `municipio`
- Validacion obligatoria de `colonia`

## Ejecucion

### Backend
```bash
cd CentroCostosTec-API
mvn test
```

### Frontend
```bash
cd CentroCostosTec-Front
npm install
npm run test -- --watch=false --browsers=ChromeHeadless
```

## Evidencia sugerida para anexar
- Captura de consola de `mvn test` con tests en verde
- Captura de consola de `ng test`/`npm run test` con tests en verde
- Conteo final de pruebas y tiempo de ejecucion

## Indicador de mejora para evaluacion
- Meta propuesta: reducir tiempo promedio de procesamiento de 48h a 4h
- Resultado reportado: 2-3h
- Cumplimiento: 125% (excede meta >20%)

Con esta fase, el indicador se complementa con evidencia tecnica reproducible de calidad y control del software.
