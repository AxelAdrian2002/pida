import { HttpEventType, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, tap } from 'rxjs/operators';
import { throwError } from 'rxjs';
import Swal from 'sweetalert2';
import { UiLoaderService } from '../../services/ui-loader.service';

function shouldShowPostFeedback(url: string): boolean {
  const cleanUrl = (url || '').toLowerCase();

  // El login debe entrar directo al inicio sin alerta global.
  if (cleanUrl.includes('/login/v1/login') || cleanUrl.endsWith('/auth/login')) {
    return false;
  }

  // La fase de prefactura (procesar) no debe disparar exito global.
  if (cleanUrl.includes('/procesar')) {
    return false;
  }

  // El guardado de pedido tiene flujo especial en la vista de prefactura.
  if (cleanUrl.includes('/guardarpedido')) {
    return false;
  }

  return true;
}

function resolveModuleRoot(url: string): string | null {
  const cleanUrl = url.split('?')[0].split('#')[0];
  const firstSegment = cleanUrl.split('/').filter(Boolean)[0] || '';

  switch (firstSegment.toLowerCase()) {
    case 'solicitudes':
    case 'prefactura':
      return '/solicitudes';
    case 'credenciales':
      return '/credenciales';
    case 'grupos':
      return '/grupos';
    case 'empleados':
      return '/empleados';
    default:
      return null;
  }
}

export const uiInterceptor: HttpInterceptorFn = (req, next) => {
  const loader = inject(UiLoaderService);
  const router = inject(Router);
  const isPost = req.method === 'POST';
  const shouldHandlePostFeedback = isPost && shouldShowPostFeedback(req.url);

  loader.show();

  return next(req).pipe(
    tap((event) => {
      if (!shouldHandlePostFeedback || event.type !== HttpEventType.Response) {
        return;
      }

      const body = (event as any)?.body as Record<string, unknown> | undefined;
      const successMessage = body?.['mensaje'] || body?.['message'] || 'La accion se realizo correctamente.';
      const moduleRoot = resolveModuleRoot(router.url);

      void Swal.fire({
        icon: 'success',
        title: 'Operacion exitosa',
        text: String(successMessage),
        confirmButtonText: 'Aceptar'
      }).then(() => {
        if (moduleRoot && moduleRoot !== '/solicitudes' && router.url !== moduleRoot) {
          void router.navigateByUrl(moduleRoot);
        }
      });
    }),
    catchError((error) => {
      if (!shouldHandlePostFeedback) {
        return throwError(() => error);
      }

      const reason =
        error?.error?.mensaje ||
        error?.error?.message ||
        error?.message ||
        'Ocurrio un error al procesar la solicitud.';

      void Swal.fire({
        icon: 'error',
        title: 'Error en la operacion',
        text: String(reason),
        confirmButtonText: 'Aceptar'
      });

      return throwError(() => error);
    }),
    finalize(() => {
      loader.hide();
    })
  );
};
