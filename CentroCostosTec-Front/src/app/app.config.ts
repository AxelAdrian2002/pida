import { ApplicationConfig } from '@angular/core';
import { provideRouter, withRouterConfig } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { apiBaseUrlInterceptor } from './shared/interceptors/api-base-url.interceptor';
import { authInterceptor } from './shared/interceptors/auth.interceptor';
import { uiInterceptor } from './shared/interceptors/ui.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withRouterConfig({ paramsInheritanceStrategy: 'always' })),
    provideHttpClient(withInterceptors([apiBaseUrlInterceptor, authInterceptor, uiInterceptor])),
    provideAnimations()
  ]
};
