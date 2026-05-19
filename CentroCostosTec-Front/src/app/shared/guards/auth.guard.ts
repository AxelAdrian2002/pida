import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    const requiereCambio = authService.requiresPasswordChange();
    const esRutaCambioPassword = state.url.includes('/Administracion_Login/Cambio_Password')
      || state.url.includes('/cambio-contrasenia')
      || state.url.includes('/Cambio_de_contrasenia');

    if (requiereCambio && !esRutaCambioPassword) {
      router.navigate(['/Administracion_Login/Cambio_Password']);
      return false;
    }

    return true;
  }
  router.navigate(['/login']);
  return false;
};
